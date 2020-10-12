package org.nobloat.bare.dsl;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

public class Scanner implements AutoCloseable {

    private final BufferedInputStream reader;

    public Scanner(InputStream is) {
        reader = new BufferedInputStream(is, 2);
    }

    public int readChar() throws IOException {
        reader.mark(1);
        return reader.read();
    }

    public void unreadChar() throws IOException {
        reader.reset();
    }

    public String readInteger() throws IOException {
        StringBuilder builder = new StringBuilder();

        while (true) {
            int ch = readChar();

            if (ch == -1) {
                break;
            }

            char character = (char) ch;
            if (!isDigit(character)) {
                unreadChar();
                break;
            }

            builder.append(character);
        }

        if(builder.length() == 0) {
            throw new IOException("Is not an integer");
        }

        return builder.toString();
    }

    public String readWord() throws IOException {
        StringBuilder builder = new StringBuilder();

        while (true) {
            int ch = readChar();

            if (ch == -1) {
                break;
            }

            char character = (char) ch;
            if (!isLetterOrDigit(character)) {
                unreadChar();
                break;
            }

            builder.append(character);
        }

        if(builder.length() == 0) {
            throw new IOException("Is not a word");
        }

        return builder.toString();
    }

    public String readAllUntil(char stopCharacterExclusive) throws IOException {
        StringBuilder builder = new StringBuilder();

        while (true) {
            int ch = readChar();

            if (ch == -1) {
                break;
            }

            char character = (char) ch;
            if (character == stopCharacterExclusive) {
                unreadChar();
                break;
            }

            builder.append(character);
        }

        return builder.toString();
    }

    public boolean isSpace(char character) {
        return Character.isSpaceChar(character);
    }

    public boolean isDigit(char character) {
        return Character.isDigit(character);
    }

    public boolean isLetter(char character) {
        return Character.isLetter(character);
    }

    public boolean isLetterOrDigit(char character) {
        return Character.isLetterOrDigit(character);
    }

    public void close() throws Exception {
        reader.close();
    }

    public boolean isNewLine(char character) {
        return character == '\n' || character == '\r';
    }
}
