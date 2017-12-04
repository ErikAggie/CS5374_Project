package edu.ttu.erikpeterson.cs5381.parser.lockCheckers;

import edu.ttu.erikpeterson.cs5381.parser.block.MethodBlock;

import java.util.LinkedList;
import java.util.List;

public class LockFinderFactory {

    public static List<LockFinder> buildAllLockFinders(MethodBlock methodBlock)
    {
        List<LockFinder> lockFinders = new LinkedList<>();

        lockFinders.add(new SynchronizedLockFinder(methodBlock));
        lockFinders.add(new ReentrantLockFinder(methodBlock));
        lockFinders.add(new ReadWriteLockFinder(methodBlock));

        return lockFinders;
    }
}
