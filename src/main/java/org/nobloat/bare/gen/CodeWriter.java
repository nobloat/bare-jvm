package org.nobloat.bare.gen;

import java.io.OutputStream;
import java.io.PrintWriter;

public class CodeWriter {

    public static String INDENDT_CHARACTER = "\t";
    private int indent = 0;
    private PrintWriter writer;

    public CodeWriter(OutputStream out) {
        writer = new PrintWriter(out);

    }

    public void indendt() {
        indent++;
    }

    public void dedendt() {
        indent--;
    }

    public void write(String codeLine) {
        writer.println(INDENDT_CHARACTER.repeat(indent) + codeLine);
    }
}
