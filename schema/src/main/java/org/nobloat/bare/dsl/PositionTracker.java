package org.nobloat.bare.dsl;

public class PositionTracker {

    private int line = 1;
    private int column = 0;
    private int previousColumn = 0;
    private char lastCharacter;

    public void nextCharacter(int read) {
        lastCharacter = (char) read;

        column++;

        if (lastCharacter == '\n') {
            line++;
            previousColumn = column;
            column = 0;
        }
    }

    public void withdrawLastCharacter() {
        if (lastCharacter == '\n') {
            line--;
            column = previousColumn;
        } else {
            column--;
        }
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }
}
