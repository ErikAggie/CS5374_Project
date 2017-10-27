package edu.ttu.erikpeterson.cs5381.parser;

import java.util.*;

public class CodeWalker {
    private final List<CodeBlock> codeBlockList;
    private final List<CodeBlock> threadStarts = new ArrayList<>();

    public CodeWalker(List<CodeBlock> codeBlockList)
    {
        this.codeBlockList = codeBlockList;
        for ( CodeBlock codeBlock : codeBlockList) {
            addThreadEntryBlocks(codeBlock);
        }
    }

    public List<CodeBlock> getThreadStarts()
    {
        return threadStarts;
    }

    /**
     * Walk through all the identified threads
     */
    public void walkAllThreadStarts()
    {
        for ( CodeBlock threadStart : threadStarts)
        {
            walkThread(threadStart);
        }
    }

    /**
     * Walk thorugh a specific thread
     * @param thread The code block to walk
     */
    public void walkThread(CodeBlock thread)
    {
        // TODO: Fill in!
    }

    /**
     * Recursive check for thread start blocks
     *
     * @param codeBlock Block to scan
     */
    private void addThreadEntryBlocks(CodeBlock codeBlock)
    {
        if ( codeBlock.getBlockType() == CodeBlockType.THREAD_ENTRY)
        {
            threadStarts.add(codeBlock);
        }
        if ( codeBlock.hasSubBlocks())
        {
            for ( CodeBlock subCodeBlock : codeBlock.getSubCodeBlocks())
            {
                addThreadEntryBlocks(subCodeBlock);
            }
        }
    }
}
