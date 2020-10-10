package org.nobloat.bare;

import java.io.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class PrimitiveBareEncoder {
    private final DataOutputStream os;
    private static final BigInteger UNSIGNED_LONG_MASK = BigInteger.ONE.shiftLeft(Long.SIZE).subtract(BigInteger.ONE);

    public PrimitiveBareEncoder(OutputStream os) {
        this.os = new DataOutputStream(os);
    }

    public void u8(byte b) throws IOException {
        os.writeByte(b);
    }

    public void u16(int b) throws IOException {
        os.write(new byte[]{(byte) (b & 0xFF), (byte) (b >> 8 & 0xFF)});
    }

    public void u32(Integer b) throws IOException {
        os.write(new byte[]{(byte) (b & 0xFF), (byte) (b >> 8 & 0xFF), (byte) (b >> 16 & 0xFF), (byte) (b >>24 & 0xFF)});
    }

    public void u64(BigInteger b) throws IOException {
        assert b.bitCount() <= 64;
        i64(b.and(UNSIGNED_LONG_MASK).longValue());
    }

    public void i8(byte b) throws IOException {
        u8(b);
    }

    public void i16(short b) throws IOException {
        u16(b);
    }

    public void i32(int b) throws IOException {
        u32(b);
    }

    public void i64(long b) throws IOException {
        os.write(new byte[]{(byte) (b & 0xFF), (byte) (b >> 8 & 0xFF), (byte) (b >> 16 & 0xFF), (byte) (b >>24 & 0xFF),
                (byte) (b >>32 & 0xFF), (byte) (b >> 40 & 0xFF), (byte) (b >> 48 & 0xFF), (byte) (b >> 56 & 0xFF)
        });
    }

    public void f32(float b) throws IOException {
        i32(Float.floatToIntBits(b));
    }

    public void f64(double b) throws IOException {
        i64(Double.doubleToLongBits(b));
    }

    public void bool(boolean b) throws IOException {
        if (b) {
            u8((byte) 1);
        } else {
            u8((byte) 0);
        }
    }

    public void data(byte[] data) throws IOException {
        variadicUInt(data.length);
        os.write(data);
    }

    public void string(String s) throws IOException {
        data(s.getBytes(StandardCharsets.UTF_8));
    }

    public void variadicUInt(long value) throws IOException {
        int i = 0;
        List<Byte> bytes = new ArrayList<>(2);
        while (value >= 0x80) {
            os.write((byte) (value | 0x80));
            value >>= 7;
            i++;
        }
        os.write((byte) value);
    }
}
