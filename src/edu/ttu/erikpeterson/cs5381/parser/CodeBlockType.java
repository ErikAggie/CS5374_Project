package edu.ttu.erikpeterson.cs5381.parser;

/**
 * Represents the types of code blocks we care about
 */
public enum CodeBlockType {
    /**
     * A class block, which presumably contains methods
     */
    CLASS,
    /**
     * A specialized method for a thread entry point, i.e. a Runnable's run() method,
     * a Callable's call() method, or main()
     */
    THREAD_ENTRY,
    /**
     * A method block, which might contain one or more internal blocks
     */
    METHOD,
    /**
     * A synchronized block
     */
    SYNCHRONIZED,
    /**
     * Something that's just code (i.e. we can scan it without calling it out separately
     */
    CODE_BLOCK
}
