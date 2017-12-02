package edu.ttu.erikpeterson.cs5381.test;

import edu.ttu.erikpeterson.cs5381.parser.block.ClassBlock;
import edu.ttu.erikpeterson.cs5381.parser.block.CodeBlock;
import edu.ttu.erikpeterson.cs5381.parser.CodeBlockParser;
import edu.ttu.erikpeterson.cs5381.parser.block.CodeBlockType;
import edu.ttu.erikpeterson.cs5381.parser.CodeWalker;
import edu.ttu.erikpeterson.cs5381.parser.block.MethodBlock;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CodeBlockParserTest {

    @Test
    void parseBasicFile() {
        File basicClass = new File(Util.TEST_CLASS_PATH + "/BasicClass.java");
        List<CodeBlock> codeBlocks = null;
        try
        {
            codeBlocks = CodeBlockParser.parse(basicClass);
        }
        catch ( Exception e)
        {
            e.printStackTrace();
            fail("Unable to parse file " + basicClass.getAbsolutePath() + ": " + e.getMessage());
        }

        assertEquals(codeBlocks.size(), 1);
        CodeBlock classBlockLikely = codeBlocks.get(0);
        assertTrue(classBlockLikely instanceof ClassBlock);

        ClassBlock classBlock = (ClassBlock) classBlockLikely;
        assertEquals(classBlock.getBlockInfo().trim(), "public class BasicClass");
        assertEquals(classBlock.getBlockType(), CodeBlockType.CLASS);
        assertEquals(classBlock.getName(), "BasicClass");
        assertTrue(classBlock.getContents().contains("for ( int i=0; i<10; i++"));
        assertNotNull(classBlock.getMethodBlock("method1"));
        assertNotNull(classBlock.getMethodBlock("method2"));
        assertEquals(classBlock.getSubCodeBlock(0).getName(), "BasicClass");
        assertEquals(classBlock.getSubCodeBlock(1).getName(), "method1");
        assertEquals(classBlock.getSubCodeBlock(2).getName(), "method2");

        // Check for string literal
        assertTrue(!classBlock.getContents().contains("This is "));
    }

    @Test
    void mainFile() {
        File mainClass = new File(Util.TEST_CLASS_PATH + "/MainClass.java");
        List<CodeBlock> codeBlocks = null;
        try
        {
            codeBlocks = CodeBlockParser.parse(mainClass);
        }
        catch ( Exception e)
        {
            e.printStackTrace();
            fail("Unable to parse file " + mainClass.getAbsolutePath() + ": " + e.getMessage());
        }

        assertEquals(codeBlocks.size(), 1);

        ClassBlock classBlock = (ClassBlock) codeBlocks.get(0);
        Map<String, String> classVariables = classBlock.getClassVariables();
        assertEquals("String", classVariables.get("MY_STRING"));
        assertEquals("int", classVariables.get("NUM_THREADS"));

        CodeBlock mainCodeBlock = classBlock.getSubCodeBlock(0);
        assertNotNull(mainCodeBlock);
        assertTrue(mainCodeBlock instanceof MethodBlock);
        assertEquals(mainCodeBlock.getParent(), codeBlocks.get(0));
        assertTrue(mainCodeBlock.getBlockInfo().contains("public static void main("));
        assertEquals(mainCodeBlock.getBlockType(), CodeBlockType.THREAD_ENTRY);

        // First for loop
        assertTrue(mainCodeBlock.getSubCodeBlock(0).getBlockInfo().contains("i++"));

        // 2nd for loop
        assertTrue(mainCodeBlock.getSubCodeBlock(1).getContents().contains("total"));
        assertTrue(mainCodeBlock.getSubCodeBlock(1).getBlockType() == CodeBlockType.CODE_BLOCK);

        // Make sure the try & catch are marked as code blocks
        assertTrue(mainCodeBlock.getSubCodeBlock(1).getSubCodeBlock(0).getBlockType() == CodeBlockType.CODE_BLOCK);
        assertTrue(mainCodeBlock.getSubCodeBlock(1).getSubCodeBlock(1).getBlockType() == CodeBlockType.CODE_BLOCK);

        assertTrue(mainCodeBlock.getSubCodeBlock(2).getBlockType() == CodeBlockType.SYNCHRONIZED);

        CodeWalker codeWalker = new CodeWalker(codeBlocks);
        List<MethodBlock> threadStartBlocks = codeWalker.getThreadStarts();
        assertEquals(threadStartBlocks.size(), 3);
        assertTrue(threadStartBlocks.get(2).getBlockInfo().contains("methodFuture = "));

        // Check for comments (should be removed)
        assertTrue(!classBlock.getContents().contains("Main method :)"));
        assertTrue(!classBlock.getContents().contains("Normally this wouldn't be"));

    }

    @Test
    void parseAllFiles()
    {
        File testDirectory = new File(Util.TEST_CLASS_PATH);
        List<CodeBlock> codeBlocks = null;
        try
        {
            codeBlocks = CodeBlockParser.parsePath(testDirectory);
        }
        catch ( Exception e)
        {
            e.printStackTrace();
            fail("Unable to parse directory " + testDirectory.getAbsolutePath() + ": " + e.getMessage());
        }

        assertEquals(codeBlocks.size(), 4);
    }
}
