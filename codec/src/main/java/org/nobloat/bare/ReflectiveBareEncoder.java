package org.nobloat.bare;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ReflectiveBareEncoder extends AggregateBareEncoder {

    public static final List<String> INTEGER_TYPES = List.of(new String[]{"java.lang.Long", "java.lang.Integer", "java.lang.BigInteger", "java.lang.Short"});
    public static final List<String> PRIMITIVE_TYPES = List.of(new String[]{"java.lang.String", "java.lang.Boolean", "java.lang.Byte", "java.lang.Float", "java.lang.Double"});


    public ReflectiveBareEncoder(OutputStream os) {
        super(os);
    }

    public void encode(Object o) throws IllegalAccessException, BareException, IOException, NoSuchFieldException {
        for (Field f : o.getClass().getFields()) {
            encodeField(o, f);
        }
    }

    void encodeField(Object o, Field f) throws IllegalAccessException, BareException, IOException, NoSuchFieldException {
        if (INTEGER_TYPES.contains(f.getType())) {
            encodeInteger(o, f);
        } else if (PRIMITIVE_TYPES.contains(f.getType())) {

        } else if (f.get(o) instanceof Optional) {

        } else if (f.get(o) instanceof Map<?,?>) {

        } else if (f.get(o) instanceof List<?>) {

        } else if (f.get(o) instanceof Union) {

        } else if (f.getType().isArray()) {
            encodeArray(f.get(o));
        } else if (f.getType().isEnum()) {
            encodeEnum(f.get(0));
        } else {
            //Struct
            encode(f.get(o));
        }
    }

    void encodeArray(Object array) throws IOException, BareException {
        var length = Array.getLength(array);
        variadicUInt(length);
        for (var o : (Object[])array) {
            //encodeField(o, );
        }
    }

    void encodeEnum(Object o) throws NoSuchFieldException, IllegalAccessException, IOException, BareException {
        var value = (int) o.getClass().getField("value").get(o);
        variadicUInt(value);
    }


    void encodeInteger(Object o, Field f) throws BareException, IllegalAccessException, IOException {
        var annotation = f.getAnnotation(Int.class);
        if (annotation == null) {
            throw new BareException("Missing @Int type annotation on number field: " + f.getName());
        }

        switch (annotation.value()) {
            case i8: i8(f.getByte(o));
            case u8: u8(f.getByte(o));
            case i16: i16(f.getShort(o));
            case u16: u16(f.getShort(o));
            case i32: i32(f.getInt(o));
            case u32: u32(f.getLong(o));
            case u64: u64((BigInteger)f.get(o));
            case i64: i64(f.getLong(o));
            case i: variadicInt(f.getLong(o));
            case ui: variadicUInt((BigInteger)f.get(o));
            default:
                throw new UnsupportedEncodingException("Unknown Int type: " + annotation.value());
        }
    }

}
