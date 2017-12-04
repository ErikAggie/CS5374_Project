package edu.ttu.erikpeterson.cs5381.parser.block;

/**
 * Factory for creating code blocks
 */
public class CodeBlockFactory {

    /**
     * Build the proper type of code block
     *
     * @param blockInfo Block information
     * @param blockType Block type
     * @param contents Block contents
     * @param fileContents Full file contents
     * @param startPosition Start position of this block (including block info)
     * @param endPosition End position of this block
     * @return The proper kind of CodeBlock
     */
    public static CodeBlock BuildBlock(String blockInfo,
                                       CodeBlockType blockType,
                                       String contents,
                                       String fileContents,
                                       int startPosition,
                                       int endPosition)
    {
        switch ( blockType)
        {
            case CLASS:
                return new ClassBlock(blockInfo, contents, fileContents, startPosition, endPosition);
            case METHOD:
            case THREAD_ENTRY:
                return new MethodBlock(blockInfo, blockType, contents, fileContents, startPosition, endPosition);
            default:
                return new CodeBlock(blockInfo, blockType, contents, fileContents, startPosition, endPosition);
        }
    }
}
