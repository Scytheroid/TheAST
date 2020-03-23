import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;

import static org.junit.Assert.*;

public class VyrazyTest {

    // Simple input parsing
    private int compute(String input) {
        Vyrazy.Lexer lexer = new Vyrazy.Lexer(input);
        Vyrazy.Node ast = Vyrazy.Parser.parse(lexer);
        return ast.compute();
    }

    // Declarations necessary for proper system.in testing accesible at
    // Source: https://stackoverflow.com/a/50721326

    @Test
    public void addition() {
        assertEquals( 9, compute("4+5"));
    }

    @Test
    public void multiplication() {
        assertEquals( 12, compute("3*4"));
    }

    @Test
    public void additionAndMultiplication() {
        assertEquals(17, compute("3*4+5"));
        assertEquals(26, compute("5*4+6"));
    }

    @Test
    public void brackets() {
        assertEquals(30, compute("(3+3)*5"));
        assertEquals(14, compute("(3*3)+5"));
        assertEquals(25, compute("(3+2)*(4+1)"));
        assertEquals(10, compute("(3+2)+(4+1)"));
    }

    @Test
    public void submergedBrackets() {
        assertEquals(32, compute("((3+2)*3+1)*2"));
    }
}