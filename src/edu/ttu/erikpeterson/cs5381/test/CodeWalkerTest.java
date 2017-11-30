package edu.ttu.erikpeterson.cs5381.test;

import edu.ttu.erikpeterson.cs5381.parser.BlockParsingException;
import edu.ttu.erikpeterson.cs5381.parser.block.ClassBlock;
import edu.ttu.erikpeterson.cs5381.parser.block.CodeBlock;
import edu.ttu.erikpeterson.cs5381.parser.CodeBlockParser;
import edu.ttu.erikpeterson.cs5381.parser.CodeWalker;
import edu.ttu.erikpeterson.cs5381.parser.block.MethodBlock;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test the CodeWalker class (i.e. walk the code to find potential deadlocks)
 */
class CodeWalkerTest {

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
        File mainClassFile = new File(Util.TEST_CLASS_PATH + "/MainClass.java");
        List<CodeBlock> codeBlocks = null;
        try
        {
            codeBlocks = CodeBlockParser.parse(mainClassFile);
        }
        catch ( Exception e)
        {
            fail("Unable to parse file " + mainClassFile.getAbsolutePath() + ": " + e.getMessage());
        }

        assertEquals(1, codeBlocks.size());
        assertTrue(codeBlocks.get(0) instanceof ClassBlock);
        ClassBlock mainClass = ((ClassBlock)codeBlocks.get(0));
        MethodBlock mainMethod = mainClass.getMethodBlock("main");
        assertNotNull(mainMethod);

        // main() has some interior methods (the thread start blocks), so make sure they're getting cut out
        assertNotEquals(mainMethod.getThisMethodsCode().length(), mainMethod.getContents().length());

        assertTrue(mainMethod.getThisMethodsCode().indexOf("for (int i=0; i<NUM_THREADS; i++)") > 0);

        Map<String, String> mainVariables = mainMethod.getVariables();
        // This contains the "randomValue" from one of the Futures, but that doesn't cause a problem elsewhere
        assertEquals(8, mainVariables.keySet().size());

        CodeWalker walker = new CodeWalker(codeBlocks);
        assertEquals(walker.getThreadStarts().size(), 3);
        walker.walkAllThreadStarts();
    }


    @Test
    void parseSynchronizedDeadlock() throws FileNotFoundException, BlockParsingException {
        List<CodeBlock> codeBlocks = CodeBlockParser.parse(new File(Util.TEST_CLASS_PATH + "/SynchronizedDeadlock.java"));
        ClassBlock classBlock = (ClassBlock) codeBlocks.get(0);
        Map<String, String> variables = classBlock.getClassVariables();
        assertEquals(variables.get("string1"), "String");
        assertEquals(variables.get("string2"), "String");

        CodeWalker walker = new CodeWalker(codeBlocks);
        assertEquals(walker.getThreadStarts().size(), 3);

        walker.walkAllThreadStarts();

        // Now we get to find the deadlock :)
        List<String> deadlocks = walker.findDeadlocks();
        assertEquals(1, deadlocks.size());
        System.out.println("Found deadlock! " + deadlocks.get(0));
    }

}
