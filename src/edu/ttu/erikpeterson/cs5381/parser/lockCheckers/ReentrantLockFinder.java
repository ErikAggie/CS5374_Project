package edu.ttu.erikpeterson.cs5381.parser.lockCheckers;

import edu.ttu.erikpeterson.cs5381.parser.block.LockInfo;
import edu.ttu.erikpeterson.cs5381.parser.block.MethodBlock;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReentrantLockFinder extends LockFinder {

    private static final Pattern TRYLOCK_MATCHER = Pattern.compile("\\s*(\\w+)\\s*\\.\\s*tryLock\\s*\\(");
    private static final Pattern LOCK_MATCHER = Pattern.compile("\\s*(\\w+)\\s*\\.\\s*lock\\s*\\(");
    private static final Pattern UNLOCK_MATCHER = Pattern.compile("\\s*(\\w+)\\s*\\.\\s*unlock\\s*\\(");

    ReentrantLockFinder(MethodBlock methodBlock)
    {
        super(methodBlock);
    }

    @Override
    public void checkStatement(String statement, List<LockInfo> lockInfoList) {
        Matcher trylockMatcher = TRYLOCK_MATCHER.matcher(statement);
        Matcher lockMatcher = LOCK_MATCHER.matcher(statement);
        Matcher unlockMatcher = UNLOCK_MATCHER.matcher(statement);

        String variable;
        boolean lock;
        if ( trylockMatcher.find())
        {
            variable = trylockMatcher.group(1);
            lock = true;
        }
        else if ( lockMatcher.find())
        {
            variable = lockMatcher.group(1);
            lock = true;
        }
        else if ( unlockMatcher.find())
        {
            variable = unlockMatcher.group(1);
            lock = false;
        }
        else
        {
            // No match
            return;
        }

        if ( variables.containsKey(variable))
        {
            String type = variables.get(variable);
            if ( type.equals("ReentrantLock"))
            {
                lockInfoList.add(new LockInfo(variable, type, methodBlock, lock));
            }
        }
    }
}
