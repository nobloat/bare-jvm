package org.nobloat.bare;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class AggregateBareEncoder extends PrimitiveBareEncoder {

    public AggregateBareEncoder(OutputStream os) {
        super(os);
    }

    public <T> void optional(Optional<T> value, EncodeFunction<T> encoder) throws IOException, BareException {
        if (value.isPresent()) {
            bool(true);
            encoder.apply(value.get());
        } else {
            bool(false);
        }
    }

    public <T> void array(T[] value, EncodeFunction<T> itemEncoder) throws IOException, BareException {
        for (var item : value) {
            itemEncoder.apply(item);
        }
    }

    public void array(int[] value, EncodeFunction<Integer> itemEncoder) throws IOException, BareException {
        for (var item : value) {
            itemEncoder.apply(item);
        }
    }

    public void array(long[] value, EncodeFunction<Long> itemEncoder) throws IOException, BareException {
        for (var item : value) {
            itemEncoder.apply(item);
        }
    }

    public void array(short[] value, EncodeFunction<Short> itemEncoder) throws IOException, BareException {
        for (var item : value) {
            itemEncoder.apply(item);
        }
    }

    public void array(boolean[] value, EncodeFunction<Boolean> itemEncoder) throws IOException, BareException {
        for (var item : value) {
            itemEncoder.apply(item);
        }
    }

    public void array(float[] value, EncodeFunction<Float> itemEncoder) throws IOException, BareException {
        for (var item : value) {
            itemEncoder.apply(item);
        }
    }

    public void array(double[] value, EncodeFunction<Double> itemEncoder) throws IOException, BareException {
        for (var item : value) {
            itemEncoder.apply(item);
        }
    }

    public void array(byte[] value) throws IOException {
        for (var item : value) {
            u8(item);
        }
    }

    public <T> void slice(List<T> value, EncodeFunction<T> itemEncoder) throws IOException, BareException {
        variadicUInt(value.size());
        for (var item : value) {
            itemEncoder.apply(item);
        }
    }

    public <K,V> void map(Map<K,V> values, EncodeFunction<K> keyEncoder, EncodeFunction<V> valueEncoder) throws IOException, BareException {
        variadicUInt(values.size());
        for (var entry : values.entrySet()) {
            keyEncoder.apply(entry.getKey());
            valueEncoder.apply(entry.getValue());
        }
    }

    public void union(Union value, Map<Integer, EncodeFunction> encodeFunctions) throws IOException, BareException {
        var encoder = encodeFunctions.get((value.type()));
        if (encoder == null) {
            throw new BareException("Unmapped union type: " + value.type());
        }
        variadicUInt(value.type());
        encoder.apply(value.value);
    }

    @FunctionalInterface
    public interface EncodeFunction<T> {
        void apply(T value) throws IOException, BareException;
    }
}
