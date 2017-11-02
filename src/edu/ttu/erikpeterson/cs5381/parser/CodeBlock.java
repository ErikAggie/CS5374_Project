package edu.ttu.erikpeterson.cs5381.parser;

import java.util.ArrayList;
import java.util.List;

public class CodeBlock {

    /**
     * The information right before this block
     */
    private final String blockInfo;

    private final CodeBlockType blockType;

    private String name;

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

    void setName(String name) { this.name = name; }

    public String getName() { return name; }

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

    /**
     * Add code blocks to our list of sub-blocks
     *
     * @param subCodeBlocks Blocks to add
     */
    void addCodeBlocks(List<CodeBlock> subCodeBlocks) {
        this.subCodeBlocks.addAll(subCodeBlocks);
    }

    public boolean hasSubBlocks()
    {
        return !subCodeBlocks.isEmpty();
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

    public List<CodeBlock> getSubCodeBlocks()
    {
        return subCodeBlocks;
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
