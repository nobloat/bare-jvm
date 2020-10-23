package org.nobloat.bare.gen;

import org.nobloat.bare.BareException;
import org.nobloat.bare.dsl.Ast;
import org.nobloat.bare.dsl.AstParser;
import org.nobloat.bare.dsl.Lexer;
import org.nobloat.bare.dsl.Scanner;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class CodeGenerator {

    private final String packageName;
    private final String className;
    private final List<Ast.Type> types;
    private final CodeWriter writer;
    private final Set<String> usedTypes = new HashSet<>();
    private ByteToHexStaticMethods byteToHexStaticMethods;

    public CodeGenerator(String packageName, String className, List<Ast.Type> types, OutputStream target) {
        this.packageName = packageName;
        this.types = types;
        this.className = Objects.requireNonNullElse(className, "Messages");
        this.writer = new CodeWriter(target);
    }

    public void createJavaTypes() throws IOException, BareException {
        var importSection = writer.section();

        if (packageName != null) {
            importSection.write("package " + packageName + ";");
        }

        var messagesSection = writer.section();
        messagesSection.write("public class " + className + "  {");
        messagesSection.newline();
        writer.indent();

        byteToHexStaticMethods = new ByteToHexStaticMethods(writer);

        for (var type : types) {
            createJavaType(type);
        }

        byteToHexStaticMethods.addStaticMethods();

        writer.dedent();
        writer.write("}");

        createImports(importSection);

        writer.close();
    }

    public void createImports(CodeWriter section) {
        usedTypes.add("org.nobloat.bare.AggregateBareDecoder");
        usedTypes.add("org.nobloat.bare.AggregateBareEncoder");
        usedTypes.add("org.nobloat.bare.BareException");
        usedTypes.add("java.io.IOException");

        var types = new ArrayList<>(usedTypes);
        Collections.sort(types);

        for (var usedType : types) {
            section.write("import " + usedType + ";");
        }

        section.newline();
    }


    public void createJavaType(Ast.Type type) throws BareException {
        if (type.kind == Ast.TypeKind.UserType && ((Ast.UserDefinedType) type).type.kind == Ast.TypeKind.Struct) {
            createStruct((Ast.UserDefinedType) type);
        } else if (type instanceof Ast.UserDefinedEnum) {
            createEnum((Ast.UserDefinedEnum) type);
        } else if (type.kind == Ast.TypeKind.UserType && ((Ast.UserDefinedType) type).type.kind == Ast.TypeKind.Union) {
            createUnion((Ast.UnionType) ((Ast.UserDefinedType) type).type);
        } else {
            createTypeAlias((Ast.UserDefinedType) type);
        }
    }

    public void createTypeAlias(Ast.UserDefinedType type) throws BareException {
        writer.write("public static class " + type.name + " {");
        writer.indent();
        writer.newline();

        writer.write("public " + fieldTypeMap(type.type) + " value;");
        writer.newline();

        writer.write("public static " + type.name + " decode(AggregateBareDecoder decoder) throws IOException, BareException {");
        writer.indent();
        writer.write("var o = new " + type.name + "();");
        writer.write("o.value = " + decodeStatement(type.type) + ";");
        writer.write("return o;");
        writer.dedent();
        writer.write("}");
        writer.newline();

        writer.write("public void encode(AggregateBareEncoder encoder) throws IOException, BareException {");
        writer.indent();
        writer.write(encodeStatement(type.type, "value") + ";");
        writer.dedent();
        writer.write("}");
        writer.newline();

        ToStringMethod toStringMethod = new ToStringMethod(writer, type.name, byteToHexStaticMethods);
        toStringMethod.addField(type.kind, "value");
        toStringMethod.writeEpilog();

        writer.dedent();
        writer.write("}");
        writer.newline();
    }

    public void createStruct(Ast.UserDefinedType struct) throws BareException {

        var structSection = writer.section();

        var fieldSection = structSection.section();
        var decodeSection = structSection.section();
        var encodeSection = structSection.section();
        var toStringSection = structSection.section();

        fieldSection.write("public static class " + struct.name + " {");
        fieldSection.newline();

        var fields = ((Ast.StructType) struct.type).fields;

        fieldSection.indent();
        decodeSection.indent();
        decodeSection.write("public static " + struct.name + " decode(AggregateBareDecoder decoder) throws IOException, BareException {");
        decodeSection.indent();
        decodeSection.write("var o = new " + struct.name + "();");

        encodeSection.indent();
        encodeSection.write("public void encode(AggregateBareEncoder encoder) throws IOException, BareException {");
        encodeSection.indent();

        toStringSection.indent();

        ToStringMethod toStringMethod = new ToStringMethod(toStringSection, struct.name, byteToHexStaticMethods);

        for (var field : fields) {
            String fieldMapping = "public " + fieldTypeMap(field.type) + " " + field.name;
            if (field.type.kind == Ast.TypeKind.Array || field.type.kind == Ast.TypeKind.DataArray) {
                var arrayType = (Ast.ArrayType) field.type;
                fieldMapping += " = new Array<>(" + arrayType.length + ")";
            }
            fieldSection.write(fieldMapping + ";");

            decodeSection.write("o." + field.name + " = " + decodeStatement(field.type) + ";");
            encodeSection.write(encodeStatement(field.type, field.name) + ";");

            toStringMethod.addField(field.type.kind, field.name);
        }

        fieldSection.newline();

        decodeSection.write("return o;");
        decodeSection.dedent();
        decodeSection.write("}");
        decodeSection.newline();

        encodeSection.dedent();
        encodeSection.write("}");
        encodeSection.newline();

        toStringMethod.writeEpilog();

        structSection.write("}");
        structSection.newline();
    }

    private String encodeStatement(Ast.Type type, String name) throws BareException {
        switch (type.kind) {
            case U8:
                usedTypes.add("org.nobloat.bare.Int");
                return "encoder.u8(" + name + ")";
            case I8:
                usedTypes.add("org.nobloat.bare.Int");
                return "encoder.i8(" + name + ")";
            case U16:
                usedTypes.add("org.nobloat.bare.Int");
                return "encoder.u16(" + name + ")";
            case I16:
                usedTypes.add("org.nobloat.bare.Int");
                return "encoder.i16(" + name + ")";
            case U32:
                usedTypes.add("org.nobloat.bare.Int");
                return "encoder.u32(" + name + ")";
            case I32:
                usedTypes.add("org.nobloat.bare.Int");
                return "encoder.i32(" + name + ")";
            case U64:
                usedTypes.add("org.nobloat.bare.Int");
                return "encoder.u64(" + name + ")";
            case I64:
                usedTypes.add("org.nobloat.bare.Int");
                return "encoder.i64(" + name + ")";
            case STRING:
                return "encoder.string(" + name + ")";
            case Bool:
                return "encoder.bool(" + name + ")";
            case F32:
                return "encoder.f32(" + name + ")";
            case F64:
                return "encoder.f64(" + name + ")";
            case INT:
                usedTypes.add("org.nobloat.bare.Int");
                return "encoder.variadicInt(" + name + ")";
            case UINT:
                usedTypes.add("org.nobloat.bare.Int");
                return "encoder.variadicUInt(" + name + ")";
            case DataSlice:
                return "encoder.data(" + name + ")";
            case UserType:
                return name + ".encode(encoder)";
            case Optional:
                usedTypes.add("java.util.Optional");
                return "encoder.optional(" + name + "," + encodeLambda(((Ast.OptionalType) type).subType) + ")";
            case Map:
                return "encoder.map(" + name + "," + encodeLambda(((Ast.MapType) type).key) + "," + encodeLambda(((Ast.MapType) type).value) + ")";
            case Slice:
                return "encoder.slice(" + name + "," + encodeLambda(((Ast.ArrayType) type).member) + ")";
            case DataArray:
                return "encoder.array(" + name + ", encoder::u8)";
            case Array:
                return "encoder.array(" + name + ", " + encodeLambda(((Ast.ArrayType) type).member) + ")";
            default:
                throw new BareException("Unknown encoding statement for " + type.name);
        }
    }

    private String encodeLambda(Ast.Type type) throws BareException {
        switch (type.kind) {
            case U8:
                return "encoder::u8";
            case I8:
                return "encoder::i8";
            case U16:
                return "encoder::u16";
            case I16:
                return "encoder::i16";
            case U32:
                return "encoder::u32";
            case I32:
                return "encoder::i32";
            case U64:
                return "encoder::u64";
            case I64:
                return "encoder::i64";
            case STRING:
                return "encoder::string";
            case Bool:
                return "encoder::bool";
            case F32:
                return "encoder::f32";
            case F64:
                return "encoder::f64";
            case INT:
                return "encoder::variadicInt";
            case UINT:
                return "encoder::variadicUint";
            case DataSlice:
                return "encoder::data";
            case Struct:
            case UserType:
                return "o -> o.encode(encoder)";
            default:
                throw new BareException("Unknown lambda for " + type.name);
        }
    }

    public void createUnion(Ast.UnionType union) {

        usedTypes.add("org.nobloat.bare.Union");

        var types = union.variants.stream().map(v -> {
            try {
                return v.tag + "," + decodeLambda(v.subtype);
            } catch (BareException e) {
                e.printStackTrace();
            }
            return null;
        }).collect(Collectors.joining(","));


        writer.write("public static class " + union.name + " extends Union {");
        writer.indent();
        writer.newline();

        writer.write("public static Union decode(AggregateBareDecoder decoder) throws IOException, BareException {");
        writer.indent();
        writer.write("return decoder.union(Map.of(" + types + "));");
        writer.dedent();
        writer.write("}");
        writer.newline();

        types = union.variants.stream().map(v -> v.tag + ", o -> ((" + v.subtype.name + ")o).encode(encoder)").collect(Collectors.joining(","));

        writer.write("public void encode(AggregateBareEncoder encoder) throws IOException, BareException {");
        writer.indent();
        writer.write("encoder.union(this, Map.of(" + types + "));");
        writer.dedent();
        writer.write("}");

        writer.dedent();
        writer.write("}");
        writer.newline();
    }

    public void createEnum(Ast.UserDefinedEnum enumeration) {
        writer.write("public enum " + enumeration.name + " {");
        writer.indent();
        writer.newline();

        writer.write(enumeration.values.stream().map(v -> v.name + "(" + v.value + ")").collect(Collectors.joining(",")) + ";");
        writer.newline();

        writer.write("@Int(Int.Type.ui)");
        writer.write("private int value;");
        writer.newline();

        writer.write(enumeration.name + "(int value) {");
        writer.indent();
        writer.write("this.value = value;");
        writer.dedent();
        writer.write("}");
        writer.newline();

        writer.write("public static " + enumeration.name + " decode(AggregateBareDecoder decoder) throws IOException, BareException {");
        writer.indent();

        writer.write("var i = decoder.variadicUint().intValue();");
        writer.write("switch(i) {");
        writer.indent();

        for (var value : enumeration.values) {
            writer.write("case " + value.value + ": return " + value.name + ";");
        }

        writer.write("default: throw new BareException(\"Unexpected enum value: \" + i); ");

        writer.dedent();
        writer.write("}");
        writer.dedent();
        writer.write("}");
        writer.newline();

        writer.write("public void encode(AggregateBareEncoder encoder) throws IOException {");
        writer.indent();
        writer.write("encoder.variadicUInt(value);");
        writer.dedent();
        writer.write("}");

        writer.dedent();
        writer.write("}");
        writer.newline();
    }

    private String decodeStatement(Ast.Type type) throws BareException {
        switch (type.kind) {
            case U8:
                usedTypes.add("org.nobloat.bare.Int");
                return "decoder.u8()";
            case I8:
                usedTypes.add("org.nobloat.bare.Int");
                return "decoder.i8()";
            case U16:
                usedTypes.add("org.nobloat.bare.Int");
                return "decoder.u16()";
            case I16:
                usedTypes.add("org.nobloat.bare.Int");
                return "decoder.i16()";
            case U32:
                usedTypes.add("org.nobloat.bare.Int");
                return "decoder.u32()";
            case I32:
                usedTypes.add("org.nobloat.bare.Int");
                return "decoder.i32()";
            case U64:
                usedTypes.add("org.nobloat.bare.Int");
                return "decoder.u64()";
            case I64:
                usedTypes.add("org.nobloat.bare.Int");
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
                usedTypes.add("org.nobloat.bare.Int");
                return "decoder.variadicInt()";
            case UINT:
                usedTypes.add("org.nobloat.bare.Int");
                return "decoder.variadicUint()";
            case DataSlice:
                return "decoder.data()";
            case DataArray:
                return "decoder.data(" + ((Ast.DataType) type).length + ")";
            case UserType:
                return type.name + ".decode(decoder)";
            case Optional:
                usedTypes.add("java.util.Optional");
                return "decoder.optional(" + decodeLambda(((Ast.OptionalType) type).subType) + ")";
            case Map:
                return "decoder.map(" + decodeLambda(((Ast.MapType) type).key) + "," + decodeLambda(((Ast.MapType) type).value) + ")";
            case Slice:
                return "decoder.slice(" + decodeLambda(((Ast.ArrayType) type).member) + ")";
            case Array:
                return "decoder.array(" + ((Ast.ArrayType) type).length + ", " + decodeLambda(((Ast.ArrayType) type).member) + ")";
        }
        return "";
    }

    private String decodeLambda(Ast.Type type) throws BareException {
        switch (type.kind) {
            case U8:
                return "AggregateBareDecoder::u8";
            case I8:
                return "AggregateBareDecoder::i8";
            case U16:
                return "AggregateBareDecoder::u16";
            case I16:
                return "AggregateBareDecoder::i16";
            case U32:
                return "AggregateBareDecoder::u32";
            case I32:
                return "AggregateBareDecoder::i32";
            case U64:
                return "AggregateBareDecoder::u64";
            case I64:
                return "AggregateBareDecoder::i64";
            case STRING:
                return "AggregateBareDecoder::string";
            case Bool:
                return "AggregateBareDecoder::bool";
            case F32:
                return "AggregateBareDecoder::f32";
            case F64:
                return "AggregateBareDecoder::f64";
            case INT:
                return "AggregateBareDecoder::variadicInt";
            case UINT:
                return "AggregateBareDecoder::variadicUint";
            case DataSlice:
                return "AggregateBareDecoder::data";
            case Struct:
            case UserType:
                return type.name + "::decode";
            default:
                throw new BareException("Unknown lambda for " + type.name);
        }
    }

    private String fieldTypeMap(Ast.Type type) throws BareException {
        switch (type.kind) {
            case U8:
                return "@Int(Int.Type.u8) byte";
            case I8:
                return "@Int(Int.Type.i8) short";
            case U16:
                return "@Int(Int.Type.u16) int";
            case I16:
                return "@Int(Int.Type.i16) short";
            case U32:
                return "@Int(Int.Type.u32) long";
            case I32:
                return "@Int(Int.Type.i32) int";
            case U64:
                usedTypes.add("java.math.BigInteger");
                return "@Int(Int.Type.u64) BigInteger";
            case I64:
                return "@Int(Int.Type.i64) long";
            case STRING:
                return "String";
            case Bool:
                return "boolean";
            case F32:
                return "float";
            case F64:
                return "double";
            case INT:
                return "@Int(Int.Type.i) long";
            case UINT:
                usedTypes.add("java.math.BigInteger");
                return "@Int(Int.Type.ui) BigInteger";
            case DataSlice:
                return "byte[]";
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
                return "Array<" + fieldTypeMap(((Ast.ArrayType) type).member) + ">";
            case Struct:
                throw new UnsupportedOperationException("Java does not support anonymous nested classes");
            default:
                throw new BareException("Unknown field type mapping for " + type.name);
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.out.println("Usage: java -jar bare-jvm.jar schame.bare [Messages.java]");
            System.err.println("   Input schema required");
            System.exit(1);
        }

        String className = "Messages";
        String packageName = null;
        File outputFile = new File(className + ".java");

        if (args.length >= 2) {
            className = args[1];
            if (className.contains(".")) {
                packageName = className.substring(0, className.lastIndexOf("."));
                className = className.substring(className.lastIndexOf(".") + 1);
                var dirs = new File(packageName.replaceAll("\\.", "/"));
                dirs.mkdirs();
                outputFile = new File(dirs.getAbsolutePath() + "/" + className + ".java");
            }
        }

        try (var is = new FileInputStream(args[0]); var scanner = new Scanner(is); var target = new FileOutputStream(outputFile)) {
            Lexer lexer = new Lexer(scanner);
            AstParser parser = new AstParser(lexer);
            var types = parser.parse();
            new CodeGenerator(packageName, className, types, target).createJavaTypes();
        }
    }

}



