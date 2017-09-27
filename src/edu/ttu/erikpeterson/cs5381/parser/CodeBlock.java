package edu.ttu.erikpeterson.cs5381.parser;

public class CodeBlock {

    /**
     * The information right before this block
     */

    private final String blockInfo;

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

    CodeBlock(String blockInfo, String contents, int startPosition, int endPosition)
    {
        this.blockInfo = blockInfo;
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
}
