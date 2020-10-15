package org.nobloat.bare.dsl;

import java.util.List;
import java.util.stream.Collectors;

public class Ast {

    public static class Type {
        public String name;
        public final TypeKind kind;

        public Type(String name, TypeKind kind) {
            this.name = name;
            this.kind = kind;
        }
    }

    public static class UserDefinedType extends Type {
        public final Type type;
        public UserDefinedType(String name, Type type) {
            super(name, TypeKind.UserType);
            this.type = type;
        }
    }

    public static class PrimitiveType extends Type {
        public PrimitiveType(TypeKind kind) {
            super(kind.toString(),kind);
        }
    }

    public static class NamedUserType extends Type {
        public NamedUserType(String name) {
            super(name, TypeKind.UserType);
        }
    }

    public static class OptionalType extends Type {
        public final Type subType;

        public OptionalType(Type subType) {
            super(String.format("optional<%s>", subType.name), TypeKind.Optional);
            this.subType = subType;
        }
    }

    public static class DataType extends Type {
        public final long length;

        public DataType(long length) {
            super("data", length == 0 ? TypeKind.DataSlice : TypeKind.DataArray);
            this.length = length;
        }
    }

    public static class MapType extends Type {
        public final Type key;
        public final Type value;

        public MapType(Type key, Type value) {
            super(String.format("map[%s]%s",key.name, value.name), TypeKind.Map);
            this.key = key;
            this.value = value;
        }
    }

    public static class ArrayType extends Type {
        public final Type member;
        public final long length;

        public ArrayType(Type member, long length) {
            super("[]"+member.name, length == 0 ? TypeKind.Slice : TypeKind.Array);
            this.member = member;
            this.length = length;
        }
    }

    public static class UnionType extends Type {
        public final List<UnionVariant> variants;

        public UnionType(List<UnionVariant> variants) {
            super(String.format("union<%s>", variants.stream().map(u -> u.subtype.name).collect(Collectors.joining(","))), TypeKind.Union);
            this.variants = variants;
        }
    }

    public static class UnionVariant {
        public final Type subtype;
        public final int tag;

        public UnionVariant(Type subtype, int tag) {
            this.subtype = subtype;
            this.tag = tag;
        }
    }

    public static class StructType extends Type {
        public final List<StructField> fields;

        public StructType(List<StructField> fields) {
            super("struct", TypeKind.Struct);
            this.fields = fields;
        }
    }

    public static class StructField {
        public final String name;
        public final Type type;

        public StructField(String name, Type type) {
            this.name = name;
            this.type = type;
        }
    }

    public static class UserDefinedEnum extends Type {
        public final List<EnumValue> values;

        public UserDefinedEnum(String name, TypeKind kind, List<EnumValue> values) {
            super (name, kind);
            this.values = values;
        }
    }

    public static class EnumValue {
        public final String name;
        public final int value;

        public EnumValue(String name, int value) {
            this.name = name;
            this.value = value;
        }
    }

    public enum TypeKind {
        UINT,U8,U16,U32,U64,
        INT,I8,I16,I32,I64,
        F32,F64,
        Bool,
        STRING,
        Void,
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