package edu.ttu.erikpeterson.cs5381.parser;

import java.util.ArrayList;
import java.util.List;

public class CodeBlock {

    /**
     * The information right before this block
     */

    private final String blockInfo;

    private final CodeBlockType blockType;

    /**
     * Everything between '{' and '}'
     */
    private final String contents;

    /**
     * The beginning position of this block (including header stuff) in the file
     */
    private final int startPosition;

    /**
     * The end position of this block in the file
     */
    private final int endPosition;

    private List<CodeBlock> subCodeBlocks = new ArrayList<>();

    CodeBlock(String blockInfo, CodeBlockType blockType, String contents, int startPosition, int endPosition)
    {
        this.blockInfo = blockInfo;
        this.blockType = blockType;
        this.contents = contents;
        this.startPosition = startPosition;
        this.endPosition = endPosition;
    }

    public String getBlockInfo() {
        return blockInfo;
    }

    public String getContents() {
        return contents;
    }

    public int getStartPosition() { return startPosition; }

    public int getEndPosition() { return endPosition; }

    public CodeBlockType getBlockType() {
        return blockType;
    }

    public void addCodeBlocks(List<CodeBlock> subCodeBlocks) {
        for ( CodeBlock subCodeBlock : subCodeBlocks)
        {
            this.subCodeBlocks.add(subCodeBlock);
        }
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

    public CodeBlock getSubCodeBlock(int index)
    {
        if ( subCodeBlocks.size() <= index)
        {
            throw new ArrayIndexOutOfBoundsException("Only " + subCodeBlocks.size() + " sub code blocks; asked for index " + index);
        }
        return subCodeBlocks.get(index);
    }
}
