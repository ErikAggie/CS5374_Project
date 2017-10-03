package edu.ttu.erikpeterson.cs5381.parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

public class CodeBlockParser {

    /**
     * @param file File to parse
     * @return The blocks in this file
     */
    public static LinkedList<CodeBlock> parse(File file) throws FileNotFoundException, BlockParsingException {
        LinkedList<CodeBlock> codeBlocks = new LinkedList<>();

        String contents = new Scanner(file).useDelimiter("\\Z").next();

        int position = 0;
        while (position < contents.length()) {
            CodeBlock codeBlock = findBlock(contents, codeBlocks, position);
            if ( codeBlock == null)
            {
                return codeBlocks;
            }
            position = codeBlock.getEndPosition() + 1;
        }

        return codeBlocks;
    }


    /**
     * Recursively find code blocks
     *
     * @param contents contents of the Java file
     * @param codeBlocks List of code blocks--expect stuff to be added to it!
     * @param startPosition Where to start looking in the file
     * @return All class code blocks (methods and whatnot are held internally)
     */
    private static CodeBlock findBlock(String contents, LinkedList<CodeBlock> codeBlocks, int startPosition) throws BlockParsingException {
        int numBlocksToStart = codeBlocks.size();
        int firstOpenParen = contents.indexOf('{', startPosition);
        int firstCloseParen = contents.indexOf('}', startPosition);
        if (firstOpenParen < 0 || firstCloseParen < firstOpenParen) {
            // No more blocks
            return null;
        }

        // Grab the info for this block (the stuff just before the '}' and after the previous ';' or '}'
        String blockInfo;
        // Note that for loops (which include ';'s will be truncated here, but we don't need to identify them... :)
        int previousSemicolonPosition = contents.lastIndexOf(';', firstOpenParen-1);
        int previousOpenParenPosition = contents.lastIndexOf('{', firstOpenParen-1);
        int mostRecentPosition = Math.max(previousSemicolonPosition, previousOpenParenPosition)+1;
        if (mostRecentPosition < 0) {
            blockInfo = contents.substring(0, startPosition);
        } else if ( firstOpenParen == mostRecentPosition)
        {
            blockInfo = "";
        } else {
            blockInfo = contents.substring(mostRecentPosition+1, firstOpenParen);
        }
        int blockInfoStart = startPosition-blockInfo.length();
        blockInfo = blockInfo.trim();

        int newStartPosition = firstOpenParen + 1;

        // See if there are blocks internal to us
        int nextOpenParen = contents.indexOf('{', newStartPosition);
        int nextCloseParen = contents.indexOf('}', newStartPosition);

        if ( nextOpenParen > nextCloseParen)
        {
            // We are a self-contained block
            String blockContents = contents.substring(firstOpenParen + 1, nextCloseParen);
            CodeBlockType blockType = getBlockType(contents, blockInfo, blockInfoStart);
            CodeBlock codeBlock = new CodeBlock(blockInfo,
                                                blockType,
                                                blockContents,
                                                blockInfoStart,
                                                nextCloseParen);
            if ( blockType == CodeBlockType.CLASS)
            {
                codeBlocks.add(numBlocksToStart, codeBlock);
            }
            return codeBlock;
        }

        List<CodeBlock> subCodeBlocks = new ArrayList<>();
        // Now recursively look for internal code blocks
        while (true) {
            CodeBlock internalCodeBlock = findBlock(contents, codeBlocks, newStartPosition);
            if (internalCodeBlock == null) {
                // No more internal blocks
                break;
            } else {
                subCodeBlocks.add(internalCodeBlock);
                // Need to look for more internal blocks
                newStartPosition = internalCodeBlock.getEndPosition() + 1;
            }
        }

        // Now that we've found all the internal code blocks, the next '}' is the end of our block
        // Assuming well-formed code, and no '{' or '}' in comments....
        int closeOfOurBlock = contents.indexOf('}', newStartPosition);
        String blockContents = contents.substring(firstOpenParen + 1, closeOfOurBlock);

        // Add our code block so that it's before the sub-blocks (aka after everything that came before us)
        CodeBlockType blockType = getBlockType(contents, blockInfo, blockInfoStart);
        CodeBlock ourCodeBlock = new CodeBlock(blockInfo,
                                               blockType,
                                               blockContents,
                                               blockInfoStart,
                                               closeOfOurBlock);
        ourCodeBlock.addCodeBlocks(subCodeBlocks);
        if ( blockType == CodeBlockType.CLASS)
        {
            codeBlocks.add(numBlocksToStart, ourCodeBlock);
        }
        return ourCodeBlock;
    }

    private static CodeBlockType getBlockType(String fullText, String blockInfo, int blockInfoPosition) throws BlockParsingException
    {
        if ( blockInfo.startsWith("class ") || blockInfo.contains(" class "))
        {
            // It's a class!
            return CodeBlockType.CLASS;
        }

        // TODO: flesh out...
        return CodeBlockType.METHOD;

        //throw new BlockParsingException("Unknown block type: " + blockInfo);
    }

}
