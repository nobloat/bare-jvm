package org.nobloat.bare.gen;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class CodeWriter implements AutoCloseable {

    public static String INDENDT_CHARACTER = "\t";
    private int indent = 0;
    private ByteArrayOutputStream buffer;
    private PrintWriter writer;
    private OutputStream target;
    private List<CodeWriter> sections;

    public CodeWriter(OutputStream out) {
        buffer = new ByteArrayOutputStream();
        this.target = out;
        writer = new PrintWriter(out);
        sections = new ArrayList<>();
        writer = new PrintWriter(buffer);
    }

    public CodeWriter section() {
        var writer = new CodeWriter(buffer);
        writer.indent = this.indent;
        sections.add(writer);
        return writer;
    }

    public void indent() {
        indent++;
    }
    public void dedent() {
        indent--;
    }

    public void write(String codeLine) {
        writer.println(INDENDT_CHARACTER.repeat(indent) + codeLine);
    }

    public void newline() {
        writer.println();
    }

    @Override
    public void close() throws IOException {
        writer.close();
        buffer.writeTo(target);
        for(var section: sections) {
            section.close();
            section.buffer.writeTo(target);
        }
    }
}
