package org.nobloat.bare.dsl;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.nobloat.bare.dsl.Lexer.Token.Type.*;

public class LexerTest {

    @Test
    public void shouldParseSimpleField() throws Exception {
        String input = "name: string";

        try (InputStream is = toStream(input);
             Scanner scanner = new Scanner(is)) {
            Lexer lexer = new Lexer(scanner);

            assertTypeAndValue(lexer, NAME, "name");
            assertType(lexer, COLON);

            assertType(lexer, STRING);

            assertType(lexer, EOF);
        }
    }

    @Test
    public void shouldParseMap() throws Exception {
        String input = "metadata: map[string]data";

        try (InputStream is = toStream(input);
             Scanner scanner = new Scanner(is)) {
            Lexer lexer = new Lexer(scanner);

            assertTypeAndValue(lexer, NAME, "metadata");
            assertType(lexer, COLON);

            assertType(lexer, MAP);
            assertType(lexer, L_BRACKET);
            assertType(lexer, STRING);
            assertType(lexer, R_BRACKET);
            assertType(lexer, DATA);

            assertType(lexer, EOF);
        }
    }

    @Test
    public void shouldParseArray() throws Exception {
        String input = "address: [4]string";

        try (InputStream is = toStream(input);
             Scanner scanner = new Scanner(is)) {
            Lexer lexer = new Lexer(scanner);

            assertTypeAndValue(lexer, NAME, "address");
            assertType(lexer, COLON);

            assertType(lexer, L_BRACKET);
            assertTypeAndValue(lexer, NUMBER, "4");
            assertType(lexer, R_BRACKET);
            assertType(lexer, STRING);

            assertType(lexer, EOF);
        }
    }

    @Test
    public void shouldParseOptional() throws Exception {
        String input = "publicKey: optional<PublicKey>";

        try (InputStream is = toStream(input);
             Scanner scanner = new Scanner(is)) {
            Lexer lexer = new Lexer(scanner);

            assertTypeAndValue(lexer, NAME, "publicKey");
            assertType(lexer, COLON);

            assertType(lexer, OPTIONAL);
            assertType(lexer, L_ANGLE);
            assertTypeAndValue(lexer, NAME, "PublicKey");
            assertType(lexer, R_ANGLE);

            assertType(lexer, EOF);
        }
    }

    @Test
    public void shouldParseArrayWithAnonymousType() throws Exception {
        String input = "orders: []{\n" +
                "    orderId: i64\n" +
                "    quantity: i32\n" +
                "  }";

        try (InputStream is = toStream(input);
             Scanner scanner = new Scanner(is)) {
            Lexer lexer = new Lexer(scanner);

            assertTypeAndValue(lexer, NAME, "orders");
            assertType(lexer, COLON);
            assertType(lexer, L_BRACKET);
            assertType(lexer, R_BRACKET);
            assertType(lexer, L_BRACE);

            assertTypeAndValue(lexer, NAME, "orderId");
            assertType(lexer, COLON);
            assertType(lexer, I64);

            assertTypeAndValue(lexer, NAME, "quantity");
            assertType(lexer, COLON);
            assertType(lexer, I32);

            assertType(lexer, R_BRACE);

            assertType(lexer, EOF);
        }
    }

    @Test
    public void shouldParseUnionType() throws Exception {
        String input = "type Person (Customer | Employee)";

        try (InputStream is = toStream(input);
             Scanner scanner = new Scanner(is)) {

            Lexer lexer = new Lexer(scanner);

            assertType(lexer, TYPE);
            assertTypeAndValue(lexer, NAME, "Person");

            assertType(lexer, L_PAREN);
            assertTypeAndValue(lexer, NAME, "Customer");

            assertType(lexer, PIPE);

            assertTypeAndValue(lexer, NAME, "Employee");
            assertType(lexer, R_PAREN);

            assertType(lexer, EOF);
        }
    }

