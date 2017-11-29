package edu.ttu.erikpeterson.cs5381.parser.block;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MethodBlock extends CodeBlock {

    private static final Pattern VARIABLE_DECLARE_ASSIGN = Pattern.compile("^(final\\s+)?([\\w\\<\\>]+)\\s+(\\w+) =");
    private static final Pattern VARIABLE_DECLARE = Pattern.compile("^(final\\s+)?([\\w\\<\\>]+)\\s+(\\w+)$");

    // Pattern for synchronized blocks
    private static final Pattern SYNCHRONIZED_PATTERN = Pattern.compile("\\s*synchronized\\s*\\(\\s*(\\w+)\\s*\\)");

    // Pattern for method calls
    private static final Pattern METHOD_CALL_PATTERN_1 = Pattern.compile("\\s*(\\w+)\\.(\\w+)\\s*\\(");
    // This one is for new Something().method(...)
    private static final Pattern METHOD_CALL_PATTERN_2 = Pattern.compile("new\\s+(\\w+)\\s*\\([\\s\\w]*\\)\\s*\\.\\s*(\\w+)\\s*\\(");
    private static final Pattern INTRA_CLASS_METHOD_CALL = Pattern.compile("\\s*(\\w+)\\s*\\(");


    private final Map<String, String> variables = new HashMap<>();
    private boolean foundVariables = false;

    private String thisMethodsCode = "";


    /**
     * Constructor
     *
     * @param blockInfo Block information
     * @param blockType Kind of block
     * @param contents Block contents
     * @param fileContents Full file contents
     * @param startPosition Start of this block in the file (including block info)
     * @param endPosition End of this block in the file
     */
    MethodBlock(String blockInfo,
                CodeBlockType blockType,
                String contents,
                String fileContents,
                int startPosition,
                int endPosition)
    {
        super(blockInfo, blockType, contents, fileContents, startPosition, endPosition);
    }

    @Override
    public boolean equals(Object other)
    {
        if ( !(other instanceof MethodBlock))
        {
            return false;
        }

        MethodBlock otherMethodBlock = (MethodBlock) other;
        return ( startPosition == otherMethodBlock.startPosition &&
                 endPosition == otherMethodBlock.endPosition &&
                 blockType == otherMethodBlock.blockType &&
                 blockInfo.equals(otherMethodBlock.blockInfo) &&
                 contents.equals(otherMethodBlock.contents));
    }

    @Override
    public int hashCode() {
        return 31 * blockInfo.hashCode() + contents.hashCode();
    }

    public Map<String, String> getVariables()
    {
        if ( !foundVariables)
        {
            findVariables();
        }
        return variables;
    }

    public String getThisMethodsCode()
    {
        if ( thisMethodsCode.isEmpty())
        {
            findThisMethodsCode();
        }
        return thisMethodsCode;
    }

    /**
     * Walk through this method, looking for locks and unlocks
     */
    public void walkMethod(List<CodeBlock> allCodeBlocks, List<LockInfo> lockInfoList)
    {
        // Get ready
        if ( !foundVariables)
        {
            findVariables();
        }
        findThisMethodsCode();

        String[] statements = thisMethodsCode.split("[{;}]");
        for ( String statement : statements)
        {
            checkForLocks(statement, lockInfoList);
            checkForMethodCall(statement, allCodeBlocks, lockInfoList);
        }
    }

    private void checkForLocks(String statement, List<LockInfo> lockInfoList)
    {
        Matcher synchronizedMatcher = SYNCHRONIZED_PATTERN.matcher(statement);
        if ( synchronizedMatcher.find())
        {
            // Found a synchronized block. See if we can find the variable's type
            String variable = synchronizedMatcher.group(1);
            if ( variable.isEmpty())
            {
                System.out.println("Found empty variable in " + statement);
                return;
            }

            String type;

            if ( !variables.containsKey(variable)) {
                return;
            }

            type = variables.get(variable);
            lockInfoList.add(new LockInfo(variable, type, true));
        }

        // TODO: add unlock and other lock types
    }

    private void checkForMethodCall(String statement, List<CodeBlock> allCodeBlocks, List<LockInfo> lockInfoList)
    {
        Matcher regularCallMatcher = METHOD_CALL_PATTERN_1.matcher(statement);
        Matcher newCallMatcher = METHOD_CALL_PATTERN_2.matcher(statement);
        Matcher intraClassMatcher = INTRA_CLASS_METHOD_CALL.matcher(statement);
        String variableOrClass;
        String method;

        // Note that this does not cover nested calls e.g. myString.substring(...).length
        if ( regularCallMatcher.find())
        {
            variableOrClass = regularCallMatcher.group(1);
            method = regularCallMatcher.group(2);
        }
        else if ( newCallMatcher.find())
        {
            variableOrClass = newCallMatcher.group(1);
            method = newCallMatcher.group(2);
        }
        // This regex will also catch new Thread(), in which we need to call the constructor
        else if ( intraClassMatcher.find())
        {
            if (statement.contains("new "))
            {
                // This is a call to a constructor
                variableOrClass = intraClassMatcher.group(1);
                method = intraClassMatcher.group(1);
            }
            else
            {
                variableOrClass = findTopParent().getName();
                method = intraClassMatcher.group(1);
            }
        }
        else
        {
            // No match
            return;
        }

        // Note that there's a hole here for inner classes (such as calling OuterClass.this.method())
        if ( variableOrClass.equals("this"))
        {
            variableOrClass = this.parent.getName();
        }

        if ( variables.containsKey(variableOrClass))
        {
            // This is a variable; we want the class
            variableOrClass = variables.get(variableOrClass);
        }
        // Remove any generic info (<..>)
        variableOrClass = variableOrClass.replaceAll("<\\s*>", "");

        for ( CodeBlock codeBlock : allCodeBlocks)
        {
            if (codeBlock.getName().equals(variableOrClass))
            {
                // Found one of our classes. See if it has this method
                // Another hole here: this doesn't consider method overloads (e.g. substring(int) and substring(int, int)
                for ( CodeBlock subCodeBlock : codeBlock.getSubCodeBlocks())
                {
                    if ( subCodeBlock == null || subCodeBlock.getName() == null)
                    {
                        continue;
                    }
                    // Be sure we don't include calls to ourself (recursive and catching the declaration line)
                    if ( subCodeBlock instanceof MethodBlock &&
                         subCodeBlock != this &&
                         subCodeBlock.getName().equals(method))
                    {
                        // We found something we should call!
                        ((MethodBlock) subCodeBlock).walkMethod(allCodeBlocks, lockInfoList);
                        return;
                    }
                }
            }
        }
    }

    private ClassBlock findTopParent()
    {
        CodeBlock topParent = this;
        while ( topParent.parent != null)
        {
            topParent = topParent.parent;
        }

        if ( !(topParent instanceof ClassBlock))
        {
            throw new IllegalArgumentException(topParent.getName() + " ought to be a ClassBlock, but isn't!");
        }

        return ((ClassBlock) topParent);
    }

    /**
     * Remove all the interior method blocks so we have exactly what this method will execute.
     * Delayed construction to allow subblocks to be added
     */
    private void findThisMethodsCode()
    {
        if ( !thisMethodsCode.isEmpty())
        {
            // Already done
            return;
        }

        if ( subCodeBlocks.isEmpty())
        {
            thisMethodsCode = contents;
            return;
        }

        StringBuilder builder = new StringBuilder();
        // The first '{' will be the end of the method's signiture. Stip it
        int startLoc = fileContents.indexOf("{", startPosition) + 1;

        findCodeInBlock(this, builder, startLoc);

        thisMethodsCode = builder.toString();
    }

    /**
     * Find all the code in a block. Recurses to sub-blocks as needed
     *
     * @param block The block to examine
     * @param builder Holds the code we parse out
     * @param startLoc Location to start looking
     * @return The end point of this block
     */
    private int findCodeInBlock(CodeBlock block, StringBuilder builder, int startLoc)
    {
        if ( block != this &&
             block instanceof MethodBlock)
        {
            // skip it
            return block.endPosition;
        }

        for ( CodeBlock subBlock : block.subCodeBlocks)
        {
            // Grab everything up to the '{' at the beginning of this block
            int startOfNextBlock = fileContents.indexOf("{", startLoc) + 1;
            builder.append(fileContents.substring(startLoc, startOfNextBlock));
            startLoc = findCodeInBlock(subBlock, builder, startOfNextBlock);
        }

        // Now add whatever's left
        builder.append(fileContents.substring(startLoc, block.endPosition));
        return block.endPosition;
    }

    /**
     * Find the variables used in this method.
     * Delayed construction in case this method doesn't get executed.
     *
     * Note that this method will find variables in nested methods. This is okay, since they're only looked up
     * when a variable is found
     */
    private void findVariables()
    {
        if ( foundVariables)
        {
            return;
        }
        String[] statements = contents.split("[;\\{\\}]");

        for ( String statement : statements)
        {
            statement = statement.trim();
            // Don't care about return statements
            if ( statement.startsWith("return")) {
                continue;
            }
            Matcher variableDeclareAssignMatcher = VARIABLE_DECLARE_ASSIGN.matcher(statement);
            Matcher variableDeclareMatcher = VARIABLE_DECLARE.matcher(statement);
            if ( variableDeclareAssignMatcher.find())
            {
                if ( !variables.containsKey(variableDeclareAssignMatcher.group(3)))
                {
                    variables.put(variableDeclareAssignMatcher.group(3), variableDeclareAssignMatcher.group(2));

                }
            }
            else if (variableDeclareMatcher.find())
            {
                if ( !variables.containsKey(variableDeclareAssignMatcher.group(3)))
                {
                    variables.put(variableDeclareMatcher.group(3), variableDeclareMatcher.group(2));
                }
            }
        }

        // Add all the class variables so they're easier to find
        variables.putAll(findTopParent().getClassVariables());
        foundVariables = true;
    }

}
