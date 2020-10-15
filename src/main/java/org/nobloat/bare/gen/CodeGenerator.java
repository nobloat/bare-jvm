package org.nobloat.bare.gen;

import org.nobloat.bare.dsl.Ast;
import org.nobloat.bare.dsl.AstParser;
import org.nobloat.bare.dsl.Lexer;
import org.nobloat.bare.dsl.Scanner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class CodeGenerator {

    private String packageName;
    private List<Ast.Type> types;
    private CodeWriter writer;
    private Set<String> usedTypes;

    public CodeGenerator(String packageName, List<Ast.Type> types, OutputStream target) {
        this.packageName = packageName;
        this.types = types;
        usedTypes = new HashSet<>();
        writer = new CodeWriter(target);
    }


    public void createJavaTypes() throws IOException {
        writer.write("package " + packageName + ";");

        var importSection = writer.section();

        writer.write("public class Messages {");
        writer.indent();

        for (var type : types) {
            createJavaType(type);
        }

        writer.dedent();
        writer.write("}");


        writer.close();
    }


    public void createJavaType(Ast.Type type) {
        if (type.kind == Ast.TypeKind.UserType && ((Ast.UserDefinedType)type).type.kind == Ast.TypeKind.Struct) {
            //createStruct((Ast.StructType) ((Ast.UserDefinedType) type).type);
        } else if (type instanceof Ast.UserDefinedEnum) {
            createEnum((Ast.UserDefinedEnum)type);
        } else if (type.kind == Ast.TypeKind.Union) {
            //TODO: map unions
        } else {
            //TODO: change to inheritance -> move upwards to createJavaTypes
            //var userDefindeType = (Ast.UserDefinedType)type;
            //writer.write("public " + fieldTypeMap(userDefindeType.type) + " " + userDefindeType.name + ";");
        }
    }

    public void createStruct(Ast.StructType struct) {

    }

    public void createUnion(Ast.UnionType union) {

    }

    public void createEnum(Ast.UserDefinedEnum enumeration) {
        writer.write("public enum " + enumeration.name + " {");
        writer.indent();

        writer.write(enumeration.values.stream().map(v -> v.name + "(" + v.value + ")").collect(Collectors.joining(",")) + ";");

        usedTypes.add("org.nobloat.bare.Int");
        usedTypes.add("org.nobloat.bare.PrimitiveBareDecoder");
        usedTypes.add("org.nobloat.bare.PrimitiveBareEncoder");

        writer.write("@Int(Int.Type.ui)");
        writer.write("private int value;");

        writer.newline();

        writer.write(enumeration.name + "(int value) {");
        writer.indent();
        writer.write("this.value = value;");
        writer.dedent();
        writer.write("}");


        writer.write("public static " + enumeration.name + " decode(PrimitiveBareDecoder decoder) throws IOException, BareException {");
        writer.indent();

        writer.write("var i = decoder.variadicUint().intValue();");
        writer.write("return switch(i) {");
        writer.indent();

        for(var value : enumeration.values) {
            writer.write("case " + value.value + " -> " + value.name + ";");
        }

        writer.write("default -> throw new BareException(\"Unexpected enum value: \" + i); ");

        writer.dedent();
        writer.write("};");
        writer.dedent();
        writer.write("}");

        writer.write("public void encode(PrimitiveBareEncoder encoder) throws IOException {");
        writer.indent();
        writer.write("encoder.variadicUInt(value);");
        writer.dedent();
        writer.write("}");

        writer.dedent();
        writer.write("}");
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


    public static void main(String[] args) throws Exception {
        try (var is = openFile("schema.bare"); var scanner = new Scanner(is)) {
            Lexer lexer = new Lexer(scanner);
            AstParser parser = new AstParser(lexer);

            new CodeGenerator("org.example", parser.parse(), System.out).createJavaTypes();
        }
    }

    public static InputStream openFile(String name) throws FileNotFoundException {
        String path = "src/test/resources";
        File file = new File(path);
        return new FileInputStream(file.getAbsolutePath() + "/" + name);
    }
}
