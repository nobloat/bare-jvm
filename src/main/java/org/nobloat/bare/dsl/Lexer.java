package org.nobloat.bare.dsl;

import java.io.IOException;

public class Lexer {

    private final Scanner scanner;

    public Lexer(Scanner scanner) {
        this.scanner = scanner;
    }

    public Token nextToken() throws IOException {
        int ch;

        do {
            ch = scanner.readChar();
        } while (Character.isSpaceChar((char) ch) || scanner.isNewLine((char) ch));

        if (ch == -1) {
            return new Token(Token.Type.EOF);
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
            return new Token(Token.Type.NUMBER, integer);
        }

        switch (character) {
            case '#':
                String comment = scanner.readAllUntil('\n');
                return new Token(Token.Type.COMMENT, comment);
            case '<':
                return new Token(Token.Type.L_ANGLE);
            case '>':
                return new Token(Token.Type.R_ANGLE);
            case '{':
                return new Token(Token.Type.L_BRACE);
            case '}':
                return new Token(Token.Type.R_BRACE);
            case '[':
                return new Token(Token.Type.L_BRACKET);
            case ']':
                return new Token(Token.Type.R_BRACKET);
            case '(':
                return new Token(Token.Type.L_PAREN);
            case ')':
                return new Token(Token.Type.R_PAREN);
            case '|':
                return new Token(Token.Type.PIPE);
            case '=':
                return new Token(Token.Type.EQUAL);
            case ':':
                return new Token(Token.Type.COLON);
        }

        return new Token(Token.Type.UNKNOWN);
    }

    private Token tokenOfWord(String word) {

        switch (word) {
            case "type":
                return new Token(Token.Type.TYPE);
            case "enum":
                return new Token(Token.Type.ENUM);
            case "uint":
                return new Token(Token.Type.UINT);
            case "u8":
                return new Token(Token.Type.U8);
            case "u16":
                return new Token(Token.Type.U16);
            case "u32":
                return new Token(Token.Type.U32);
            case "u64":
                return new Token(Token.Type.U64);
            case "int":
                return new Token(Token.Type.INT);
            case "i8":
                return new Token(Token.Type.I8);
            case "i16":
                return new Token(Token.Type.I16);
            case "i32":
                return new Token(Token.Type.I32);
            case "i64":
                return new Token(Token.Type.I64);
            case "f32":
                return new Token(Token.Type.F32);
            case "f64":
                return new Token(Token.Type.F64);
            case "bool":
                return new Token(Token.Type.BOOL);
            case "string":
                return new Token(Token.Type.STRING);
            case "data":
                return new Token(Token.Type.DATA);
            case "void":
                return new Token(Token.Type.VOID);
            case "optional":
                return new Token(Token.Type.OPTIONAL);
            case "map":
                return new Token(Token.Type.MAP);
        }

        return new Token(Token.Type.NAME, word);
    }

    public static class Token {

        final Type type;
        final String value;

        public Token(Type type) {
            this(type, null);
        }

        public Token(Type type, String value) {
            this.type = type;
            this.value = value;
        }

        enum Type {
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
