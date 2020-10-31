package org.nobloat.bare.gen;

import org.nobloat.bare.dsl.Ast;

class ToStringMethod {

    private final CodeWriter codeWriter;
    private final String className;
    private String fieldSeparator = "";
    private ByteToHexStaticMethods byteToHexStaticMethods;

    public ToStringMethod(CodeWriter codeWriter, String className, ByteToHexStaticMethods byteToHexStaticMethods) {
        this.codeWriter = codeWriter;
        this.className = className;
        this.byteToHexStaticMethods = byteToHexStaticMethods;
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

    public void addField(Ast.Type type, String fieldName) {
        String field;
        switch (type.kind) {
            case U8:
                field = byteToHexStaticMethods.callByteToHex(fieldName);
                break;
            case DataArray:
                field = byteToHexStaticMethods.callByteArrayToHex(fieldName);
                break;
            case DataSlice:
                field = byteToHexStaticMethods.callByteSliceToHex(fieldName);
                break;
            case Slice:
                field = "\"[\"+orders.stream().map(Object::toString).collect(Collectors.joining(\",\")) + \"]\"";
                break;
            case Array:
                field = "Arrays.toString(" + fieldName + ")";
                break;
            case DefinedUserType:
                addField(((Ast.UserDefinedType)type).type, fieldName);
                return;
            case Map:


            default:
                field = fieldName;
        }

        codeWriter.write(String.format("\"%s%s=\" + %s + ", fieldSeparator, fieldName, field));
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
