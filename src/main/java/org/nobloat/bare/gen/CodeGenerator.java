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
import java.util.stream.Collectors;

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
            writer.write("");
        }
        writer.dedendt();
        writer.write("}");
        writer.close();
    }

    private static void writeTypeFields(CodeWriter writer, Ast.Type type) {
        if (type.kind == Ast.TypeKind.UserType && ((Ast.UserDefinedType)type).type.kind == Ast.TypeKind.Struct) {
            writeStructFields(writer, (Ast.StructType) ((Ast.UserDefinedType)type).type);
        } else if (type instanceof Ast.UserDefinedEnum) {
            writer.write(writeEnumValues(((Ast.UserDefinedEnum)type).values));
        } else if (type.kind == Ast.TypeKind.Union) {
            //TODO: map unions
        } else {
            //TODO: change to inheritance -> move upwards to createJavaTypes
            var userDefindeType = (Ast.UserDefinedType)type;
            writer.write("public " + fieldTypeMap(userDefindeType.type) + " " + userDefindeType.name + ";");
        }

    }

    private static String writeEnumValues(List<Ast.EnumValue> values) {
        return values.stream().map(v -> v.name + "(" + v.value +")").collect(Collectors.joining(",")) + ";";
    }

    private static void writeStructFields (CodeWriter writer, Ast.StructType struct) {
        for(var field : struct.fields) {
            String fieldMapping = "public " + fieldTypeMap(field.type) + " " + field.name;
            if (field.type.kind == Ast.TypeKind.Array || field.type.kind == Ast.TypeKind.DataArray) {
                var arrayType = (Ast.ArrayType)field.type;
                fieldMapping += " = new Array<>("+arrayType.length+")";
            }
            writer.write(fieldMapping + ";");
        }

    }

    private static String fieldTypeMap(Ast.Type type) {
        switch (type.kind) {
            case U8:
               return "@Int(Int.Type.u8) Short";
            case I8:
                return "@Int(Int.Type.i8) Short";
            case U16:
                return "@Int(Int.Type.u16) Integer";
            case I16:
                return "@Int(Int.Type.i16) Short";
            case U32:
                return "@Int(Int.Type.u32) Long";
            case I32:
                return "@Int(Int.Type.i32) Integer";
            case U64:
                return "@Int(Int.Type.u64) BigInteger";
            case I64:
                return "@Int(Int.Type.i64) Long";
            case STRING:
                return "String";
            case Bool:
                return "Boolean";
            case F32:
                return "Float";
            case F64:
                return "Double";
            case INT:
                return "@Int(Int.Type.i) Long";
            case UINT:
                return "@Int(Int.Type.ui) Long";
            case DataSlice:
                return "List<Byte>";
            case DataArray:
                return "Array<Byte>";
            case UserType:
                return "" + type.name + ";";
            case Optional:
                //TODO: set state that optional was used for importing
                return "Optional<" + fieldTypeMap(((Ast.OptionalType) type).subType) + ">";
            case Map:
                return "Map<" + fieldTypeMap(((Ast.MapType) type).key) + "," + fieldTypeMap(((Ast.MapType) type).value) + ">";
            case Slice:
                return "List<" + fieldTypeMap(((Ast.ArrayType) type).member) + ">";
            case Array:
                return "Array<"+fieldTypeMap(((Ast.ArrayType) type).member) + ">";
        }
        return "";
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
