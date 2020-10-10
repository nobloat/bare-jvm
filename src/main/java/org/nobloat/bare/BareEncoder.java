package org.nobloat.bare;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class BareEncoder {

    public static Byte[] uVarInt(long value) {
        int i = 0;
        List<Byte> bytes = new ArrayList<>(2);
        while (value >= 0x80) {
            bytes.add((byte) (value | 0x80));
            value >>= 7;
            i++;
        }
        bytes.add((byte) value);
        return bytes.toArray(Byte[]::new);
    }
}
