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
        if ( methodBlock != null)
        {
            this.variables = methodBlock.getVariables();
        }
        else
        {
            this.variables = null;
        }
    }

    public abstract void checkStatement(String statement, List<LockInfo> lockInfoList);

    /**
     * Gives a lock finder the chance to validate a "deadlock"
     * @param first First lock
     * @param second Second lock
     * @param combination1 The first lock combination we're looking at
     * @param combination2 The second lock combination we're looking at
     * @return True if this is a deadlock
     */
    public boolean verifyDeadlock(LockInfo first, LockInfo second, List<LockInfo> combination1, List<LockInfo> combination2)
    {
        return true;
    }
}
