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

    private Map<String, String> findVariables()
    {
        Map<String, String> variables = new HashMap<>();
        String[] statements = contents.split("[;\\{\\}]");

        for ( String statement : statements)
        {
            statement = statement.trim();
            Matcher variableDeclareAssignMatcher = VARIABLE_DECLARE_ASSIGN.matcher(statement);
            Matcher variableDeclareMatcher = VARIABLE_DECLARE.matcher(statement);
            if ( variableDeclareAssignMatcher.find())
            {
                variables.put(variableDeclareAssignMatcher.group(3), variableDeclareAssignMatcher.group(2));
            }
            else if (variableDeclareMatcher.find())
            {
                variables.put(variableDeclareMatcher.group(3), variableDeclareMatcher.group(2));
            }
        }
        foundVariables = true;

        return variables;
    }

}
