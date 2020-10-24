package org.nobloat.bare;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class AggregateBareDecoder extends PrimitiveBareDecoder {

    public int MaxMapLength = 1000000000;

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

    public <T> List<T> array(int count, DecodeFunction<T> itemDecoder) throws IOException, BareException {
        var result = new ArrayList<T>(count);
        for (int i=0; i < count; i++) {
            result.add(itemDecoder.apply(this));
        }
        return result;
    }

    public <T> List<T> slice(DecodeFunction<T> itemDecoder) throws IOException, BareException {
        var length = variadicUint().intValue();
        if (length > MaxMapLength) {
            throw new BareException(String.format("Decoding slice with entries %d > %d max length", length, MaxSliceLength));
        }
        return array(length, itemDecoder);
    }

    public <K,V> Map<K,V> map(DecodeFunction<K> keyDecoder, DecodeFunction<V> valueDecoder) throws IOException, BareException {
        var length = variadicUint().intValue();
        if (length > MaxMapLength) {
            throw new BareException(String.format("Decoding map with entries %d > %d max length", length, MaxSliceLength));
        }
        var result = new HashMap<K,V>();
        for (int i=0; i < length; i++) {
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
