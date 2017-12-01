package edu.ttu.erikpeterson.cs5381.parser;

import edu.ttu.erikpeterson.cs5381.parser.block.CodeBlock;
import edu.ttu.erikpeterson.cs5381.parser.block.CodeBlockType;
import edu.ttu.erikpeterson.cs5381.parser.block.LockInfo;
import edu.ttu.erikpeterson.cs5381.parser.block.MethodBlock;

import java.util.*;

public class CodeWalker {
    private final List<CodeBlock> codeBlockList;
    private final List<MethodBlock> threadStarts = new ArrayList<>();
    private Map<MethodBlock, List<LockInfo>> allLockInfo = new HashMap<>();

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
        for ( MethodBlock threadStart : threadStarts)
        {
            allLockInfo.put(threadStart, walkThread(threadStart));
        }
    }

    /**
     * Walk thorugh a specific thread
     * @param thread The code block to walk
     */
    private List<LockInfo> walkThread(MethodBlock thread)
    {
        List<LockInfo> lockInfo = new ArrayList<>();
        thread.walkMethod(codeBlockList, lockInfo);

        return lockInfo;
    }

    /**
     * Scan for and return deadlocks
     * @return Info on the potential deadlocks, if any
     */
    public List<String> findDeadlocks()
    {
        List<String> deadlockInfo = new ArrayList<>();

        List<List<LockInfo>> allLockCombinations = new ArrayList<>();

        for ( MethodBlock threadStart : allLockInfo.keySet())
        {
            List<LockInfo> thisThreadsLockInfo = allLockInfo.get(threadStart);
            if ( thisThreadsLockInfo.isEmpty())
            {
                continue;
            }

            List<LockInfo> lockCombination = new ArrayList<>();

            for ( LockInfo info : thisThreadsLockInfo)
            {
                if ( info.isLock())
                {
                    lockCombination.add(info);
                }
                else
                {
                    // Is an unlock. If there's more than one in this combination, we need to check it
                    // before removing it from the "current" list
                    if ( lockCombination.size() > 1)
                    {
                        checkLockCombination(allLockCombinations, lockCombination, deadlockInfo);

                        // Save this lock combination for later
                        // Have to copy it since this list will change!
                        List<LockInfo> thisCombination = new ArrayList<>();
                        thisCombination.addAll(lockCombination);
                        allLockCombinations.add(thisCombination);
                    }
                    // Now remove the matching lock
                    lockCombination.remove(new LockInfo(info.getName(), info.getType(), info.getWhereFound(), true));
                }
            }

            // Handle the last lock combination, if any
            if ( lockCombination.size() > 1)
            {
                checkLockCombination(allLockCombinations, lockCombination, deadlockInfo);
                allLockCombinations.add(lockCombination);
            }

        }

        return deadlockInfo;

    }

    private void checkLockCombination(List<List<LockInfo>> existingCombinations, List<LockInfo> currentCombinations, List<String> deadlockInfo)
    {
        if ( existingCombinations.isEmpty())
        {
            return;
        }

        // We need to check the other lock combinations to see if out particular combination has any elements
        // in a different order elsewhere
        //
        // We'll find it by checking the positions of our locks against the positions of the existing combinations.
        // If
        for ( List<LockInfo> existingCombination : existingCombinations)
        {
            int lastPosition = 0;
            List<Integer> positions = new ArrayList<>();
            for ( LockInfo ourLockInfo : currentCombinations)
            {
                int position = existingCombination.indexOf(ourLockInfo);
                if ( position < 0)
                {
                    // To make the length of our index list match the other one...and to keep the later
                    // check from freaking out over a missing lock, reuse the last position
                    positions.add(lastPosition);
                }
                else
                {
                    // Found this lock in the other combination
                    positions.add(position);
                    lastPosition = position;
                }
            }

            // Now let's see if there's a different order
            lastPosition = 0;
            // Keep track of the last lock we actually found. A lock that is not found in the other list will share the index
            // of the previous lock.
            int lastChangeIndex = 0;
            for ( int i=0; i<positions.size(); i++)
            {
                int currentPosition = positions.get(i);
                if ( positions.get(i) < lastPosition)
                {
                    // Here's a potential deadlock!
                    deadlockInfo.add("Potential deadlock between variables " +
                                     currentCombinations.get(lastChangeIndex) + " and " +
                                     currentCombinations.get(i));
                    lastChangeIndex = i;
                }
                else if ( positions.get(i) > lastPosition)
                {
                    lastChangeIndex = i;
                }
                lastPosition = currentPosition;
            }
        }
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
