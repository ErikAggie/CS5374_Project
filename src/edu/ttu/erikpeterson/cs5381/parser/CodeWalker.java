package edu.ttu.erikpeterson.cs5381.parser;

import java.util.ArrayList;
import java.util.List;

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
