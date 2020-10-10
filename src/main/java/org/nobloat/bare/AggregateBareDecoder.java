package org.nobloat.bare;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AggregateBareDecoder {

    private InputStream is;
    private BareDecoder primitiveDecoder;

    public AggregateBareDecoder(InputStream inputStream) {
        this.is = inputStream;
        this.primitiveDecoder = new BareDecoder(is);
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
            default:
                throw new UnsupportedOperationException("readType not implemented for " + c.getName());
        }
    }

}
