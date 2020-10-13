package org.nobloat.bare.dsl;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AstParserTest {

    @Test
    public void shouldParseSimpleDataTypeWithLength() throws Exception {
        String input = "type PublicKey data<128>";

        try (InputStream is = toStream(input);
             Scanner scanner = new Scanner(is)) {
            Lexer lexer = new Lexer(scanner);
            AstParser astParser = new AstParser(lexer);

            List<Ast.Type> Types = astParser.parse();
            assertEquals(1, Types.size());

            Ast.Type type = Types.get(0);
            assertEquals("PublicKey", type.name);
            assertTrue(type instanceof Ast.UserDefinedType);

            Ast.UserDefinedType userDefinedType = (Ast.UserDefinedType) type;
            assertEquals(Ast.TypeKind.DataArray, userDefinedType.type.kind);

            Ast.DataType dataType = (Ast.DataType) userDefinedType.type;
            assertEquals(128, dataType.length);
        }
    }

    @Test
    public void shouldParseUnionType() throws Exception {
        String input = "type Person (Customer | Employee)";

        try (InputStream is = toStream(input);
             Scanner scanner = new Scanner(is)) {
            Lexer lexer = new Lexer(scanner);
            AstParser astParser = new AstParser(lexer);

            List<Ast.Type> Types = astParser.parse();
            assertEquals(1, Types.size());

            Ast.Type type = Types.get(0);
            assertEquals("Person", type.name);
            assertTrue(type instanceof Ast.UserDefinedType);

            Ast.UserDefinedType userDefinedType = (Ast.UserDefinedType) type;
            assertEquals(Ast.TypeKind.Union, userDefinedType.type.kind);

            Ast.UnionType unionType = (Ast.UnionType) userDefinedType.type;
            List<Ast.UnionVariant> subTypes = unionType.variants;
            assertEquals(2, subTypes.size());

            Ast.UnionVariant customerSubType = subTypes.get(0);
            assertEquals(Ast.TypeKind.UserType, customerSubType.subtype.kind);
        }
    }

    @Test
    public void shouldParseEnum() throws Exception {
        String input = "enum Department {\n" +
                "  ACCOUNTING\n" +
                "  ADMINISTRATION\n" +
                "  CUSTOMER_SERVICE\n" +
                "  DEVELOPMENT\n" +
                "\n" +
                "  # Reserved for the CEO\n" +
                "  JSMITH = 99\n" +
                "}";

        try (InputStream is = toStream(input);
             Scanner scanner = new Scanner(is)) {
            Lexer lexer = new Lexer(scanner);
            AstParser astParser = new AstParser(lexer);

            List<Ast.Type> Types = astParser.parse();
            assertEquals(1, Types.size());

            Ast.Type type = Types.get(0);
            assertEquals("Department", type.name);
            assertTrue(type instanceof Ast.UserDefinedEnum);

            Ast.UserDefinedEnum userDefinedEnum = (Ast.UserDefinedEnum) type;
            assertEquals(Ast.TypeKind.UINT, userDefinedEnum.kind);

            List<Ast.EnumValue> values = userDefinedEnum.values;
            assertEquals(5, values.size());

            Ast.EnumValue accounting = values.get(0);
            assertEquals("ACCOUNTING", accounting.name);
            assertEquals(0, accounting.value);

            Ast.EnumValue administration = values.get(1);
            assertEquals("ADMINISTRATION", administration.name);
            assertEquals(1, administration.value);

            Ast.EnumValue customerService = values.get(2);
            assertEquals("CUSTOMER_SERVICE", customerService.name);
            assertEquals(2, customerService.value);

            Ast.EnumValue development = values.get(3);
            assertEquals("DEVELOPMENT", development.name);
            assertEquals(3, development.value);

            Ast.EnumValue jsmith = values.get(4);
            assertEquals("JSMITH", jsmith.name);
            assertEquals(99, jsmith.value);
        }
    }

    @Test
    public void shouldParseUserDefinedType() throws Exception {
        String input = "type Address {\n" +
                "  address: [4]string\n" +
                "  city: string\n" +
                "  publicKey: optional<PublicKey>\n" +
                "  metadata: map[string]data\n" +
                "  orders: []{\n" +
                "    orderId: i64\n" +
                "    quantity: i32\n" +
                "  }\n" +
                "}";

        try (InputStream is = toStream(input);
             Scanner scanner = new Scanner(is)) {
            Lexer lexer = new Lexer(scanner);
            AstParser astParser = new AstParser(lexer);

            List<Ast.Type> Types = astParser.parse();
            assertEquals(1, Types.size());

            Ast.Type type = Types.get(0);
            assertEquals("Address", type.name);
            assertTrue(type instanceof Ast.UserDefinedType);

            Ast.UserDefinedType userDefinedType = (Ast.UserDefinedType) type;
            assertEquals(Ast.TypeKind.Struct, userDefinedType.type.kind);

            Ast.StructType structType = (Ast.StructType) userDefinedType.type;
            List<Ast.StructField> fields = structType.fields;
            assertEquals(5, fields.size());

            // address: [4]string
            Ast.StructField addressField = fields.get(0);
            assertEquals("address", addressField.name);
            assertTrue(addressField.type instanceof Ast.ArrayType);
            Ast.ArrayType addressArrayType = (Ast.ArrayType) addressField.type;
            assertEquals(4, addressArrayType.length);
            assertEquals(Ast.TypeKind.STRING, addressArrayType.member.kind);

            // city: string
            Ast.StructField cityField = fields.get(1);
            assertEquals("city", cityField.name);
            assertTrue(cityField.type instanceof Ast.PrimitiveType);
            Ast.PrimitiveType cityPrimitiveType = (Ast.PrimitiveType) cityField.type;
            assertEquals(Ast.TypeKind.STRING, cityPrimitiveType.kind);

            // publicKey: optional<PublicKey>
            Ast.StructField publicKeyField = fields.get(2);
            assertEquals("publicKey", publicKeyField.name);
            assertTrue(publicKeyField.type instanceof Ast.OptionalType);
            Ast.OptionalType publicKeyOptionalType = (Ast.OptionalType) publicKeyField.type;
            assertEquals(Ast.TypeKind.Optional, publicKeyOptionalType.kind);
            assertEquals(Ast.TypeKind.UserType, publicKeyOptionalType.subType.kind);
            assertEquals("PublicKey", publicKeyOptionalType.subType.name);

            // metadata: map[string]data
            Ast.StructField metadataField = fields.get(3);
            assertEquals("metadata", metadataField.name);
            assertTrue(metadataField.type instanceof Ast.MapType);
            Ast.MapType metadataMapType = (Ast.MapType) metadataField.type;
            assertEquals(Ast.TypeKind.Map, metadataMapType.kind);
            assertTrue(metadataMapType.key instanceof Ast.PrimitiveType);
            assertEquals(Ast.TypeKind.STRING, metadataMapType.key.kind);
            assertTrue(metadataMapType.value instanceof Ast.DataType);
            assertEquals(Ast.TypeKind.DataSlice, metadataMapType.value.kind);


            // orders: []{
            //  orderId: i64
            //  quantity: i32
            // }
            Ast.StructField ordersArrayField = fields.get(4);
            assertEquals("orders", ordersArrayField.name);
            assertTrue(ordersArrayField.type instanceof Ast.ArrayType);
            Ast.ArrayType ordersArrayType = (Ast.ArrayType) ordersArrayField.type;
            assertEquals(0, ordersArrayType.length);
            assertTrue(ordersArrayType.member instanceof Ast.StructType);

            Ast.StructType ordersStructType = (Ast.StructType) ordersArrayType.member;
            List<Ast.StructField> orderFields = ordersStructType.fields;
            assertEquals(2, orderFields.size());

            // orderId: i64
            Ast.StructField orderIdField = orderFields.get(0);
            assertEquals("orderId", orderIdField.name);
            assertTrue(orderIdField.type instanceof Ast.PrimitiveType);
            Ast.PrimitiveType orderIdType = (Ast.PrimitiveType) orderIdField.type;
            assertEquals(Ast.TypeKind.I64, orderIdType.kind);

            // quantity: i32
            Ast.StructField quantityField = orderFields.get(1);
            assertEquals("quantity", quantityField.name);
            assertTrue(quantityField.type instanceof Ast.PrimitiveType);
            Ast.PrimitiveType quantityType = (Ast.PrimitiveType) quantityField.type;
            assertEquals(Ast.TypeKind.I32, quantityType.kind);
        }
    }

    private InputStream toStream(String input) {
        return new ByteArrayInputStream(input.getBytes());
    }
}
