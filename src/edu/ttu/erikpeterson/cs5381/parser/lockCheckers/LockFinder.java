package edu.ttu.erikpeterson.cs5381.parser.lockCheckers;

import edu.ttu.erikpeterson.cs5381.parser.block.LockInfo;
import edu.ttu.erikpeterson.cs5381.parser.block.MethodBlock;

import java.util.List;
import java.util.Map;

public abstract class LockFinder {

    final MethodBlock methodBlock;
    final Map<String, String> variables;

    LockFinder(MethodBlock methodBlock)
    {
        this.methodBlock = methodBlock;
        this.variables = methodBlock.getVariables();
    }

    public abstract void checkStatement(String statement, List<LockInfo> lockInfoList);
}
