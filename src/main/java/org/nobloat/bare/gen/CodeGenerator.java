package org.nobloat.bare.gen;

import org.nobloat.bare.BareException;
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
import java.util.ArrayList;
import java.util.Collections;
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


    public void createJavaTypes() throws IOException, BareException {

        var importSection = writer.section();

        importSection.write("package " + packageName + ";");

        var messagesSection = writer.section();
        messagesSection.write("public class Messages {");
        writer.indent();

        for (var type : types) {
            createJavaType(type);
        }

        writer.dedent();
        writer.write("}");

       createImports(importSection);

        writer.close();
    }

    public void createImports(CodeWriter section) {

        var types = new ArrayList<>(usedTypes);
        Collections.sort(types);

        for(var usedType : types) {
            section.write("import " + usedType + ";");
        }
    }


    public void createJavaType(Ast.Type type) throws BareException {
        if (type.kind == Ast.TypeKind.UserType && ((Ast.UserDefinedType)type).type.kind == Ast.TypeKind.Struct) {
            createStruct((Ast.UserDefinedType)type);
        } else if (type instanceof Ast.UserDefinedEnum) {
            createEnum((Ast.UserDefinedEnum)type);
        } else if (type.kind == Ast.TypeKind.UserType && ((Ast.UserDefinedType)type).type.kind == Ast.TypeKind.Union) {
            createUnion((Ast.UnionType) ((Ast.UserDefinedType)type).type);
        } else {
            //TODO: change to inheritance -> move upwards to createJavaTypes
            //var userDefindeType = (Ast.UserDefinedType)type;
            //writer.write("public " + fieldTypeMap(userDefindeType.type) + " " + userDefindeType.name + ";");
        }
    }

    public void createStruct(Ast.UserDefinedType struct) throws BareException {

        var structSection = writer.section();

        var fieldSection = structSection.section();
        var decodeSection = structSection.section();

        fieldSection.write("public static class " + struct.name + " {");

        var fields = ((Ast.StructType)struct.type).fields;

        fieldSection.indent();
        decodeSection.indent();
        decodeSection.write("public static " + struct.name + " decode(AggregateBareDecoder decoder) throws IOException, BareException {");
        decodeSection.indent();
        decodeSection.write("var o = new " + struct.name + "();");

        for(var field : fields) {
            String fieldMapping = "public " + fieldTypeMap(field.type) + " " + field.name;
            if (field.type.kind == Ast.TypeKind.Array || field.type.kind == Ast.TypeKind.DataArray) {
                var arrayType = (Ast.ArrayType)field.type;
                fieldMapping += " = new Array<>("+arrayType.length+")";
            }
            fieldSection.write(fieldMapping + ";");

            decodeSection.write("o." + field.name + " = " + deocodeStatement(field.type) + ";");

        }

        decodeSection.write("return o;");
        decodeSection.dedent();
        decodeSection.write("}");

        structSection.write("}");
    }

    public void createUnion(Ast.UnionType union) {

        writer.write("public static class " + union.name + " extends Union {");
        writer.indent();

        writer.write("public static Union decode(AggregateBareDecoder decoder) throws IOException, BareException {");
        writer.indent();


        var types = union.variants.stream().map(v -> {
            try {
                return v.tag + "," + decodeLambda(v.subtype);
            } catch (BareException e) {
                e.printStackTrace();
            }
            return null;
        }).collect(Collectors.joining(","));

        writer.write("decoder.union(Map.of("+types+"));");

        writer.dedent();
        writer.write("}");

        writer.dedent();
        writer.write("}");

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

    private String deocodeStatement(Ast.Type type) throws BareException {
        switch (type.kind) {
            case U8:
                return "decoder.u8()";
            case I8:
                return "decoder.i8()";
            case U16:
                return "deocoder.u16()";
            case I16:
                return "deocoder.i16()";
            case U32:
                return "deocoder.u32()";
            case I32:
                return "deocoder.i32()";
            case U64:
                return "decoder.u64()";
            case I64:
                return "decoder.i64()";
            case STRING:
                return "decoder.string()";
            case Bool:
                return "decoder.bool()";
            case F32:
                return "decoder.f32()";
            case F64:
                return "decoder.f64()";
            case INT:
                return "decoder.variadicInt()";
            case UINT:
                return "decoder.variadicUint()";
            case DataSlice:
                return "decoder.data()";
            case DataArray:
                return "decoder.array("+((Ast.ArrayType)type).length+")";
            case UserType:
                return type.name + ".decode(decoder)";
            case Optional:
                return "decoder.optional("+decodeLambda(((Ast.OptionalType) type).subType)+")";
            case Map:
                return "decoder.map("+decodeLambda(((Ast.MapType) type).key)+","+decodeLambda(((Ast.MapType) type).value) + ")";
            case Slice:
                return "decoder.slice("+decodeLambda(((Ast.ArrayType) type).member)+")";
            case Array:
                return "decoder.array("+ ((Ast.ArrayType) type).length + ", " + decodeLambda(((Ast.ArrayType) type).member) +")";
        }
        return "";
    }

    private String decodeLambda(Ast.Type type) throws BareException {
        switch (type.kind) {
            case U8:
                return "PrimitiveBareDecoder::u8";
            case I8:
                return "PrimitiveBareDecoder::i8";
            case U16:
                return "PrimitiveBareDecoder::u16";
            case I16:
                return "PrimitiveBareDecoder::i16";
            case U32:
                return "PrimitiveBareDecoder::u32";
            case I32:
                return "PrimitiveBareDecoder::i32";
            case U64:
                return "PrimitiveBareDecoder::u64";
            case I64:
                return "PrimitiveBareDecoder::i64";
            case STRING:
                return "PrimitiveBareDecoder::string";
            case Bool:
                return "PrimitiveBareDecoder::bool";
            case F32:
                return "PrimitiveBareDecoder::f32";
            case F64:
                return "PrimitiveBareDecoder::f64";
            case INT:
                return "PrimitiveBareDecoder::variadicInt";
            case UINT:
                return "PrimitiveBareDecoder::variadicUint";
            case DataSlice:
                return "PrimitiveBareDecoder::data";
            case Struct:
            case UserType:
                return type.name + "::decode";
            default: throw new BareException("Unknown lambda for " + type.name);
        }
    }

    private String enocodeStatement(Ast.Type type) {
        return "";
    }

    private String fieldTypeMap(Ast.Type type) {
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
                usedTypes.add("java.util.List");
                return "List<Byte>";
            case DataArray:
                usedTypes.add("java.util.List");
                return "Array<Byte>";
            case UserType:
                return type.name;
            case Optional:
                usedTypes.add("org.nobloat.bare.Array");
                return "Optional<" + fieldTypeMap(((Ast.OptionalType) type).subType) + ">";
            case Map:
                usedTypes.add("java.util.Map");
                return "Map<" + fieldTypeMap(((Ast.MapType) type).key) + "," + fieldTypeMap(((Ast.MapType) type).value) + ">";
            case Slice:
                usedTypes.add("java.util.List");
                return "List<" + fieldTypeMap(((Ast.ArrayType) type).member) + ">";
            case Array:
                usedTypes.add("org.nobloat.bare.Array");
                return "Array<"+fieldTypeMap(((Ast.ArrayType) type).member) + ">";
        }
        return "";
    }

    public static void main(String[] args) throws Exception {
        try (var is = openFile("schema.bare"); var scanner = new Scanner(is)) {
            Lexer lexer = new Lexer(scanner);
            AstParser parser = new AstParser(lexer);
            var types = parser.parse();
            new CodeGenerator("org.example", types, System.out).createJavaTypes();
        }
    }

    public static InputStream openFile(String name) throws FileNotFoundException {
        String path = "src/test/resources";
        File file = new File(path);
        return new FileInputStream(file.getAbsolutePath() + "/" + name);
    }
}
