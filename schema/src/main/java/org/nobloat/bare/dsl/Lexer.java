package org.nobloat.bare.dsl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Lexer {

    private final Scanner scanner;
    private final List<Token> pushbackTokens;

    public Lexer(Scanner scanner) {
        this.scanner = scanner;
        this.pushbackTokens = new ArrayList<>();
    }

    public Token nextToken() throws IOException {
        if (pushbackTokens.size() > 0) {
            return pushbackTokens.remove(0);
        }

        int ch;

        do {
            ch = scanner.readChar();
        } while (Character.isSpaceChar((char) ch) || scanner.isNewLine((char) ch));

        if (ch == -1) {
            return new Token(Token.Type.EOF, "EOF", scanner.getLineNumber(), scanner.getColumn());
        }

        char character = (char) ch;

        if (Character.isLetter(character)) {
            scanner.unreadChar();
            String word = scanner.readWord();
            return tokenOfWord(word);
        }

        if (Character.isDigit(character)) {
            scanner.unreadChar();
            String integer = scanner.readInteger();
            return new Token(Token.Type.NUMBER, integer, scanner.getLineNumber(), scanner.getColumn());
        }

        switch (character) {
            case '#':
                String comment = scanner.readAllUntil('\n');
//                return new Token(Token.Type.COMMENT, comment);
                return nextToken();
            case '<':
                return new Token(Token.Type.L_ANGLE, character, scanner.getLineNumber(), scanner.getColumn());
            case '>':
                return new Token(Token.Type.R_ANGLE, character, scanner.getLineNumber(), scanner.getColumn());
            case '{':
                return new Token(Token.Type.L_BRACE, character, scanner.getLineNumber(), scanner.getColumn());
            case '}':
                return new Token(Token.Type.R_BRACE, character, scanner.getLineNumber(), scanner.getColumn());
            case '[':
                return new Token(Token.Type.L_BRACKET, character, scanner.getLineNumber(), scanner.getColumn());
            case ']':
                return new Token(Token.Type.R_BRACKET, character, scanner.getLineNumber(), scanner.getColumn());
            case '(':
                return new Token(Token.Type.L_PAREN, character, scanner.getLineNumber(), scanner.getColumn());
            case ')':
                return new Token(Token.Type.R_PAREN, character, scanner.getLineNumber(), scanner.getColumn());
            case '|':
                return new Token(Token.Type.PIPE, character, scanner.getLineNumber(), scanner.getColumn());
            case '=':
                return new Token(Token.Type.EQUAL, character, scanner.getLineNumber(), scanner.getColumn());
            case ':':
                return new Token(Token.Type.COLON, character, scanner.getLineNumber(), scanner.getColumn());
        }

        return new Token(Token.Type.UNKNOWN, character, scanner.getLineNumber(), scanner.getColumn());
    }

    public void pushback(Token token) {
        pushbackTokens.add(token);
    }

    private Token tokenOfWord(String word) {

        switch (word) {
            case "type":
                return new Token(Token.Type.TYPE, word, scanner.getLineNumber(), scanner.getColumn());
            case "enum":
                return new Token(Token.Type.ENUM, word, scanner.getLineNumber(), scanner.getColumn());
            case "uint":
                return new Token(Token.Type.UINT, word, scanner.getLineNumber(), scanner.getColumn());
            case "u8":
                return new Token(Token.Type.U8, word, scanner.getLineNumber(), scanner.getColumn());
            case "u16":
                return new Token(Token.Type.U16, word, scanner.getLineNumber(), scanner.getColumn());
            case "u32":
                return new Token(Token.Type.U32, word, scanner.getLineNumber(), scanner.getColumn());
            case "u64":
                return new Token(Token.Type.U64, word, scanner.getLineNumber(), scanner.getColumn());
            case "int":
                return new Token(Token.Type.INT, word, scanner.getLineNumber(), scanner.getColumn());
            case "i8":
                return new Token(Token.Type.I8, word, scanner.getLineNumber(), scanner.getColumn());
            case "i16":
                return new Token(Token.Type.I16, word, scanner.getLineNumber(), scanner.getColumn());
            case "i32":
                return new Token(Token.Type.I32, word, scanner.getLineNumber(), scanner.getColumn());
            case "i64":
                return new Token(Token.Type.I64, word, scanner.getLineNumber(), scanner.getColumn());
            case "f32":
                return new Token(Token.Type.F32, word, scanner.getLineNumber(), scanner.getColumn());
            case "f64":
                return new Token(Token.Type.F64, word, scanner.getLineNumber(), scanner.getColumn());
            case "bool":
                return new Token(Token.Type.BOOL, word, scanner.getLineNumber(), scanner.getColumn());
            case "string":
                return new Token(Token.Type.STRING, word, scanner.getLineNumber(), scanner.getColumn());
            case "data":
                return new Token(Token.Type.DATA, word, scanner.getLineNumber(), scanner.getColumn());
            case "void":
                return new Token(Token.Type.VOID, word, scanner.getLineNumber(), scanner.getColumn());
            case "optional":
                return new Token(Token.Type.OPTIONAL, word, scanner.getLineNumber(), scanner.getColumn());
            case "map":
                return new Token(Token.Type.MAP, word, scanner.getLineNumber(), scanner.getColumn());
        }

        return new Token(Token.Type.NAME, word, scanner.getLineNumber(), scanner.getColumn());
    }

    public static class Token {

        final Type type;
        final String value;
        final int lineNumber;
        final int column;

        public Token(Type type, char value, int lineNumber, int column) {
            this(type, Character.toString(value), lineNumber, column);
        }

        public Token(Type type, String value, int lineNumber, int column) {
            this.type = type;
            this.value = value;
            this.lineNumber = lineNumber;
            this.column = column;
        }

        public enum Type {
            NUMBER,
            L_ANGLE, R_ANGLE,
            L_BRACE, R_BRACE,
            L_BRACKET, R_BRACKET,
            L_PAREN, R_PAREN,
            PIPE,
            EQUAL,
            COLON,
            UNKNOWN,
            EOF,
            COMMENT,
            TYPE,
            ENUM,
            UINT,
            U8, U16, U32, U64,
            INT,
            I8, I16, I32, I64,
            F32, F64,
            BOOL,
            STRING,
            DATA,
            VOID,
            OPTIONAL,
            MAP,
            NAME
        }

    }
}
