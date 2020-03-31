import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import static org.junit.Assert.assertEquals;

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

    @Test
    public void standardInput() throws UnsupportedEncodingException {
        String input = "1 + 2\n";
        final ByteArrayOutputStream baos_out = new ByteArrayOutputStream();
        final ByteArrayOutputStream baos_debug = new ByteArrayOutputStream();
        final String utf8 = StandardCharsets.UTF_8.name();
        PrintStream out = new PrintStream(baos_out, true, utf8);
        PrintStream debug = new PrintStream(baos_debug, true, utf8);

        Vyrazy.run(new Scanner(input), out, debug);

        assertEquals(baos_debug.toString().trim().replace("\r",""), "3");


    }

    @Test
    public void name() {
    }
}