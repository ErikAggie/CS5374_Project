package edu.ttu.erikpeterson.cs5381.parser;

import edu.ttu.erikpeterson.cs5381.parser.block.CodeBlock;
import edu.ttu.erikpeterson.cs5381.parser.block.CodeBlockType;
import edu.ttu.erikpeterson.cs5381.parser.block.LockInfo;
import edu.ttu.erikpeterson.cs5381.parser.block.MethodBlock;

import java.util.*;

public class CodeWalker {
    private final List<CodeBlock> codeBlockList;
    private final List<MethodBlock> threadStarts = new ArrayList<>();

    public CodeWalker(List<CodeBlock> codeBlockList)
    {
        this.codeBlockList = codeBlockList;
        for ( CodeBlock codeBlock : codeBlockList) {
            addThreadEntryBlocks(codeBlock);
        }
    }

    public List<MethodBlock> getThreadStarts()
    {
        return threadStarts;
    }

    /**
     * Walk through all the identified threads
     */
    public void walkAllThreadStarts()
    {
        List<List<LockInfo>> allLockInfo = new ArrayList<>();
        for ( MethodBlock threadStart : threadStarts)
        {
            walkThread(threadStart);
        }
    }

    /**
     * Walk thorugh a specific thread
     * @param thread The code block to walk
     */
    public List<LockInfo> walkThread(MethodBlock thread)
    {
        List<LockInfo> lockInfo = new ArrayList<>();
        thread.walkMethod(codeBlockList, lockInfo);

        return lockInfo;
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
            threadStarts.add((MethodBlock)codeBlock);
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
