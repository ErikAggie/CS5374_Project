package edu.ttu.erikpeterson.cs5381.parser.block;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MethodBlock extends CodeBlock {

    private static final Pattern VARIABLE_DECLARE_ASSIGN = Pattern.compile("^(final\\s+)?([\\w\\<\\>]+)\\s+(\\w+) =");
    private static final Pattern VARIABLE_DECLARE = Pattern.compile("^(final\\s+)?([\\w\\<\\>]+)\\s+(\\w+)$");

    final Map<String, String> variables = new HashMap<>();
    private String thisMethodsCode = "";

    private boolean foundVariables = false;

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

    public void walkMethod(List<CodeBlock> classCodeBlocks)
    {
        if ( !foundVariables)
        {
            findVariables();
        }
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
     * Remove all the interior method blocks so we have exactly what this method will execute.
     * Delayed construction to allow subblocks to be added
     */
    private void findThisMethodsCode()
    {
        if ( subCodeBlocks.isEmpty())
        {
            thisMethodsCode = contents;
        }

        StringBuilder builder = new StringBuilder();
        findCodeInBlock(this, builder);
        /*int startLoc = fileContents.indexOf("{", startPosition) + 1;
        int endLoc = startPosition + contents.length() - 1;
        for ( CodeBlock block : subCodeBlocks)
        {
            builder.append(fileContents.substring(startLoc, block.startPosition));
            findCodeInBlock(block, builder);
            startLoc = block.endPosition - this.startPosition + 1;
        }

        // Now add whatever's left
        builder.append(fileContents.substring(startLoc, endPosition));*/

        thisMethodsCode = builder.toString();

        System.out.println("Executable code: " + thisMethodsCode);
    }

    /**
     * Find code thie method executes in a particular block.
     * Recurses as it finds subblocks
     *
     * @param block
     * @param builder
     */
    private void findCodeInBlock(CodeBlock block, StringBuilder builder)
    {
        if ( block != this &&
             block instanceof MethodBlock)
        {
            // skip it
            return;
        }

        int startLoc = fileContents.indexOf("{", block.startPosition) + 1;
        for ( CodeBlock subBlock : block.subCodeBlocks)
        {
            builder.append(fileContents.substring(startLoc, subBlock.startPosition));
            findCodeInBlock(subBlock, builder);
            startLoc = subBlock.endPosition + 1;
        }

        // Now add whatever's left
        builder.append(fileContents.substring(startLoc, endPosition));
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
        foundVariables = true;
    }

}
