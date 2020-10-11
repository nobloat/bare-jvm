package org.nobloat.bare;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class AggregateBareDecoder {

    public static final List<String> INTEGER_TYPES = List.of(new String[]{"java.lang.Long", "java.lang.Integer", "java.lang.BigInteger", "java.lang.Short"});
    public static final List<String> PRIMITIVE_TYPES = List.of(new String[]{"java.lang.String", "java.lang.Boolean", "java.lang.Byte", "java.lang.Float", "java.lang.Double"});

    private InputStream is;
    private PrimitiveBareDecoder primitiveDecoder;

    public AggregateBareDecoder(InputStream inputStream) {
        this.is = inputStream;
        this.primitiveDecoder = new PrimitiveBareDecoder(is);
    }

    public <T> Optional<T> optional(Class<T> c) throws IOException {
        if (primitiveDecoder.bool()) {
            return Optional.of(readPrimitiveType(c));
        }
        return Optional.empty();
    }

    public <T> List<T> values(Class<T> c) throws IOException, ReflectiveOperationException {
        //TODO: add max length
        var length = primitiveDecoder.variadicUint();
        if (length.equals(BigInteger.ZERO)) {
            return List.of();
        }
        var result = new ArrayList<T>();
        while (!length.equals(BigInteger.ZERO)) {
            result.add(readType(c));
            length = length.subtract(BigInteger.ONE);
        }
        return result;
    }

    public <T> Array<T> values(Class<T> c, int length) throws IOException, ReflectiveOperationException {
        var result = new Array<T>(length);
        for (int i = 0; i < length; i++) {
            result.values.add(readType(c));
        }
        return result;
    }

    public <K, V> Map<K, V> map(Class<K> key, Class<V> value) throws IOException, ReflectiveOperationException {
        //TODO: add max length
        assert PRIMITIVE_TYPES.contains(key.getName());

        var length = primitiveDecoder.variadicUint();
        var result = new HashMap<K, V>(length.intValue());
        while (!length.equals(BigInteger.ZERO)) {
            result.put(readPrimitiveType(key), readType(value));
            length = length.subtract(BigInteger.ONE);
        }
        return result;
    }

    public Union union(Class<?>... possibleTypes) throws IOException, ReflectiveOperationException {
        return union(new Union(possibleTypes).types);
    }

    public Union union(Map<Long, Class<?>> allowedTypes) throws IOException, ReflectiveOperationException {
        var union = new Union(allowedTypes);
        int type = primitiveDecoder.variadicUint().intValue();
        var clazz = union.type(type);
        union.set(type, readType(clazz));
        return union;
    }

    public <T> T enumeration(Class<T> c) throws IOException, ReflectiveOperationException {
        //TODO: maybe fallback to ordinal? -> I like it more explicit, therefore no fallback
        var field = c.getField("value");
        var enumValue = readIntegerType(field);

        //TODO: enum creation not working
        return c.getConstructor(field.getType()).newInstance(enumValue);
    }

    @SuppressWarnings("unchecked")
    public <T> T  struct(Class<T> c) throws ReflectiveOperationException, IOException {
        var fields = c.getFields();
        var result = c.getConstructor().newInstance();
        for(var f : fields) {
            f.setAccessible(true);
            if (INTEGER_TYPES.contains(f.getType().getName())) {
                f.set(result, readIntegerType(f));
            } else if (PRIMITIVE_TYPES.contains(f.getType().getName())) {
                f.set(result, readPrimitiveType(f.getType()));
            } else if(f.getType().getName().equals("org.nobloat.bare.Array")) {
                var array = (Array<T>)f.get(result);
                ParameterizedType type = (ParameterizedType)f.getGenericType();
                var elementType =  type.getActualTypeArguments()[0];
                f.set(result, values((Class<?>)elementType, array.size));
            } else if(f.getType().getName().equals("java.util.List")) {
                ParameterizedType type = (ParameterizedType)f.getGenericType();
                var elementType =  type.getActualTypeArguments()[0];
                f.set(result, values((Class<?>) elementType));
            } else if(f.getType().getName().equals("java.util.Map")) {
                ParameterizedType type = (ParameterizedType)f.getGenericType();
                var keyType =  type.getActualTypeArguments()[0];
                var valueType =  type.getActualTypeArguments()[0];
                f.set(result, map((Class<?>)keyType, (Class<?>) valueType));
            } else{
                f.set(result, readType(f.getType()));
            }
        }
        return result;
    }


    //TODO: optimize for slices -> check type only once
    @SuppressWarnings("unchecked")
    public <T> T readPrimitiveType(Class<?> c) throws IOException {
        switch (c.getName()) {
            case "java.lang.Boolean":
                return (T) Boolean.valueOf(primitiveDecoder.bool());
            case "java.lang.Byte":
                return (T) Byte.valueOf(primitiveDecoder.i8());
            case "java.lang.Float":
                return (T) Float.valueOf(primitiveDecoder.f32());
            case "java.lang.Double":
                return (T) Double.valueOf(primitiveDecoder.f64());
            case "java.lang.String":
                return (T) primitiveDecoder.string();
            default:
                throw new UnsupportedOperationException("readType not implemented for " + c.getName());
        }
    }

    public <T> T readIntegerType(Field f) throws IOException {
        var annotation = f.getAnnotation(Int.class);
        if (annotation == null) {
           throw new UnsupportedEncodingException("Missing @Int type annotation on number field: " + f.getName());
        }
        switch (annotation.value()) {
            case i8: return (T) Byte.valueOf(primitiveDecoder.i8());
            case u8: return (T) Byte.valueOf(primitiveDecoder.u8());
            case i16: return (T) Short.valueOf(primitiveDecoder.i16());
            case u16: return (T) Integer.valueOf(primitiveDecoder.u16());
            case i32: return (T) Integer.valueOf(primitiveDecoder.i32());
            case u32: return (T) Integer.valueOf(primitiveDecoder.u32());
            case u64: return (T) primitiveDecoder.u64();
            case i64: return (T) Long.valueOf(primitiveDecoder.i64());
            default:
                throw new UnsupportedEncodingException("Unknown Int type: " + annotation.value());
        }
    }

    public <T> T readType(Class<T> c) throws IOException, ReflectiveOperationException {
        try {
            if (c.isEnum()) {
                return enumeration(c);
            }
            return readPrimitiveType(c);
        } catch (UnsupportedOperationException e) {
            return struct(c);
        }
    }


}
