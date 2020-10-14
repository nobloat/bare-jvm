package org.nobloat.bare.dsl;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.nobloat.bare.TestUtil.openFile;

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
            verifyPublicKey(Types.get(0));
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

            verifyPersonType(Types.get(0));
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

            verifyDepartment(Types.get(0));
        }
    }

    @Test
    public void shouldParseUserDefinedType() throws Exception {
        String input = "type Customer {\n" +
                "  name: string\n" +
                "  email: string\n" +
                "  address: Address\n" +
                "  orders: []{\n" +
                "    orderId: i64\n" +
                "    quantity: i32\n" +
                "  }\n" +
                "  metadata: map[string]data\n" +
                "}";

        try (InputStream is = toStream(input);
             Scanner scanner = new Scanner(is)) {
            Lexer lexer = new Lexer(scanner);
            AstParser astParser = new AstParser(lexer);

            List<Ast.Type> types = astParser.parse();
            assertEquals(1, types.size());
            verifyCustomerType(((Ast.UserDefinedType) types.get(0)).type);
        }
    }

    @Test
    void testWholeSchema() throws Exception {
        try (var stream = openFile("schema.bare"); var scanner = new Scanner(stream)) {
            Lexer lexer = new Lexer(scanner);
            AstParser parser = new AstParser(lexer);

            var types = parser.parse();
            assertEquals(7, types.size());


            verifyPublicKey(types.get(0));
            verifyTimeType(types.get(1));

            verifyDepartment(types.get(2));

            verifyCustomerType(((Ast.UserDefinedType) types.get(3)).type);
            verifyEmployee(((Ast.UserDefinedType) types.get(4)).type);

            verifyPersonType(types.get(5));
            verifyAddressType(types.get(6));
        }
    }

    @Test
    public void shouldThrowExceptionWhenInvalidTokenIsFound() throws Exception {
        String input = "type Customer {\n" +
                "  name: string\n" +
                "  metadata: asdf[string]data\n" +
                "}";

        try (InputStream is = toStream(input);
             Scanner scanner = new Scanner(is)) {
            Lexer lexer = new Lexer(scanner);
            AstParser astParser = new AstParser(lexer);

            try {
                astParser.parse();
            } catch (IllegalStateException ex) {
                assertEquals("(3:17) - Unexpected token '['. Required: field name", ex.getMessage());
            }
        }
    }

    @Test
    public void shouldThrowExceptionWhenInvalidTokenIsFound2() throws Exception {
        String input = "type Customer {\n" +
                "  address: Address\n" +
                "  orders: [{\n" +
                "    orderId: i64\n" +
                "    quantity: i32\n" +
                "  }\n" +
                "}";

        try (InputStream is = toStream(input);
             Scanner scanner = new Scanner(is)) {
            Lexer lexer = new Lexer(scanner);
            AstParser astParser = new AstParser(lexer);

            try {
                astParser.parse();
            } catch (IllegalStateException ex) {
                assertEquals("(3:12) - Unexpected token '{'. Required: ']'", ex.getMessage());
            }
        }
    }

    private InputStream toStream(String input) {
        return new ByteArrayInputStream(input.getBytes());
    }

    void verifyEmployee(Ast.Type type) {
        var fields = ((Ast.StructType) type).fields;

        verifyNameMailAddress(fields);

        assertEquals("department", fields.get(3).name);
        assertEquals(Ast.TypeKind.UserType, fields.get(3).type.kind);
        assertEquals("Department", fields.get(3).type.name);

        assertEquals("hireDate", fields.get(4).name);
        assertEquals(Ast.TypeKind.UserType, fields.get(4).type.kind);
        assertEquals("Time", fields.get(4).type.name);

        assertEquals("publicKey", fields.get(5).name);
        assertEquals(Ast.TypeKind.Optional, fields.get(5).type.kind);
        assertEquals("PublicKey", ((Ast.OptionalType) fields.get(5).type).subType.name);

        verifyMetaData(fields.get(6));
    }

    private void verifyNameMailAddress(List<Ast.StructField> fields) {
        var nameField = fields.get(0);
        assertEquals("name", nameField.name);
        assertEquals(Ast.TypeKind.STRING, nameField.type.kind);

        var emailField = fields.get(1);
        assertEquals("email", emailField.name);
        assertEquals(Ast.TypeKind.STRING, emailField.type.kind);

        assertEquals("address", fields.get(2).name);
        assertEquals(Ast.TypeKind.UserType, fields.get(2).type.kind);
    }

    void verifyCustomerType(Ast.Type type) {

        var fields = ((Ast.StructType) type).fields;

        verifyNameMailAddress(fields);

        // orders: []{
        //  orderId: i64
        //  quantity: i32
        // }
        Ast.StructField ordersArrayField = fields.get(3);
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

        // metadata: map[string]data
        verifyMetaData(fields.get(4));
    }

    private void verifyMetaData(Ast.StructField metadataField) {
        assertEquals("metadata", metadataField.name);
        assertTrue(metadataField.type instanceof Ast.MapType);
        Ast.MapType metadataMapType = (Ast.MapType) metadataField.type;
        assertEquals(Ast.TypeKind.Map, metadataMapType.kind);
        assertTrue(metadataMapType.key instanceof Ast.PrimitiveType);
        assertEquals(Ast.TypeKind.STRING, metadataMapType.key.kind);
        assertTrue(metadataMapType.value instanceof Ast.DataType);
        assertEquals(Ast.TypeKind.DataSlice, metadataMapType.value.kind);
    }

    void verifyAddressType(Ast.Type type) {
        assertEquals("Address", type.name);
        assertTrue(type instanceof Ast.UserDefinedType);

        Ast.UserDefinedType userDefinedType = (Ast.UserDefinedType) type;
        assertEquals(Ast.TypeKind.Struct, userDefinedType.type.kind);

        Ast.StructType structType = (Ast.StructType) userDefinedType.type;
        List<Ast.StructField> fields = structType.fields;
        assertEquals(4, fields.size());

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

        // state: string
        Ast.StructField stateField = fields.get(2);
        assertEquals("state", stateField.name);
        assertTrue(stateField.type instanceof Ast.PrimitiveType);
        Ast.PrimitiveType statePrimitiveType = (Ast.PrimitiveType) stateField.type;
        assertEquals(Ast.TypeKind.STRING, statePrimitiveType.kind);

        // state: string
        Ast.StructField countryField = fields.get(3);
        assertEquals("country", countryField.name);
        assertTrue(countryField.type instanceof Ast.PrimitiveType);
        Ast.PrimitiveType countryPrimitiveType = (Ast.PrimitiveType) countryField.type;
        assertEquals(Ast.TypeKind.STRING, countryPrimitiveType.kind);
    }

    void verifyPublicKey(Ast.Type type) {
        assertEquals("PublicKey", type.name);
        assertTrue(type instanceof Ast.UserDefinedType);

        Ast.UserDefinedType userDefinedType = (Ast.UserDefinedType) type;
        assertEquals(Ast.TypeKind.DataArray, userDefinedType.type.kind);

        Ast.DataType dataType = (Ast.DataType) userDefinedType.type;
        assertEquals(128, dataType.length);
    }

    void verifyTimeType(Ast.Type type) {
        assertEquals("Time", type.name);
        assertTrue(type instanceof Ast.UserDefinedType);

        Ast.UserDefinedType userDefinedType = (Ast.UserDefinedType) type;
        assertEquals(Ast.TypeKind.STRING, userDefinedType.type.kind);
    }

    void verifyPersonType(Ast.Type type) {
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

    void verifyDepartment(Ast.Type type) {
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
