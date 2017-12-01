package edu.ttu.erikpeterson.cs5381.parser.lockCheckers;

import edu.ttu.erikpeterson.cs5381.parser.block.LockInfo;
import edu.ttu.erikpeterson.cs5381.parser.block.MethodBlock;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SynchronizedLockFinder extends LockFinder {

    private static final Pattern SYNCHRONIZED_PATTERN = Pattern.compile("\\s*synchronized\\s*\\(\\s*(\\w+)\\s*\\)");

    private int currentOpenParenLevel;
    private Map<Integer, LockInfo> mapOfFoundSynchronizedBlocks = new HashMap<>();

    SynchronizedLockFinder(MethodBlock methodBlock)
    {
        super(methodBlock);
    }

    @Override
    public void checkStatement(String statement, List<LockInfo> lockInfoList)
    {
        Matcher synchronizedMatcher = SYNCHRONIZED_PATTERN.matcher(statement);
        if ( synchronizedMatcher.find())
        {
            // Found a synchronized block. See if we can find the variable's type
            String variable = synchronizedMatcher.group(1);
            if ( variable.isEmpty())
            {
                System.out.println("Found empty variable in " + statement);
                return;
            }

            String type;

            if ( variable.equals("this"))
            {
                type = methodBlock.getClassParent().getName();
            }
            else if ( variables.containsKey(variable))
            {
                type = variables.get(variable);
            }
            else
            {
                // Don't know what this is
                System.err.println("Locking unknown variable " + variable + " in " + methodBlock.getClassAndName());
                return;
            }

            LockInfo lockInfo = new LockInfo(variable, type, methodBlock.getClassAndName(), true);

            // When we find a '}' that gets us back to this level we'll mark this unlocked
            mapOfFoundSynchronizedBlocks.put(currentOpenParenLevel, lockInfo);
            lockInfoList.add(lockInfo);
        }

        // Check to see if there's an '{' or '}' (there will be at most one of them
        if ( statement.contains("{"))
        {
            currentOpenParenLevel++;
        }
        else if ( statement.contains("}"))
        {
            currentOpenParenLevel--;
            // Check to see if we just closed a synchronized block
            if ( mapOfFoundSynchronizedBlocks.containsKey(currentOpenParenLevel))
            {
                LockInfo lockJustUnlocked = mapOfFoundSynchronizedBlocks.remove(currentOpenParenLevel);
                lockInfoList.add(new LockInfo(lockJustUnlocked.getName(), lockJustUnlocked.getType(), methodBlock.getClassAndName(), false));
            }
        }

    }
}
