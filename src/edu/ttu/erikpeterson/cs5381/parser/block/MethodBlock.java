package edu.ttu.erikpeterson.cs5381.parser.block;

public class MethodBlock extends CodeBlock {


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

    public void walkMethod()
    {

    }

}
