package org.nobloat.bare.dsl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.nobloat.bare.dsl.Ast.TypeKind.*;

public class AstParser {

    private final static Pattern USER_TYPE_NAME_RE = Pattern.compile("[A-Z][A-Za-z0-9]*");
    private final static Pattern USER_ENUM_NAME_RE = Pattern.compile("[A-Z][A-Za-z0-9]*");
    private final static Pattern FIELD_NAME_RE = Pattern.compile("[a-z][A-Za-z0-9]*");
    private final static Pattern ENUM_VALUE_RE = Pattern.compile("[A-Z][A-Z0-9_]*");

    private final Lexer lexer;

    public AstParser(Lexer lexer) {
        this.lexer = lexer;
    }

    public List<Ast.Type> parse() throws IOException {
        List<Ast.Type> schemaTypes = new ArrayList<>();

        while (true) {
            Lexer.Token token = lexer.nextToken();

            if (token.type == Lexer.Token.Type.EOF) {
                break;
            }

            Ast.Type schemaType = parseSchemaType(token);

            schemaTypes.add(schemaType);
        }

        return schemaTypes;
    }

    private Ast.Type parseSchemaType(Lexer.Token token) throws IOException {
        switch (token.type) {
            case TYPE:
                return parseUserType();
            case ENUM:
                return parseUserEnum();
        }

        throw new IllegalStateException(String.format("Unexpected token %s. Required: 'type' or 'enum'", token.type));
    }

    private  Ast.UserDefinedType parseUserType() throws IOException {
        Lexer.Token nameToken = lexer.nextToken();

        if (nameToken.type != Lexer.Token.Type.NAME) {
            throw new IllegalStateException(String.format("Unexpected token %s. Required: type 'name'", nameToken.type));
        }

        Matcher matcher = USER_TYPE_NAME_RE.matcher(nameToken.value);
        if (!matcher.matches()) {
            throw new IllegalStateException(String.format("Invalid name for user type %s. Must match: %s", nameToken.type, USER_TYPE_NAME_RE.pattern()));
        }

        Ast.Type type = parseType();

        return new Ast.UserDefinedType(nameToken.value, type);
    }

    private Ast.Type parseType() throws IOException {
        Lexer.Token token = lexer.nextToken();

        switch (token.type) {
            case UINT:
                return new Ast.PrimitiveType(UINT);
            case U8:
                return new Ast.PrimitiveType(U8);
            case U16:
                return new Ast.PrimitiveType(U16);
            case U32:
                return new Ast.PrimitiveType(U32);
            case U64:
                return new Ast.PrimitiveType(U64);
            case INT:
                return new Ast.PrimitiveType(INT);
            case I8:
                return new Ast.PrimitiveType(I8);
            case I16:
                return new Ast.PrimitiveType(I16);
            case I32:
                return new Ast.PrimitiveType(I32);
            case I64:
                return new Ast.PrimitiveType(I64);
            case F32:
                return new Ast.PrimitiveType(F32);
            case F64:
                return new Ast.PrimitiveType(F64);
            case BOOL:
                return new Ast.PrimitiveType(Bool);
            case STRING:
                return new Ast.PrimitiveType(STRING);
            case VOID:
                return new Ast.PrimitiveType(Void);
            case OPTIONAL:
                return parseOptionalType();
            case DATA:
                return parseDataType();
            case MAP:
                return parseMapType();
            case L_BRACKET:
                return parseArrayType();
            case L_PAREN:
                return parseUnionType();
            case L_BRACE:
                return parseStructType();
            case NAME:
                return new Ast.NamedUserType(token.value);
        }

        throw new IllegalStateException(String.format("Unexpected token %s. Required: type ", token.type));
    }

