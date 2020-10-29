package org.nobloat.bare.gen;

public class ByteToHexStaticMethods {

    private final CodeWriter codeWriter;
    private boolean byteToHex = false;
    private boolean byteArrayToHex = false;
    private boolean byteSliceToHex = false;

    public ByteToHexStaticMethods(CodeWriter codeWriter) {
        this.codeWriter = codeWriter;
    }

    public String callByteToHex(String fieldName) {
        byteToHex = true;
        return String.format("byteToHex(%s)", fieldName);
    }

    public String callByteArrayToHex(String fieldName) {
        byteArrayToHex = true;
        return String.format("byteArrayToHex(%s)", fieldName);
    }

    public String callByteSliceToHex(String fieldName) {
        byteSliceToHex = true;
        return String.format("byteSliceToHex(%s)", fieldName);
    }

    public void addStaticMethods() {
        if (byteToHex || byteArrayToHex) {
            byteToHexMethod();
        }

        if(byteArrayToHex) {
            byteArrayToHexMethod();
        }

        if (byteSliceToHex) {
            byteSliceToHexMethod();
        }
    }

    private void byteToHexMethod() {
        codeWriter.write("static String byteToHex(byte num) {");
        codeWriter.indent();

        codeWriter.write("char[] hexDigits = new char[4];");
        codeWriter.write("hexDigits[0] = '0';");
        codeWriter.write("hexDigits[1] = 'x';");
        codeWriter.write("hexDigits[2] = Character.forDigit((num >> 4) & 0xF, 16);");
        codeWriter.write("hexDigits[3] = Character.forDigit((num & 0xF), 16);");
        codeWriter.write("return new String(hexDigits);");

        codeWriter.dedent();
        codeWriter.write("}");
    }

    private void byteArrayToHexMethod() {
        codeWriter.write("static String byteArrayToHex(byte[] byteArray) {");
        codeWriter.indent();

        codeWriter.write("StringBuffer hexStringBuffer = new StringBuffer();");
        codeWriter.write("for (int i = 0; i < byteArray.length; i++) {");
        codeWriter.indent();
        codeWriter.write("hexStringBuffer.append(byteToHex(byteArray[i]));");
        codeWriter.write("hexStringBuffer.append(' ');");
        codeWriter.dedent();
        codeWriter.write("}");
        codeWriter.newline();

        codeWriter.write("hexStringBuffer.deleteCharAt(hexStringBuffer.length() - 1);");
        codeWriter.write("return hexStringBuffer.toString();");

        codeWriter.dedent();
        codeWriter.write("}");
    }

    private void byteSliceToHexMethod() {
        codeWriter.write("static String byteSliceToHex(Byte[] byteArray) {");
        codeWriter.indent();

        codeWriter.write("StringBuffer hexStringBuffer = new StringBuffer();");
        codeWriter.write("for (int i = 0; i < byteArray.length; i++) {");
        codeWriter.indent();
        codeWriter.write("hexStringBuffer.append(byteToHex(byteArray[i]));");
        codeWriter.write("hexStringBuffer.append(' ');");
        codeWriter.dedent();
        codeWriter.write("}");
        codeWriter.newline();

        codeWriter.write("hexStringBuffer.deleteCharAt(hexStringBuffer.length() - 1);");
        codeWriter.write("return hexStringBuffer.toString();");

        codeWriter.dedent();
        codeWriter.write("}");
    }
}
