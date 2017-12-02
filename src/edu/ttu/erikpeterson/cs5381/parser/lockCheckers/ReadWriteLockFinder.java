package edu.ttu.erikpeterson.cs5381.parser.lockCheckers;

import edu.ttu.erikpeterson.cs5381.parser.block.LockInfo;
import edu.ttu.erikpeterson.cs5381.parser.block.MethodBlock;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Finder for ReadWriteLocks. We'll use a little trickery to handle the read/write lock split.
 */
public class ReadWriteLockFinder extends LockFinder {

    // TODO: Add readLock().lock(), writeLock().lock(), readLock().unlock(), writeLock().unlock()
    private static final Pattern READ_LOCK_MATCHER = Pattern.compile("\\s*(\\w+)\\s*\\.\\s*readLock\\s*\\(");
    private static final Pattern WRITE_LOCK_MATCHER = Pattern.compile("\\s*(\\w+)\\s*\\.\\s*writeLock\\s*\\(");
    private static final Pattern UNLOCK_MATCHER = Pattern.compile("\\s*(\\w+)\\s*\\.\\s*unlock\\s*\\(");

    private static enum LockType
    {
        READ_LOCK,
        WRITE_LOCK,
        UNLOCK;
    }

    /**
     * Constructor
     *
     * @param methodBlock Method we're looking at
     */
    public ReadWriteLockFinder(MethodBlock methodBlock)
    {
        super(methodBlock);
    }

    @Override
    public void checkStatement(String statement, List<LockInfo> lockInfoList) {

        Matcher readLockMatcher = READ_LOCK_MATCHER.matcher(statement);
        Matcher writeLockMatcher = WRITE_LOCK_MATCHER.matcher(statement);
        Matcher unlockMaster = UNLOCK_MATCHER.matcher(statement);

        String variable;
        LockType lockType;

        if ( readLockMatcher.find())
        {
            variable = readLockMatcher.group(1);
            lockType = LockType.READ_LOCK;
        }
        else if ( writeLockMatcher.find())
        {
            variable = writeLockMatcher.group(1);
            lockType = LockType.WRITE_LOCK;
        }
        else if ( unlockMaster.find())
        {
            variable = unlockMaster.group(1);
            lockType = LockType.UNLOCK;
        }
        else
        {
            // No match
            return;
        }

        if ( variables.containsKey(variable))
        {
            String type = variables.get(variable);
            // Handle both the interface "ReadWriteLock" and the known implementation "ReentrantReadWriteLock"
            if ( type.equals("ReadWriteLock") ||
                 type.equals("ReentrantReadWriteLock"))
            {
                switch(lockType)
                {
                    // Use _read and _write to show the kind of locks we're getting/releasing
                    case READ_LOCK:
                        lockInfoList.add(new LockInfo(variable + "_read", type, methodBlock, true));
                        break;
                    case WRITE_LOCK:
                        lockInfoList.add(new LockInfo(variable + "_read", type, methodBlock, true));
                        lockInfoList.add(new LockInfo(variable + "_write", type, methodBlock, true));
                        break;
                    case UNLOCK:
                        lockInfoList.add(new LockInfo(variable + "_read", type, methodBlock, false));
                        lockInfoList.add(new LockInfo(variable + "_write", type, methodBlock, false));
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown lock type " + lockType);
                }
            }
        }
    }
}
