package edu.ttu.erikpeterson.cs5381.parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.Scanner;

public class CodeBlockParser {

    /**
     * @param file File to parse
     * @return The blocks in this file
     */
    public static LinkedList<CodeBlock> parse(File file) throws FileNotFoundException {
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
     * @return The CodeBlock (if any) found
     */
    private static CodeBlock findBlock(String contents, LinkedList<CodeBlock> codeBlocks, int startPosition) {
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
            CodeBlock codeBlock = new CodeBlock(blockInfo, blockContents, blockInfoStart, nextCloseParen);
            codeBlocks.add(numBlocksToStart, codeBlock);
            return codeBlock;
        }

        // Now recursively look for internal code blocks
        while (true) {
            CodeBlock internalCodeBlock = findBlock(contents, codeBlocks, newStartPosition);
            if (internalCodeBlock == null) {
                // No more internal blocks
                break;
            } else {
                // Need to look for more internal blocks
                newStartPosition = internalCodeBlock.getEndPosition() + 1;
            }
        }

        // Now that we've found all the internal code blocks, the next '}' is the end of our block
        // Assuming well-formed code, and no '{' or '}' in comments....
        int closeOfOurBlock = contents.indexOf('}', newStartPosition);
        String blockContents = contents.substring(firstOpenParen + 1, closeOfOurBlock);

        // Add our code block so that it's before the sub-blocks (aka after everything that came before us)
        CodeBlock ourCodeBlock = new CodeBlock(blockInfo, blockContents, blockInfoStart, closeOfOurBlock);
        codeBlocks.add(numBlocksToStart, ourCodeBlock);
        return ourCodeBlock;
    }

}
