package org.nobloat.bare;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ReflectiveBareDecoder extends AggregateBareDecoder {

    public static final List<String> INTEGER_TYPES = List.of(new String[]{"java.lang.Long", "java.lang.Integer", "java.lang.BigInteger", "java.lang.Short"});
    public static final List<String> PRIMITIVE_TYPES = List.of(new String[]{"java.lang.String", "java.lang.Boolean", "java.lang.Byte", "java.lang.Float", "java.lang.Double"});

    public ReflectiveBareDecoder(InputStream inputStream) {
        super(inputStream);
    }

    public <T> Optional<T> optional(Class<T> c) throws IOException, BareException {
        if (bool()) {
            return Optional.of(readPrimitiveType(c));
        }
        return Optional.empty();
    }

    public <T> List<T> slice(Class<T> c) throws IOException, ReflectiveOperationException, BareException {
        var length = variadicUint().intValue();
        if (length > MaxSliceLength) {
            throw new BareException(String.format("Decoding slice with entries %d > %d max length", length, MaxSliceLength));
        }
        return array(c, length);
    }

    public <T> List<T> array(Class<T> c, int length) throws IOException, ReflectiveOperationException, BareException {
        var result = new ArrayList<T>(length);
        for (int i = 0; i < length; i++) {
            result.add(readType(c));
        }
        return result;
    }

    public <K, V> Map<K, V> map(Class<K> key, Class<V> value) throws IOException, ReflectiveOperationException, BareException {
        assert PRIMITIVE_TYPES.contains(key.getName());

        var length = variadicUint().intValue();

        if (length > MaxMapLength) {
            throw new BareException(String.format("Decoding map with entries %d > %d max length", length, MaxSliceLength));
        }

        var result = new HashMap<K, V>(length);
        for(int i=0; i < length; i++) {
            result.put(readPrimitiveType(key), readType(value));
        }
        return result;
    }


    public Union union(Class<?>... possibleTypes) throws IOException, ReflectiveOperationException, BareException {
        var union = new Union(possibleTypes);
        int type = variadicUint().intValue();
        var clazz = union.type(type);
        union.set(type, readType(clazz));
        return union;
    }

    public <T> T enumeration(Class<T> c) throws IOException, ReflectiveOperationException {
        //TODO: maybe fallback to ordinal? -> I like it more explicit, therefore no fallback
        var field = c.getField("value");
        var enumValue = variadicUint().longValue();

        return null;
    }

    @SuppressWarnings("unchecked")
    public <T> T  struct(Class<T> c) throws ReflectiveOperationException, IOException, BareException {
        var fields = c.getFields();
        var result = c.getConstructor().newInstance();
        for(var f : fields) {
            f.setAccessible(true);
            if (INTEGER_TYPES.contains(f.getType().getName())) {
                f.set(result, readIntegerType(f));
            } else if (PRIMITIVE_TYPES.contains(f.getType().getName())) {
                f.set(result, readPrimitiveType(f.getType()));
            } else if(f.getType().isArray()) {
                var array = (T[])f.get(result);
                f.set(result, array(f.getType().getComponentType(), array.length).toArray((Object[])Array.newInstance(f.getType().getComponentType(), array.length)));
            } else if(f.getType().getName().equals("java.util.List")) {
                ParameterizedType type = (ParameterizedType)f.getGenericType();
                var elementType =  type.getActualTypeArguments()[0];
                f.set(result, slice((Class<?>) elementType));
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

    @SuppressWarnings("unchecked")
    public <T> T readPrimitiveType(Class<?> c) throws IOException, BareException {
        switch (c.getName()) {
            case "java.lang.Boolean":
                return (T) Boolean.valueOf(bool());
            case "java.lang.Byte":
                return (T) Byte.valueOf(i8());
            case "java.lang.Float":
                return (T) Float.valueOf(f32());
            case "java.lang.Double":
                return (T) Double.valueOf(f64());
            case "java.lang.String":
                return (T) string();
            default:
                throw new UnsupportedOperationException("readType not implemented for " + c.getName());
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T readIntegerType(Field f) throws IOException {
        var annotation = f.getAnnotation(Int.class);
        if (annotation == null) {
            throw new UnsupportedEncodingException("Missing @Int type annotation on number field: " + f.getName());
        }
        switch (annotation.value()) {
            case i8: return (T) Byte.valueOf(i8());
            case u8: return (T) Byte.valueOf(u8());
            case i16: return (T) Short.valueOf(i16());
            case u16: return (T) Integer.valueOf(u16());
            case i32: return (T) Integer.valueOf(i32());
            case u32: return (T) Long.valueOf(u32());
            case u64: return (T) u64();
            case i64: return (T) Long.valueOf(i64());
            case i: return (T) Long.valueOf(variadicInt());
            case ui: return (T) variadicUint();
            default:
                throw new UnsupportedEncodingException("Unknown Int type: " + annotation.value());
        }
    }

    public <T> T readType(Class<T> c) throws IOException, ReflectiveOperationException, BareException {
        try {
            if (c.isEnum()) {
                return enumeration(c);
            }
            return readPrimitiveType(c);
        } catch (UnsupportedOperationException | BareException e) {
            return struct(c);
        }
    }
}