    private Ast.OptionalType parseOptionalType() throws IOException {
        Lexer.Token leftAngle = lexer.nextToken();

        if (leftAngle.type != Lexer.Token.Type.L_ANGLE) {
            throw new IllegalStateException(String.format("Unexpected token %s. Required: '<'", leftAngle.type));
        }

        Ast.Type subType = parseType();

        Lexer.Token rightAngle = lexer.nextToken();

        if (rightAngle.type != Lexer.Token.Type.R_ANGLE) {
            throw new IllegalStateException(String.format("Unexpected token %s. Required: '>'", rightAngle.type));
        }

        return new Ast.OptionalType(subType);
    }

    private Ast.DataType parseDataType() throws IOException {
        Lexer.Token leftAngle = lexer.nextToken();

        if (leftAngle.type != Lexer.Token.Type.L_ANGLE) {
            lexer.pushback(leftAngle);
            return new Ast.DataType(0);
        }

        Lexer.Token number = lexer.nextToken();

        if (number.type != Lexer.Token.Type.NUMBER) {
            throw new IllegalStateException(String.format("Unexpected token %s. Required: 'integer'", number.type));
        }

        int length = Integer.parseInt(number.value);

        Lexer.Token rightAngle = lexer.nextToken();

        if (rightAngle.type != Lexer.Token.Type.R_ANGLE) {
            throw new IllegalStateException(String.format("Unexpected token %s. Required: '>'", rightAngle.type));
        }

        return new Ast.DataType(length);
    }

    private Ast.MapType parseMapType() throws IOException {

        Lexer.Token leftBracket = lexer.nextToken();

        if (leftBracket.type != Lexer.Token.Type.L_BRACKET) {
            throw new IllegalStateException(String.format("Unexpected token %s. Required: '['", leftBracket.type));
        }

        Ast.Type keyType = parseType();

        Lexer.Token rightBracket = lexer.nextToken();

        if (rightBracket.type != Lexer.Token.Type.R_BRACKET) {
            throw new IllegalStateException(String.format("Unexpected token %s. Required: ']'", rightBracket.type));
        }

        Ast.Type valueType = parseType();

        return new Ast.MapType(keyType, valueType);
    }

    private Ast.ArrayType parseArrayType() throws IOException {
        Lexer.Token token = lexer.nextToken();

        long length = 0;
        switch (token.type) {
            case NUMBER:
                length = Long.parseLong(token.value);

                Lexer.Token rightBracket = lexer.nextToken();

                if (rightBracket.type != Lexer.Token.Type.R_BRACKET) {
                    throw new IllegalStateException(String.format("Unexpected token %s. Required: ']'", token.type));
                }
                break;
            case R_BRACKET:
                break;
            default:
                throw new IllegalStateException(String.format("Unexpected token %s. Required: ']'", token.type));
        }


        Ast.Type type = parseType();

        return new Ast.ArrayType(type, length);
    }

    private Ast.UnionType parseUnionType() throws IOException {

        List<Ast.UnionVariant> unionVariants = new ArrayList<>();
        int tag = 0;

        while (true) {
            Ast.Type type = parseType();

            Lexer.Token token = lexer.nextToken();
            if (token.type == Lexer.Token.Type.EQUAL) {

                Lexer.Token number = lexer.nextToken();
                if (number.type != Lexer.Token.Type.NUMBER) {
                    throw new IllegalStateException(String.format("Unexpected token %s. Required: 'number'", token.type));
                }

                tag = Integer.parseInt(number.value);
            } else {
                lexer.pushback(token);
            }

            unionVariants.add(new Ast.UnionVariant(type, tag));
            tag++;

            Lexer.Token nextToken = lexer.nextToken();

            if (nextToken.type == Lexer.Token.Type.PIPE) {
                continue;
            } else if (nextToken.type == Lexer.Token.Type.R_PAREN) {
                break;
            } else {
                throw new IllegalStateException(String.format("Unexpected token %s. Required: '|' or ')'", token.type));
            }
        }

        return new Ast.UnionType(unionVariants);
    }

