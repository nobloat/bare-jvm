package org.nobloat.bare.dsl;

import java.util.List;

public class Ast {

    public interface SchemaType {
        String name();
    }

    public interface Type {
        TypeKind kind();
    }

    public static class UserDefinedType implements SchemaType {

        final String name;
        final Type type;

        public UserDefinedType(String name, Type type) {
            this.name = name;
            this.type = type;
        }

        @Override
        public String name() {
            return name;
        }

        public Type type() {
            return type;
        }
    }

    public static class PrimitiveType implements Type {

        final TypeKind kind;

        public PrimitiveType(TypeKind kind) {
            this.kind = kind;
        }

        @Override
        public TypeKind kind() {
            return kind;
        }
    }

    public static class NamedUserType implements Type {

        final String name;

        public NamedUserType(String name) {
            this.name = name;
        }

        @Override
        public TypeKind kind() {
            return TypeKind.UserType;
        }
    }

    public static class OptionalType implements Type {

        final Type subType;

        public OptionalType(Type subType) {
            this.subType = subType;
        }

        @Override
        public TypeKind kind() {
            return TypeKind.Optional;
        }
    }

    public static class DataType implements Type {

        final long length;

        public DataType(long length) {
            this.length = length;
        }

        @Override
        public TypeKind kind() {
            if (length == 0) {
                return TypeKind.DataSlice;
            }

            return TypeKind.DataArray;
        }
    }

    public static class MapType implements Type {

        final Type key;
        final Type value;

        public MapType(Type key, Type value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public TypeKind kind() {
            return TypeKind.Map;
        }
    }

    public static class ArrayType implements Type {

        final Type member;
        final long length;

        public ArrayType(Type member, long length) {
            this.member = member;
            this.length = length;
        }

        @Override
        public TypeKind kind() {
            if (length == 0) {
                return TypeKind.Slice;
            }

            return TypeKind.Array;
        }
    }

    public static class UnionType implements Type {
        final List<UnionSubType> subTypes;

        public UnionType(List<UnionSubType> subTypes) {
            this.subTypes = subTypes;
        }

        @Override
        public TypeKind kind() {
            return TypeKind.Union;
        }
    }

    public static class UnionSubType {
        final Type subtype;
        final int tag;

        public UnionSubType(Type subtype, int tag) {
            this.subtype = subtype;
            this.tag = tag;
        }
    }

    public static class StructType implements Type {
        final List<StructField> fields;

        public StructType(List<StructField> fields) {
            this.fields = fields;
        }

        @Override
        public TypeKind kind() {
            return TypeKind.Struct;
        }
    }

    public static class StructField {
        final String name;
        final Type type;

        public StructField(String name, Type type) {
            this.name = name;
            this.type = type;
        }
    }

    public static class UserDefinedEnum implements Type, SchemaType {
        final String name;
        final TypeKind kind;
        final List<EnumValue> values;

        public UserDefinedEnum(String name, TypeKind kind, List<EnumValue> values) {
            this.name = name;
            this.kind = kind;
            this.values = values;
        }

        @Override
        public Ast.TypeKind kind() {
            return kind;
        }

        @Override
        public String name() {
            return name;
        }
    }

    public static class EnumValue {
        final String name;
        final int value;

        public EnumValue(String name, int value) {
            this.name = name;
            this.value = value;
        }
    }

    enum TypeKind {
        UINT,
        U8,
        U16,
        U32,
        U64,
        INT,
        I8,
        I16,
        I32,
        I64,
        F32,
        F64,
        Bool,
        STRING,
        Void,
        // data,
        Data,
        // data<length>,
        DataFixed,
        // [len]type,
        Array,
        // []type,
        Slice,
        // optional<type>,
        Optional,
        // data<len>,
        DataArray,
        // data,
        DataSlice,
        // map[type]type,
        Map,
        // (type | type | ...),
        Union,
        // { fields... },
        Struct,
        // Named user type,
        UserType,
    }
}
