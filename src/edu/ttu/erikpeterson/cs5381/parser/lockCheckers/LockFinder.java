package edu.ttu.erikpeterson.cs5381.parser.lockCheckers;

import edu.ttu.erikpeterson.cs5381.parser.block.LockInfo;

import java.util.List;
import java.util.Map;

public interface LockFinder {
    void checkStatement(String statement, List<LockInfo> lockInfoList);
}