    private Ast.StructType parseStructType() throws IOException {
        List<Ast.StructField> fields = new ArrayList<>();

        while (true) {
            Lexer.Token token = lexer.nextToken();

            if (token.type == Lexer.Token.Type.R_BRACE) {
                break;
            }
            if (token.type != Lexer.Token.Type.NAME) {
                throw new IllegalStateException(String.format("Unexpected token %s. Required: field name", token.type));
            }

            String name = token.value;
            Matcher matcher = FIELD_NAME_RE.matcher(name);
            if (!matcher.matches()) {
                throw new IllegalStateException(String.format("Invalid name for field %s. Must match: %s", name, FIELD_NAME_RE.pattern()));
            }

            Lexer.Token colon = lexer.nextToken();
            if (colon.type != Lexer.Token.Type.COLON) {
                throw new IllegalStateException(String.format("Unexpected token %s. Required: ':'", colon.type));
            }

            Ast.Type type = parseType();

            fields.add(new Ast.StructField(name, type));
        }

        return new Ast.StructType(fields);
    }

    private Ast.UserDefinedEnum parseUserEnum() throws IOException {

        Lexer.Token name = lexer.nextToken();
        if (name.type != Lexer.Token.Type.NAME) {
            throw new IllegalStateException(String.format("Unexpected token %s. Required: enum name", name.type));
        }

        Lexer.Token token = lexer.nextToken();

        Ast.TypeKind kind;
        switch (token.type) {
            case U8:
                kind = U8;
                break;
            case U16:
                kind = U16;
                break;
            case U32:
                kind = U32;
                break;
            case U64:
                kind = U64;
                break;
            default:
                kind = UINT;
                lexer.pushback(token);
        }

        Lexer.Token leftBrace = lexer.nextToken();
        if (leftBrace.type != Lexer.Token.Type.L_BRACE) {
            throw new IllegalStateException(String.format("Unexpected token %s. Required: '{'", leftBrace.type));
        }

        int value = 0;
        List<Ast.EnumValue> values = new ArrayList<>();
        while (true) {
            Lexer.Token valueNameToken = lexer.nextToken();
            if (valueNameToken.type != Lexer.Token.Type.NAME) {
                throw new IllegalStateException(String.format("Unexpected token %s. Required: value name", valueNameToken.type));
            }

            String valueName = valueNameToken.value;
            Matcher matcher = ENUM_VALUE_RE.matcher(valueName);
            if (!matcher.matches()) {
                throw new IllegalStateException(String.format("Invalid name for enum value %s. Must match: %s", valueNameToken.type, ENUM_VALUE_RE.pattern()));
            }

            int evValue;
            Lexer.Token nextToken = lexer.nextToken();
            if (nextToken.type == Lexer.Token.Type.EQUAL) {
                Lexer.Token number = lexer.nextToken();
                if (number.type != Lexer.Token.Type.NUMBER) {
                    throw new IllegalStateException(String.format("Unexpected token %s. Required: number", number.type));
                }

                value = Integer.parseInt(number.value);
                evValue = value;
            } else {
                evValue = value;
                value += 1;
                lexer.pushback(nextToken);
            }

            values.add(new Ast.EnumValue(valueName, evValue));

            Lexer.Token nextToken2 = lexer.nextToken();

            if (nextToken2.type == Lexer.Token.Type.R_BRACE) {
                break;
            } else if (nextToken2.type == Lexer.Token.Type.NAME) {
                lexer.pushback(nextToken2);
            } else {
                throw new IllegalStateException(String.format("Unexpected token %s. Required: 'value name'", nextToken2.type));
            }
        }

        Matcher matcher = USER_ENUM_NAME_RE.matcher(name.value);
        if (!matcher.matches()) {
            throw new IllegalStateException(String.format("Invalid name for user enum %s. Must match: %s", name.type, USER_ENUM_NAME_RE.pattern()));
        }

        return new Ast.UserDefinedEnum(name.value, kind, values);
    }
}
