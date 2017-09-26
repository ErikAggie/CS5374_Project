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

    CodeBlock(String blockInfo, String contents)
    {
        this.blockInfo = blockInfo;
        this.contents = contents;
    }

    public String getBlockInfo() {
        return blockInfo;
    }

    public String getContents() {
        return contents;
    }
}
