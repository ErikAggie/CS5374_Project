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
            position = findBlock(contents, codeBlocks, position);
        }

        return codeBlocks;
    }


    /**
     * Recursively find code blocks
     *
     * @param contents contents of the Java file
     * @param codeBlocks List of code blocks--expect stuff to be added to it!
     * @param startPosition Where to start looking in the file
     * @return Position after the code block, or Integer.MAX_VALUE if none found
     */
    private static int findBlock(String contents, LinkedList<CodeBlock> codeBlocks, int startPosition) {
        int numBlocksToStart = codeBlocks.size();
        int firstOpenParen = contents.indexOf('{', startPosition);
        if (firstOpenParen < 0) {
            // No more blocks
            return -1;
        }

        // Grab the info for this block (the stuff just before the '}' and after the previous ';' or '}'
        String blockInfo;
        int previousSemicolonPosition = contents.lastIndexOf(';', startPosition - 1);
        int previousOpenParenPosition = contents.lastIndexOf('{', startPosition - 1);
        int mostRecentPosition = Math.max(previousSemicolonPosition, previousOpenParenPosition);
        if (mostRecentPosition < 0) {
            blockInfo = contents.substring(0, startPosition);
        } else {
            blockInfo = contents.substring(Math.max(previousOpenParenPosition, previousSemicolonPosition), startPosition-1);
        }

        int newStartPosition = firstOpenParen + 1;

        // Now recursively look for internal code blocks
        while (true) {
            int endOfBlock = findBlock(contents, codeBlocks, newStartPosition);
            if (endOfBlock < 0) {
                // No more internal blocks
                break;
            } else {
                // Need to look for more internal blocks
                newStartPosition = endOfBlock + 1;
            }
        }

        // Now that we've found all the internal code blocks, the next '}' is the end of our block
        // Assuming well-formed code, and no '{' or '}' in comments....
        int closeOfOurBlock = contents.indexOf('}', newStartPosition);
        String blockContents = contents.substring(firstOpenParen + 1, closeOfOurBlock);

        // Add our code block so that it's before the sub-blocks (aka after everything that came before us)
        codeBlocks.add(numBlocksToStart, new CodeBlock(blockInfo, blockContents));
        return closeOfOurBlock + 1;
    }
/*

            int semicolonPosition = contents.indexOf(';', startPosition);
            semicolonPosition = (semicolonPosition >= 0) ? semicolonPosition : Integer.MAX_VALUE;
            int closeParenPosition = contents.indexOf('}', startPosition);

            String blockInfo = null;
            String blockContents = null;

            if ( semicolonPosition > contents.length() &&
                 firsopenParenPosition > contents.length()) {
                // Reached the end of the file without finding anything
            }
            else if ( closeParenPosition < openParenPosition)
            {
                // Our block is closing, so we're done
                // Add us to the spot we originally started with
                if ( blockInfo != null)
                {
                    String blockContents =
                    codeBlocks.add(numBlocksToStart, new CodeBlock(blockInfo)
                }
                return closeParenPosition;
            }

            else if ( semicolonPosition < openParenPosition )
            {
                startPosition = semicolonPosition + 1;
            }
            else
            {
                // Found a '{'!
                // Grab stuff between the '{' and the preceding '}' and ';'

                int lastInternalPosition = findBlock(contents, codeBlocks, startPosition + 1);

                int closeParenPosition
            }
        }
        return

    }*/

}
