package edu.ttu.erikpeterson.cs5381.parser.block;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClassBlock extends CodeBlock {

    private static final Pattern VARIABLE_DECLARE_PATTERN = Pattern.compile("(private|protected|public)?\\s+(static)?(final)?\\s+(\\w+)\\s+(\\w+)(\\s*=)?(\\s\\w)?$");

    private Map<String, String> classVariables;

    /**
     * Constructor. Block type is assumed
     * @param blockInfo Block information
     * @param contents Block contents
     * @param fileContents Full contents of the file
     * @param startPosition Start of this block in the file (including block info)
     * @param endPosition End of this block in the file
     */
    ClassBlock(String blockInfo,
               String contents,
               String fileContents,
               int startPosition,
               int endPosition) {
        super(blockInfo, CodeBlockType.CLASS, contents, fileContents, startPosition, endPosition);
    }

    /**
     * Finds a method with the given name
     *
     * @param methodName Method name to find
     * @return Method code block with that name, or null otherwise
     */
    public CodeBlock getMethodBlock(String methodName)
    {
        for ( CodeBlock subCodeBlock : subCodeBlocks)
        {
            // TODO: Assuming no space between the method name and open parentheses
            if ( subCodeBlock.blockType == CodeBlockType.METHOD &&
                    subCodeBlock.getBlockInfo().contains(" " + methodName + "("))
            {
                return subCodeBlock;
            }
        }

        return null;
    }

    /**
     * Return all class variables
     *
     * @return Map of class variable names to their type
     */
    public Map<String, String> getClassVariables()
    {
        if ( classVariables == null)
        {
            findClassVariables();
        }
        return classVariables;
    }

    private void findClassVariables()
    {
        classVariables = new HashMap<>();

        int currentStartPosition = this.startPosition + this.blockInfo.length();

        for ( int i=0; i<subCodeBlocks.size(); i++)
        {
            parseForClassVariables(fileContents.substring(currentStartPosition+1, subCodeBlocks.get(i).startPosition));
            currentStartPosition = subCodeBlocks.get(i).endPosition + 1;
        }

        parseForClassVariables(fileContents.substring(currentStartPosition, endPosition));
    }

    private void parseForClassVariables(String stringToParse)
    {
        String[] possibleVariables = stringToParse.split(";");

        for ( String line : possibleVariables)
        {
            line = line.trim();
            Matcher matcher = VARIABLE_DECLARE_PATTERN.matcher(line);
            if (matcher.find())
            {
                // The keys are variable names, which come after the type
                classVariables.put(matcher.group(5), matcher.group(4));
            }
        }

    }

}
