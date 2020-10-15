package org.nobloat.bare;

import javax.swing.text.html.Option;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class AggregateBareDecoder extends PrimitiveBareDecoder {

    public static final List<String> INTEGER_TYPES = List.of(new String[]{"java.lang.Long", "java.lang.Integer", "java.lang.BigInteger", "java.lang.Short"});
    public static final List<String> PRIMITIVE_TYPES = List.of(new String[]{"java.lang.String", "java.lang.Boolean", "java.lang.Byte", "java.lang.Float", "java.lang.Double"});

    public AggregateBareDecoder(InputStream inputStream) {
        super(inputStream);
    }

    public <T> Optional<T> optional(DecodeFunction<T> itemDecoder) throws IOException, BareException {
        boolean exists = bool();
        if (exists) {
            return Optional.of(itemDecoder.apply(this));
        }
        return Optional.empty();
    }

    public <T> Array<T> array(long count, DecodeFunction<T> itemDecoder) throws IOException, BareException {
        var result = new Array<T>((int)count);
        for (long i=0; i < count; i++) {
            result.values.add(itemDecoder.apply(this));
        }
        return result;
    }

    public <T> List<T> slice(DecodeFunction<T> itemDecoder) throws IOException, BareException {
        var count = variadicUint().intValue();
        return array(count, itemDecoder).values;
    }

    public <K,V> Map<K,V> map(DecodeFunction<K> keyDecoder, DecodeFunction<V> valueDecoder) throws IOException, BareException {
        var count = variadicUint().longValue();
        var result = new HashMap<K,V>();
        for (long i=0; i < count; i++) {
            result.put(keyDecoder.apply(this), valueDecoder.apply(this));
        }
        return result;
    }

    public Union union(Map<Integer, DecodeFunction> decodeFunctions) throws IOException, BareException {
        int type = variadicUint().intValue();
        var decoder = decodeFunctions.get(type);

        if (decoder == null) {
            throw new BareException("Unknown union type: " + type);
        }
        return new Union(type, decoder.apply(this));
    }

    @FunctionalInterface
    public interface DecodeFunction<T> {
        T apply(AggregateBareDecoder decoder) throws IOException, BareException;
    }

}
