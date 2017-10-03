package edu.ttu.erikpeterson.cs5381.parser;

/**
 * For when there's a problem parsing a code block
 */
public class BlockParsingException extends Exception {

    /**
     * Constructor with error message
     *
     * @param message What happened
     */
    public BlockParsingException(String message)
    {
        super(message);
    }
}
