package org.nobloat.bare.gen;

import org.nobloat.bare.dsl.Ast;
import org.nobloat.bare.dsl.AstParser;
import org.nobloat.bare.dsl.Lexer;
import org.nobloat.bare.dsl.Scanner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public class CodeGenerator {

    public static void createJavaTypes(String packageName, List<Ast.Type> types, OutputStream outputStream) throws Exception {
        CodeWriter writer = new CodeWriter(outputStream);

        writer.write("package " + packageName + ";");
        writer.write("");

        writer.write("public class Messages {");
        writer.indendt();
        for (var type : types) {
            //TODO: switch on types
            writer.write(classProlog(type.name));
            writer.indendt();
            writeTypeFields(writer, type);
            writer.dedendt();
            writer.write(classEpilog());
        }
        writer.dedendt();
        writer.write("}");
        writer.close();
    }

    private static void writeTypeFields(CodeWriter writer, Ast.Type type) {
        if (type.kind == Ast.TypeKind.UserType && ((Ast.UserDefinedType)type).type.kind == Ast.TypeKind.Struct) {
            writeStructFields(writer, (Ast.StructType) ((Ast.UserDefinedType)type).type);
        }

    }

    private static void writeStructFields (CodeWriter writer, Ast.StructType struct) {
        for(var field : struct.fields) {
            switch (field.type.kind) {
                case U8:
                    writer.write("public @Int(Int.Type.u8) Short " + field.name + ";");
                    break;
                case I8:
                    writer.write("public @Int(Int.Type.i8) Short " + field.name + ";");
                    break;
                case U16:
                    writer.write("public @Int(Int.Type.u16) Integer " + field.name + ";");
                    break;
                case I16:
                    writer.write("public @Int(Int.Type.i16) Short " + field.name + ";");
                    break;
                case U32:
                    writer.write("public @Int(Int.Type.u32) Long " + field.name + ";");
                    break;
                case I32:
                    writer.write("public @Int(Int.Type.i32) Integer " + field.name + ";");
                    break;
                case U64:
                    writer.write("public @Int(Int.Type.u64) BigInteger " + field.name + ";");
                    break;
                case I64:
                    writer.write("public @Int(Int.Type.i64) Long " + field.name + ";");
                    break;
                case STRING:
                    writer.write("public String " + field.name + ";");
                    break;
                case Bool:
                    writer.write("public Boolean " + field.name + ";");
                    break;
                case F32:
                    writer.write("public Float " + field.name + ";");
                    break;
                case F64:
                    writer.write("public Double " + field.name + ";");
                break;

            }
        }

    }


    public static String classProlog(String classname) {
        return "public static class " + classname + " {";
    }

    public static String classEpilog() {
        return "}";
    }

    public static void main(String[] args) throws Exception {
        try (var is = openFile("schema.bare"); var scanner = new Scanner(is)) {
            Lexer lexer = new Lexer(scanner);
            AstParser parser = new AstParser(lexer);
            CodeGenerator.createJavaTypes("com.example", parser.parse(), System.out);

        }
    }

    public static InputStream openFile(String name) throws FileNotFoundException {
        String path = "src/test/resources";
        File file = new File(path);
        return new FileInputStream(file.getAbsolutePath() + "/" + name);
    }
}
