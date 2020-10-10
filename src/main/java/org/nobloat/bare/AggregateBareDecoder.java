package org.nobloat.bare;

import java.io.IOException;
import java.io.InputStream;
import java.io.NotSerializableException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class AggregateBareDecoder {

    private InputStream is;
    private PrimitiveBareDecoder primitiveDecoder;

    public AggregateBareDecoder(InputStream inputStream) {
        this.is = inputStream;
        this.primitiveDecoder = new PrimitiveBareDecoder(is);
    }

    public <T>  Optional<T> optional(Class<T> c) throws IOException {
        if (primitiveDecoder.bool()) {
            return Optional.of(readType(c));
        }
        return Optional.empty();
    }

    public <T> List<T> values(Class<T> c) throws IOException {
        var length = primitiveDecoder.variadicUint();
        if (length.equals(BigInteger.ZERO)) {
            return List.of();
        }
        var result = new ArrayList<T>();
        while(! length.equals(BigInteger.ZERO)) {
            result.add(readType(c));
            length = length.subtract(BigInteger.ONE);
        }
        return result;
    }

    public <T> List<T> values(Class<T> c, int length) throws IOException {
        //Creation of plain arrays out of generic types is not possible in Java
        var result = new ArrayList<T>(length);
        for(int i=0; i < length; i++) {
            result.add(readType(c));
        }
        return result;
    }

    public <K,V> Map<K,V> map(Class<K> key, Class<V> value) throws IOException {

        //TODO: check that K is a primitive type which is not data or data<length>

        var length = primitiveDecoder.variadicUint();
        var result = new HashMap<K,V>(length.intValue());
        while(! length.equals(BigInteger.ZERO)) {
            result.put(readType(key), readType(value));
            length = length.subtract(BigInteger.ONE);
        }
        return result;
    }

    public UnionType union(Class<?>... possibleTypes) throws IOException {
        int type = primitiveDecoder.variadicUint().intValue();
        if (type > possibleTypes.length) {
            throw new NotSerializableException("Unexpected union type: " + type);
        }
        return new UnionType(type, readType(possibleTypes[type]));
    }


    //TODO: optimize for slices -> check type only once
    @SuppressWarnings("unchecked")
    public <T> T readType(Class<T> c) throws IOException {
        switch (c.getName()) {
            case "java.lang.String":
                return (T) primitiveDecoder.string();
            case "java.lang.Long":
                return (T) Long.valueOf(primitiveDecoder.variadicInt());
            case "java.lang.Integer":
                return (T) Integer.valueOf(primitiveDecoder.i32());
            case "java.lang.BigInteger":
                return (T) primitiveDecoder.u64();
            case "java.lang.Short":
                return (T) Short.valueOf(primitiveDecoder.i16());
            case "java.lang.Boolean":
                return (T) Boolean.valueOf(primitiveDecoder.bool());
            case "java.lang.Byte":
                return (T) Byte.valueOf(primitiveDecoder.i8());
            case "java.lang.Float":
                return (T) Float.valueOf(primitiveDecoder.f32());
            case "java.lang.Double":
                return (T) Double.valueOf(primitiveDecoder.f64());
            default:
                throw new UnsupportedOperationException("readType not implemented for " + c.getName());
        }
    }

}
