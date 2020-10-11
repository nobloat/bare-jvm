package org.nobloat.bare;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class AggregateBareEncoder {

    private final OutputStream os;
    private final PrimitiveBareEncoder encoder;

    public AggregateBareEncoder(OutputStream os) {
        this(os,false);
    }

    public AggregateBareEncoder(OutputStream os, boolean verifyInput) {
        this.os = os;
        this.encoder = new PrimitiveBareEncoder(os,verifyInput);
    }


    public <T> void array(Array<T> values) throws IOException {
        for (var v : values.values) {
            encodeType(v);
        }
    }

    public <T> void slice(List<T> values) throws IOException {
        encoder.variadicUInt(values.size());
        for (var v : values) {
            encodeType(v);
        }
    }

    public <K,V> void map(Map<K,V> values) throws IOException {
        encoder.variadicUInt(values.size());
        for(var e : values.entrySet()) {
            encodeType(e.getKey());
            encodeType(e.getValue());
        }
    }

    public <T> void optional(Optional<T> value) throws IOException  {
        if (value.isPresent()) {
            encoder.bool(true);
            encodeType(value.get());
        } else {
            encoder.bool(false);
        }
    }

    public void union(Union value) throws IOException {
        encoder.variadicUInt(value.type);
        encodeType(value.value);
    }

    //TODO: implement for all aggregate and primitive types
    public <T> void encodeType(T value) throws IOException {
        if (String.class.equals(value.getClass())) {
            encoder.string((String) value);
        } else if (Boolean.class.equals(value.getClass())) {
            encoder.bool((boolean) value);
        } else if (Byte.class.equals(value.getClass())) {
            encoder.u8((byte)value);
        } else {
            throw new UnsupportedOperationException("Encoding for type not implemented: " + value.getClass().getName());
        }
    }
}
