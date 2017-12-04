package edu.ttu.erikpeterson.cs5381.parser.lockCheckers;

import edu.ttu.erikpeterson.cs5381.parser.block.LockInfo;
import edu.ttu.erikpeterson.cs5381.parser.block.MethodBlock;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Finder for ReadWriteLocks. We'll use a little trickery to handle the read/write lock split.
 */
public class ReadWriteLockFinder extends LockFinder {

    private static final Pattern READ_LOCK_MATCHER = Pattern.compile("\\s*(\\w+)\\s*\\.\\s*readLock\\s*\\(\\s*\\)\\s*.\\s*lock\\s*\\(");
    private static final Pattern WRITE_LOCK_MATCHER = Pattern.compile("\\s*(\\w+)\\s*\\.\\s*writeLock\\s*\\(\\s*\\)\\s*.\\s*lock\\s*\\(");
    private static final Pattern READ_UNLOCK_MATCHER = Pattern.compile("\\s*(\\w+)\\s*\\.\\s*readLock\\s*\\(\\s*\\)\\s*.\\s*unlock\\s*\\(");
    private static final Pattern WRITE_UNLOCK_MATCHER = Pattern.compile("\\s*(\\w+)\\s*\\.\\s*writeLock\\s*\\(\\s*\\)\\s*.\\s*unlock\\s*\\(");

    private static final String READ_EXTENSION = "_readLock()";
    private static final String WRITE_EXTENSION = "_writeLock()";

    private enum LockType
    {
        READ_LOCK,
        WRITE_LOCK,
        READ_UNLOCK,
        WRITE_UNLOCK
    }

    /**
     * Constructor
     *
     * @param methodBlock Method we're looking at
     */
    ReadWriteLockFinder(MethodBlock methodBlock)
    {
        super(methodBlock);
    }

    @Override
    public void checkStatement(String statement, List<LockInfo> lockInfoList) {

        Matcher readLockMatcher = READ_LOCK_MATCHER.matcher(statement);
        Matcher writeLockMatcher = WRITE_LOCK_MATCHER.matcher(statement);
        Matcher readUnlockMaster = READ_UNLOCK_MATCHER.matcher(statement);
        Matcher writeUnlockMaster = WRITE_UNLOCK_MATCHER.matcher(statement);

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
        else if ( readUnlockMaster.find())
        {
            variable = readUnlockMaster.group(1);
            lockType = LockType.READ_UNLOCK;
        }
        else if ( writeUnlockMaster.find())
        {
            variable = writeUnlockMaster.group(1);
            lockType = LockType.WRITE_UNLOCK;
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
                        lockInfoList.add(new LockInfo(variable + READ_EXTENSION, type, methodBlock, true));
                        break;
                    case WRITE_LOCK:
                        // Treat this as a combination read and write lock so a read lock elsewhere will get flagged
                        lockInfoList.add(new LockInfo(variable + READ_EXTENSION, type, methodBlock, true));
                        lockInfoList.add(new LockInfo(variable + WRITE_EXTENSION, type, methodBlock, true));
                        break;
                    case READ_UNLOCK:
                        lockInfoList.add(new LockInfo(variable + READ_EXTENSION, type, methodBlock, false));
                        break;
                    case WRITE_UNLOCK:
                        lockInfoList.add(new LockInfo(variable + READ_EXTENSION, type, methodBlock, false));
                        lockInfoList.add(new LockInfo(variable + WRITE_EXTENSION, type, methodBlock, false));
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown lock type " + lockType);
                }
            }
        }
    }

    @Override
    public boolean verifyDeadlock(LockInfo first, LockInfo second, List<LockInfo> combination1, List<LockInfo> combination2)
    {
        String firstLockName = first.getName();
        String secondLockName = second.getName();
        // If they're both readLocks(), this isn't a deadlock
        if ( firstLockName.endsWith(READ_EXTENSION) &&
                secondLockName.endsWith(READ_EXTENSION))
        {
            // We have a POTENTIAL false positive. What we need to make sure of is that there's not one (or both) _writeLock()
            // in the first list and the other (or both again) in the second list
            String firstWriteLock = firstLockName.substring(0, firstLockName.length()-READ_EXTENSION.length()) + WRITE_EXTENSION;
            String secondWriteLock = secondLockName.substring(0, secondLockName.length()-READ_EXTENSION.length()) + WRITE_EXTENSION;

            boolean foundFirstInFirstCombo = false;
            boolean foundSecondInFirstCombo = false;
            for ( LockInfo lockInfo : combination1)
            {
                if ( lockInfo.getName().equals(firstWriteLock))
                {
                    foundFirstInFirstCombo = true;
                }
                else if ( lockInfo.getName().equals(secondWriteLock))
                {
                    foundSecondInFirstCombo = true;
                }
            }

            boolean foundFirstInSecondCombo = false;
            boolean foundSecondInSecondCombo = false;
            for ( LockInfo lockInfo : combination2)
            {
                if ( lockInfo.getName().equals(firstWriteLock))
                {
                    foundFirstInSecondCombo = true;
                }
                else if ( lockInfo.getName().equals(secondWriteLock))
                {
                    foundSecondInSecondCombo = true;
                }
            }

            // This is a potential deadlock if each combo contains at least one writeLock(),
            // with both of the covered between the two combos
            // i.e. thread1 has variable1.writeLock() and thread2 has variable2.writeLock()
            // (and, because we're in thie position, they both have read locks on the other
            // lock)
            return ((foundFirstInFirstCombo && foundSecondInSecondCombo) ||
                    (foundSecondInFirstCombo && foundFirstInSecondCombo));
        }
        return true;
    }
}
