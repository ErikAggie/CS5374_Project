package edu.ttu.erikpeterson.cs5381.parser.lockCheckers;

import edu.ttu.erikpeterson.cs5381.parser.block.MethodBlock;

import java.util.LinkedList;
import java.util.List;

public class LockFinderFactory {

    public static List<LockFinder> buildAllLockFinders(MethodBlock block)
    {
        List<LockFinder> lockFinders = new LinkedList<>();

        lockFinders.add(new SynchronizedLockFinder(block.getVariables()));

        return lockFinders;
    }
}
