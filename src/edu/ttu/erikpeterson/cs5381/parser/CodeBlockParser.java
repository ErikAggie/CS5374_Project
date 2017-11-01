package edu.ttu.erikpeterson.cs5381.parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CodeBlockParser {

    private static final Pattern CLASS_PATTERN = Pattern.compile("\\sclass\\s+(\\w+)");
    private static final Pattern SYNCHRONIZED_PATTERN = Pattern.compile("\\s*(synchronized)\\s*\\(");

    // Pattern for creating and submitting a future
    private static final Pattern FUTURE_SUBMISSION = Pattern.compile("Future\\s*<.*>.*submit\\s*\\(");

    // Main method pattern (with allowable whitespace between words)
    private static final Pattern MAIN_METHOD = Pattern.compile("public\\s+static\\s+void\\s+main\\s*\\(\\s*String\\s*\\[\\s*\\].+\\)");

    // Because the method pattern might match loops (for, while, etc.) we need to dispose of them first
    // Note that a for loop is handled in code because our parsing doesn't capture all of it (just the increment part after the last ';')
    private static final Pattern FOR_EACH_PATTERN = Pattern.compile("for\\s\\(.*:.*\\)");
    private static final Pattern WHILE_PATTERN = Pattern.compile("(while)\\s*\\(");
    private static final Pattern DO_PATTERN = Pattern.compile("^do$");

    private static final Pattern TRY_PATTERN = Pattern.compile("^try$");
    private static final Pattern CATCH_PATTERN = Pattern.compile("^catch[\\s]*\\(");
    private static final Pattern FINALLY_PATTERN = Pattern.compile("^finally$");

    // Attempts to match a method. Because this could well match while and synchronized blocks, it needs to happen last
    private static final Pattern METHOD_PATTERN = Pattern.compile("(public|private|protected)?\\s*(static)?\\s*\\w*\\s+(\\w+)\\s*\\([\\w\\[\\]<>\\s,]*\\)");
    //private static final Pattern METHOD_PATTERN = Pattern.compile("(public|private|protected)?\\s*(static)?[\\w\\s]*(\\w*)([\\w\\[\\]<>\\s,]*)");

    public static List<CodeBlock> parsePath(File directory) throws  FileNotFoundException, BlockParsingException {

        if ( !directory.exists())
        {
            throw new FileNotFoundException("Directory " + directory.getAbsolutePath() + " can't be found!");
        }

        if ( !directory.isDirectory())
        {
            return parse(directory);
        }

        List<CodeBlock> codeBlocks = new LinkedList<>();

        for ( File fileOrDirectory : directory.listFiles())
        {
            // If this is a file, the above if check will handle it
            codeBlocks.addAll(parsePath(fileOrDirectory));
        }

        return codeBlocks;
    }

    /**
     * @param file File to parse
     * @return The blocks in this file
     */
    public static List<CodeBlock> parse(File file) throws FileNotFoundException, BlockParsingException {
        List<CodeBlock> codeBlocks = new LinkedList<>();

        String contents = new Scanner(file).useDelimiter("\\Z").next();
        contents = removeAllComments(contents);

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

    private static String removeAllComments(String contents)
    {
        // Remove all /*...*/, even if there are newlines in the middle
        contents = contents.replaceAll("\\/\\*[\\W\\w]*\\*\\/", "");

        // Remove all //....
        contents = contents.replaceAll("\\/\\/.*\\n", "");

        return contents;
    }


    /**
     * Recursively find code blocks
     *
     * @param contents contents of the Java file
     * @param codeBlocks List of code blocks--expect stuff to be added to it!
     * @param startPosition Where to start looking in the file
     * @return All class code blocks (methods and whatnot are held internally)
     */
    private static CodeBlock findBlock(String contents, List<CodeBlock> codeBlocks, int startPosition) throws BlockParsingException {
        int numBlocksToStart = codeBlocks.size();
        int firstOpenBrace = contents.indexOf('{', startPosition);
        int firstCloseBrace = contents.indexOf('}', startPosition);
        if (firstOpenBrace < 0 || firstCloseBrace < firstOpenBrace) {
            // No more blocks
            return null;
        }

        // Grab the info for this block (the stuff just before the '}' and after the previous ';' or '}'
        String blockInfo;
        // Note that for loops (which include ';'s will be truncated here, but we don't need to identify them... :)
        int previousSemicolonPosition = contents.lastIndexOf(';', firstOpenBrace-1);
        int previousOpenBracePosition = contents.lastIndexOf('{', firstOpenBrace-1);
        int previousCloseBracePosition = contents.lastIndexOf('}', firstOpenBrace-1);
        int mostRecentPosition = Math.max(Math.max(previousSemicolonPosition, previousOpenBracePosition), previousCloseBracePosition)+1;
        if (mostRecentPosition < 0) {
            blockInfo = contents.substring(0, startPosition);
        } else if ( firstOpenBrace == mostRecentPosition) {
            blockInfo = "";
        } else {
            blockInfo = contents.substring(mostRecentPosition+1, firstOpenBrace);
        }
        int blockInfoStart = startPosition-blockInfo.length();
        blockInfo = blockInfo.trim();

        int newStartPosition = firstOpenBrace + 1;

        // See if there are blocks internal to us
        int nextOpenParen = contents.indexOf('{', newStartPosition);
        int nextCloseParen = contents.indexOf('}', newStartPosition);

        if ( nextOpenParen > nextCloseParen)
        {
            // We are a self-contained block
            String blockContents = contents.substring(firstOpenBrace + 1, nextCloseParen);
            CodeBlockType blockType = getBlockType(contents, blockInfo, blockInfoStart);
            CodeBlock codeBlock = new CodeBlock(blockInfo,
                                                blockType,
                                                blockContents,
                                                blockInfoStart,
                                                nextCloseParen);
            addNameIfNeeded(codeBlock);
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
        String blockContents = contents.substring(firstOpenBrace + 1, closeOfOurBlock);

        // Add our code block so that it's before the sub-blocks (aka after everything that came before us)
        CodeBlockType blockType = getBlockType(contents, blockInfo, blockInfoStart);
        CodeBlock ourCodeBlock = new CodeBlock(blockInfo,
                                               blockType,
                                               blockContents,
                                               blockInfoStart,
                                               closeOfOurBlock);
        addNameIfNeeded(ourCodeBlock);
        ourCodeBlock.addCodeBlocks(subCodeBlocks);
        if ( blockType == CodeBlockType.CLASS)
        {
            codeBlocks.add(numBlocksToStart, ourCodeBlock);
        }
        return ourCodeBlock;
    }

    private static void addNameIfNeeded(CodeBlock codeBlock)
    {
        CodeBlockType blockType = codeBlock.getBlockType();
        if ( blockType == CodeBlockType.CLASS)
        {
            // Grab the class name
            Matcher classMatcher = CLASS_PATTERN.matcher(codeBlock.getBlockInfo());
            if ( classMatcher.find())
            {
                codeBlock.setName(classMatcher.group(1));
            }
        }
        else if ( blockType == CodeBlockType.METHOD)
        {
            // Grab the method name
            Matcher methodMatcher = METHOD_PATTERN.matcher(codeBlock.getBlockInfo());
            if ( methodMatcher.find())
            {
                codeBlock.setName(methodMatcher.group(3));
            }
        }
    }

    private static CodeBlockType getBlockType(String fullText, String blockInfo, int blockInfoPosition) throws BlockParsingException
    {
        // Check for class " ... class ... "
        if ( CLASS_PATTERN.matcher(blockInfo).find())
        {
            return CodeBlockType.CLASS;
        }

        if ( SYNCHRONIZED_PATTERN.matcher(blockInfo).find())
        {
            return CodeBlockType.SYNCHRONIZED;
        }

        // Our parser will catch only the "i++)" part of a for loop because we look back to the last ';'
        if ( blockInfo.contains(")") && !blockInfo.contains("("))
        {
            return CodeBlockType.CODE_BLOCK;
        }

        // KNOWN LIMITATION: This won't catch futures that are submitted without a {} (one line Futures)
        // e.g. final Future<Integer> integerFuture = threadPool.submit(() -> new Random().nextInt(10));
        if ( FUTURE_SUBMISSION.matcher(blockInfo).find())
        {
            return CodeBlockType.THREAD_ENTRY;
        }

        if ( MAIN_METHOD.matcher(blockInfo).find())
        {
            return CodeBlockType.THREAD_ENTRY;
        }

        // TODO: May want to split out try/catch/finally at some point
        // (e.g. to check if locks done in a try are undone in a finally)
        if ( FOR_EACH_PATTERN.matcher(blockInfo).find() ||
             WHILE_PATTERN.matcher(blockInfo).find() ||
             DO_PATTERN.matcher(blockInfo).find() ||
             TRY_PATTERN.matcher(blockInfo).find() ||
             CATCH_PATTERN.matcher(blockInfo).find() ||
             FINALLY_PATTERN.matcher(blockInfo).find())
        {
            return CodeBlockType.CODE_BLOCK;
        }

        if ( METHOD_PATTERN.matcher(blockInfo).find()) {
            return CodeBlockType.METHOD;
        }

        throw new BlockParsingException("Unknown block type: " + blockInfo);
    }

}
