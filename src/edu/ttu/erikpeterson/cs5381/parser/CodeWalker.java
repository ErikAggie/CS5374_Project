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
            if ( codeBlock.getBlockType() == CodeBlockType.THREAD_ENTRY)
            {
                threadStarts.add(codeBlock);
            }
        }
    }
}