    @Test
    public void shouldParseTypeWithLength() throws Exception {
        String input = "type PublicKey data<128>";

        try (InputStream is = toStream(input);
             Scanner scanner = new Scanner(is)) {

            Lexer lexer = new Lexer(scanner);

            assertType(lexer, TYPE);
            assertTypeAndValue(lexer, NAME, "PublicKey");

            assertType(lexer, DATA);
            assertType(lexer, L_ANGLE);
            assertTypeAndValue(lexer, NUMBER, "128");
            assertType(lexer, R_ANGLE);

            assertType(lexer, EOF);
        }
    }

    @Test
    public void shouldParseTypeWithComment() throws Exception {
        String input = "type Time string # ISO 8601";

        try (InputStream is = toStream(input);
             Scanner scanner = new Scanner(is)) {
            Lexer lexer = new Lexer(scanner);

            assertType(lexer, TYPE);
            assertTypeAndValue(lexer, NAME, "Time");
            assertType(lexer, STRING);

            assertTypeAndValue(lexer, COMMENT, " ISO 8601");

            assertType(lexer, EOF);
        }
    }

    @Test
    public void shouldParseEnum() throws Exception {
        String input = "enum Department {\n" +
                "  ACCOUNTING\n" +
                "  DEVELOPMENT\n" +
                "\n" +
                "  # Reserved for the CEO\n" +
                "  JSMITH = 99\n" +
                "}\n";

        try (InputStream is = toStream(input);
             Scanner scanner = new Scanner(is)) {
            Lexer lexer = new Lexer(scanner);

            assertType(lexer, ENUM);
            assertTypeAndValue(lexer, NAME, "Department");
            assertType(lexer, L_BRACE);

            assertTypeAndValue(lexer, NAME, "ACCOUNTING");
            assertTypeAndValue(lexer, NAME, "DEVELOPMENT");

            assertTypeAndValue(lexer, COMMENT, " Reserved for the CEO");

            assertTypeAndValue(lexer, NAME, "JSMITH");
            assertType(lexer, EQUAL);
            assertTypeAndValue(lexer, NUMBER, "99");

            assertType(lexer, R_BRACE);

            assertType(lexer, EOF);
        }
    }

    @Test
    public void shouldParseComplexType() throws Exception {
        String input = "type Customer {\n" +
                "  name: string\n" +
                "  address: Address\n" +
                "  metadata: map[string]data\n" +
                "}";

        try (InputStream is = toStream(input);
             Scanner scanner = new Scanner(is)) {
            Lexer lexer = new Lexer(scanner);

            assertType(lexer, TYPE);
            assertTypeAndValue(lexer, NAME, "Customer");
            assertType(lexer, L_BRACE);

            assertTypeAndValue(lexer, NAME, "name");
            assertType(lexer, COLON);
            assertType(lexer, STRING);

            assertTypeAndValue(lexer, NAME, "address");
            assertType(lexer, COLON);
            assertTypeAndValue(lexer, NAME, "Address");

            assertTypeAndValue(lexer, NAME, "metadata");
            assertType(lexer, COLON);
            assertType(lexer, MAP);
            assertType(lexer, L_BRACKET);
            assertType(lexer, STRING);
            assertType(lexer, R_BRACKET);
            assertType(lexer, DATA);

            assertType(lexer, R_BRACE);

            assertType(lexer, EOF);
        }
    }

    private InputStream toStream(String input) {
        return new ByteArrayInputStream(input.getBytes());
    }

    private void assertType(Lexer lexer, Lexer.Token.Type expectedType) throws IOException {
        Lexer.Token token = lexer.nextToken();
        assertEquals(expectedType, token.type);
    }

    private void assertTypeAndValue(Lexer lexer, Lexer.Token.Type expectedType, String expectedValue) throws IOException {
        Lexer.Token token = lexer.nextToken();
        assertEquals(expectedType, token.type);
        assertEquals(expectedValue, token.value);
    }
}
