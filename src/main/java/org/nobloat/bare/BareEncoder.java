package org.nobloat.bare;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class BareEncoder {


    /*
    public byte[] variadicInt(long value) {

    }

    public byte[] variadicUInt(BigInteger value) {

    }

    public long variadicInt() throws IOException {
        BigInteger r = variadicUint();
        if (r.testBit(0)) {
            return r.shiftRight(1).not().longValue();
        }
        return r.shiftRight(1).longValue();
    }

    public BigInteger variadicUint() throws IOException {
        BigInteger result = BigInteger.ZERO;
        int shift = 0;
        int b;
        do {
            b = is.readByte() & 0xff;
            if (b >= 0x80) {
                result = result.or(BigInteger.valueOf(b & 0x7F).shiftLeft(shift));
                shift += 7;
            } else {
                result = result.or(BigInteger.valueOf(b).shiftLeft(shift));
            }
        } while (b >= 0x80);
        return result;
    }*/

    public static Byte[] uVarint(long value) {
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
