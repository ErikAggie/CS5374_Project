package edu.ttu.erikpeterson.cs5381.parser.block;

import java.util.ArrayList;
import java.util.List;

public class CodeBlock {

    /**
     * The information right before this block
     */
    protected final String blockInfo;

    protected final CodeBlockType blockType;

    protected CodeBlock parent;

    protected String name;

    /**
     * Everything between '{' and '}'
     */
    protected final String contents;

    /**
     * The full file contents
     */
    protected final String fileContents;

    /**
     * The beginning position of this block (including header stuff) in the file
     */
    protected final int startPosition;

    /**
     * The end position of this block in the file
     */
    protected final int endPosition;

    protected List<CodeBlock> subCodeBlocks = new ArrayList<>();

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
    CodeBlock(String blockInfo,
                     CodeBlockType blockType,
                     String contents,
                     String fileContents,
                     int startPosition,
                     int endPosition)
    {
        this.blockInfo = blockInfo;
        this.blockType = blockType;
        this.contents = contents;
        this.fileContents = fileContents;
        this.startPosition = startPosition;
        this.endPosition = endPosition;
    }

    public void setName(String name) { this.name = name; }

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

    public void setParent(CodeBlock parent) { this.parent = parent; }

    public CodeBlock getParent() { return parent; }

    /**
     * Add code blocks to our list of sub-blocks
     *
     * @param subCodeBlocks Blocks to add
     */
    public void addCodeBlocks(List<CodeBlock> subCodeBlocks) {
        this.subCodeBlocks.addAll(subCodeBlocks);
    }

    public boolean hasSubBlocks()
    {
        return !subCodeBlocks.isEmpty();
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
