/*
    This code is created mainly by Vojtěch Horký -- https://d3s.mff.cuni.cz/people/vojtechhorky/
    Some functions added by Bohdan Kopčák
 */

import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;
import java.util.Scanner;

public class Vyrazy {
    public static class Environment {
        HashMap<Character, Integer> variables;

        public Environment() {
            variables = new HashMap<>();
        }

        public void setVariable(String key, Integer value) {
            variables.put(key.charAt(0), value);
        }

        public int getVariable(String key) {
            if (variables.containsKey(key.charAt(0))) {
                return variables.get(key.charAt(0));
            } else {
                throw new IllegalStateException("Non existent variable.");
            }
        }
    }

    public static interface Node {
        public static final String INDENT = "  ";
        public int compute();
        public String format();
        public void tree(String indent);
    }

    public static class Number implements Node {
        private final int value;

        public Number(int v) {
            value = v;
        }

        @Override
        public int compute() {
            return value;
        }

        @Override
        public String format() {
            return Integer.toString(value);
        }

        @Override
        public void tree(String indent) {
            System.out.printf("%s%d\n", indent, value);
        }
    }

    public static abstract class BinaryOp implements Node {
        private final Node left;
        private final Node right;
        private final String formatter;
        private final String name;

        public BinaryOp(Node l, Node r, String fmt, String longName) {
            left = l;
            right = r;
            formatter = fmt;
            name = longName;
        }

        protected abstract int compute(int left, int right);

        @Override
        public int compute() {
            return compute(left.compute(), right.compute());
        }

        @Override
        public String format() {
            return String.format(formatter, left.format(), right.format());
        }

        @Override
        public void tree(String indent) {
            System.out.printf("%s%s\n", indent, name);
            left.tree(indent + INDENT);
            right.tree(indent + INDENT);
        }
    }

    public static class Sum extends BinaryOp {
        public Sum(Node l, Node r) {
            super(l, r, "(%s + %s)", "PLUS");
        }

        @Override
        public int compute(int left, int right) {
            return left + right;
        }
    }

    public static class Product extends BinaryOp {
        public Product(Node l, Node r) {
            super(l, r, "%s * %s", "KRAT");
        }

        @Override
        public int compute(int left, int right) {
            return left * right;
        }
    }


    public static enum TokenType {
        NUMBER, SUM, PRODUCT, LEFT_BRACKET, RIGHT_BRACKET, EOF
    }

    public static class Token {
        private final TokenType type;
        private final int number;
        private Token(TokenType t, int num) {
            type = t;
            number = num;
        }
        public static Token makeNumber(int value) {
            return new Token(TokenType.NUMBER, value);
        }
        public static Token makeSum() {
            return new Token(TokenType.SUM, 0);
        }
        public static Token makeProduct() {
            return new Token(TokenType.PRODUCT, 0);
        }
        public static Token makeLeftBracket() {
            return new Token(TokenType.LEFT_BRACKET, 0);
        }
        public static Token makeRightBracket() {
            return new Token(TokenType.RIGHT_BRACKET, 0);
        }
        public static Token makeEof() {
            return new Token(TokenType.EOF, 0);
        }
        public TokenType getType() {
            return type;
        }
        public int getNumber() {
            if (type != TokenType.NUMBER) {
                throw new IllegalStateException("Not a number.");
            }
            return number;
        }
    }

    public static class Lexer {
        private final List<Token> tokens = new LinkedList<>();

        public Lexer(String input) {
            lexIt(input + " ");
        }

        private void lexIt(String input) {
            // Expects input terminates with blank space
            boolean insideNumber = false;
            int number = 0;
            for (char c : input.toCharArray()) {
                if ((c >= '0') && (c <= '9')) {
                    insideNumber = true;
                    number = number*10 + (c - '0');
                    continue;
                }

                if (insideNumber) {
                    tokens.add(Token.makeNumber(number));
                    number = 0;
                    insideNumber = false;
                }

                // Comments function
                if (c == '#') {
                    break;
                }

                if (c == '+') {
                    tokens.add(Token.makeSum());
                } else if (c == '*') {
                    tokens.add(Token.makeProduct());
                } else if (c == '(') {
                    tokens.add(Token.makeLeftBracket());
                } else if (c == ')') {
                    tokens.add(Token.makeRightBracket());
                } else if ((c == ' ') || (c == '\t')) {
                    // Skip.
                } else {
                    throw new IllegalArgumentException("Wrong input.");
                }
            }
            tokens.add(Token.makeEof());
        }

        public TokenType peek() {
            return tokens.get(0).getType();
        }

        public Token next() {
            return tokens.remove(0);
        }
    }

    public static class Parser {
        private final Lexer lexer;
        private Parser(Lexer lexer) {
            this.lexer = lexer;
        }

        public static Node parse(Lexer lexer) {
            Parser parser = new Parser(lexer);
            return parser.parse();
        }

        private void expect(TokenType expected) {
            TokenType actual = lexer.peek();
            if (actual != expected) {
                String message = String.format("Expected %s, got %s.",
                        expected, actual);
                System.out.println();
                throw new IllegalArgumentException(message);
            }
        }

        private Node parse() {
            Node result = expression();
            expect(TokenType.EOF);
            return result;
        }

        private Node expression() {
            Node left = factor();
            if (lexer.peek() == TokenType.SUM) {
                lexer.next();
                Node right = expression();
                return new Sum(left, right);
            } else {
                return left;
            }
        }

        private Node factor() {
            Node left = number();
            if (lexer.peek() == TokenType.PRODUCT) {
                lexer.next();
                Node right = factor();
                return new Product(left, right);
            } else {
                return left;
            }
        }

        private Node number() {
            if (lexer.peek() == TokenType.LEFT_BRACKET) {
                lexer.next();
                Node content = expression();
                expect(TokenType.RIGHT_BRACKET);
                lexer.next();
                return content;
            } else {
                expect(TokenType.NUMBER);
                Token tok = lexer.next();
                return new Number(tok.getNumber());
            }
        }
    }

    public static void run(Scanner sc, PrintStream out, PrintStream debug) {

        while (sc.hasNextLine()) {
            String input = sc.nextLine();
            Lexer lexer = new Lexer(input);
            Node ast = Parser.parse(lexer);
            int result = ast.compute();
            out.printf("'%s' => '%s' = %d\n", input, ast.format(), result);
            ast.tree("");
            if (out != debug) {
                debug.println(result);
            }
        }
        sc.close();
    }

    public static void main(String[] args) {
        run(new Scanner(System.in), System.out, System.out);
    }
}