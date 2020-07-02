package org.nobloat.bare;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

public class BareDecoder {

    private final DataInputStream is;

    public BareDecoder(InputStream is) {
        this.is = new DataInputStream(is);
    }

    public byte u8() throws IOException {
        return is.readByte();
    }

    public int u16() throws IOException {
        int byte1 = is.readByte() & 0xff;
        int byte2 = is.readByte() & 0xff;
        return (byte1 | byte2 << 8);
    }

    public Integer u32() throws IOException {
        int byte1 = is.readByte() & 0xff;
        int byte2 = is.readByte() & 0xff;
        int byte3 = is.readByte() & 0xff;
        int byte4 = is.readByte() & 0xff;
        return (byte4 << 24) | (byte3 << 16) |
                (byte2 << 8) | (byte1);
    }

    public BigInteger u64() throws IOException {
        return BigInteger.valueOf(u32()).shiftLeft(32).or(BigInteger.valueOf(u32()));
    }

    public byte i8() throws IOException {
        return is.readByte();
    }

    public short i16() throws IOException {
        int byte1 = is.readByte() & 0xff;
        int byte2 = is.readByte() & 0xff;
        return (short) (byte2 << 8 | byte1);
    }

    public int i32() throws IOException {
        int byte1 = is.readByte() & 0xff;
        int byte2 = is.readByte() & 0xff;
        int byte3 = is.readByte() & 0xff;
        int byte4 = is.readByte() & 0xff;
        return (byte4 << 24) | (byte3 << 16) |
                (byte2 << 8) | (byte1);
    }

    public long i64() throws IOException {
        return ((is.readByte()) << 56) | ((is.readByte()) << 48) |
                ((is.readByte()) << 40) | ((is.readByte()) << 32) |
                ((is.readByte()) << 24) | ((is.readByte()) << 16) |
                ((is.readByte()) << 8) | (is.readByte());
    }

    public float f32() throws IOException {
        return Float.intBitsToFloat(u32());
    }

    public double f64() throws IOException {
        return Double.longBitsToDouble(i64());
    }

    public boolean bool() throws IOException {
        return is.readByte() != 0;
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
    }

    public String string() throws IOException {
        return new String(data(), StandardCharsets.UTF_8);
    }

    public byte[] data() throws IOException {
        int length = u32();
        byte[] bytes = new byte[length];
        is.read(bytes);
        return bytes;
    }

}
