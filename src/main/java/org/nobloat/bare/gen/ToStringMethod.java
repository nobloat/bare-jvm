package org.nobloat.bare.gen;

class ToStringMethod {

    private final CodeWriter codeWriter;
    private final String className;
    private String fieldSeparator = "";

    public ToStringMethod(CodeWriter codeWriter, String className) {
        this.codeWriter = codeWriter;
        this.className = className;
        writeProlog();
    }

    private void writeProlog() {
        codeWriter.write("@Override");
        codeWriter.write("public String toString() {");
        codeWriter.indent();
        codeWriter.write(String.format("return \"%s{\" +", className));
        codeWriter.indent();
        codeWriter.indent();
    }

    public void addField(String fieldName) {
        codeWriter.write(String.format("\"%s%s=\" + %s + ", fieldSeparator, fieldName, fieldName));
        fieldSeparator = ", ";
    }

    public void writeEpilog() {
        codeWriter.write("'}';");
        codeWriter.dedent();
        codeWriter.dedent();
        codeWriter.dedent();
        codeWriter.write("}");
    }
}
