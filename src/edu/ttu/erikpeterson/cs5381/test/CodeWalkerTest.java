package edu.ttu.erikpeterson.cs5381.test;

import edu.ttu.erikpeterson.cs5381.parser.BlockParsingException;
import edu.ttu.erikpeterson.cs5381.parser.CodeBlock;
import edu.ttu.erikpeterson.cs5381.parser.CodeBlockParser;
import edu.ttu.erikpeterson.cs5381.parser.CodeWalker;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class CodeWalkerTest {

    @Test
    void walkBasicClass()
    {
        File basicClass = new File(Util.TEST_CLASS_PATH + "/BasicClass.java");
        List<CodeBlock> codeBlocks = null;
        try
        {
            codeBlocks = CodeBlockParser.parse(basicClass);
        } catch ( Exception e)
        {
            fail("Unable to parse file " + basicClass.getAbsolutePath() + ": " + e.getMessage());
        }

        CodeWalker walker = new CodeWalker(codeBlocks);
        walker.walkAllThreadStarts();
        assertEquals(walker.getThreadStarts().size(), 0);
    }

    @Test
    void walkMainClass()
    {
        File mainClass = new File(Util.TEST_CLASS_PATH + "/MainClass.java");
        List<CodeBlock> codeBlocks = null;
        try
        {
            codeBlocks = CodeBlockParser.parse(mainClass);
        }
        catch ( Exception e)
        {
            fail("Unable to parse file " + mainClass.getAbsolutePath() + ": " + e.getMessage());
        }

        CodeWalker walker = new CodeWalker(codeBlocks);
        assertEquals(walker.getThreadStarts().size(), 3);
        walker.walkAllThreadStarts();
    }


    @Test
    void parseSynchronizedDeadlock() throws FileNotFoundException, BlockParsingException {
        List<CodeBlock> codeBlocks = CodeBlockParser.parse(new File(Util.TEST_CLASS_PATH + "/SynchronizedDeadlock.java"));

        CodeWalker walker = new CodeWalker(codeBlocks);
        assertEquals(walker.getThreadStarts().size(), 2);

        walker.walkAllThreadStarts();
        // TODO: get the deadlocks...
    }

}
