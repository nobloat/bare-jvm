package org.nobloat.bare;

import java.io.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class PrimitiveBareEncoder {
    private final DataOutputStream os;
    private static final BigInteger UNSIGNED_LONG_MASK = BigInteger.ONE.shiftLeft(Long.SIZE).subtract(BigInteger.ONE);
    private final boolean verifyInput;

    public PrimitiveBareEncoder(OutputStream os, boolean verifyInput) {
        this.verifyInput = verifyInput;
        this.os = new DataOutputStream(os);
    }

    public PrimitiveBareEncoder(OutputStream os) {
        this(os, false);
    }

    public void u8(byte b) throws IOException {
        os.writeByte(b);
    }

    public void u16(int b) throws IOException {
        os.write(new byte[]{(byte) (b & 0xFF), (byte) (b >> 8 & 0xFF)});
    }

    public void u32(Integer b) throws IOException {
        os.write(new byte[]{(byte) (b & 0xFF), (byte) (b >> 8 & 0xFF), (byte) (b >> 16 & 0xFF), (byte) (b >> 24 & 0xFF)});
    }

    public void u64(BigInteger b) throws IOException {
        if (verifyInput && b.bitLength() > 64) {
            throw new NotSerializableException("value for variadicUint cannot have more than 64 bits, value has " + b.bitLength() + " bits");
        }
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
        os.write(new byte[]{(byte) (b & 0xFF), (byte) (b >> 8 & 0xFF), (byte) (b >> 16 & 0xFF), (byte) (b >> 24 & 0xFF),
                (byte) (b >> 32 & 0xFF), (byte) (b >> 40 & 0xFF), (byte) (b >> 48 & 0xFF), (byte) (b >> 56 & 0xFF)
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

    public int variadicUInt(BigInteger value) throws IOException {
        if (verifyInput && value.bitLength() > 64) {
            throw new NotSerializableException("value for variadicUint cannot have more than 64 bits, value has " + value.bitLength() + " bits");
        }
        if (verifyInput && value.signum() == -1) {
            throw new NotSerializableException("value for variadicUint cannot be negative: " + value);
        }

        int i = 0;
        while (value.longValue() >= 0x80) {
            os.write((byte) (value.longValue() | 0x80));
            value = value.shiftRight(7);
            i++;
        }
        os.write((byte) value.longValue());
        return i + 1;
    }

    public int variadicUInt(long value) throws IOException {
        if (verifyInput && value < 0) {
            throw new NotSerializableException("value for variadicUint cannot be negative: " + value);
        }
        int i = 0;
        while (value >= 0x80) {
            os.write((byte) (value | 0x80));
            value >>= 7;
            i++;
        }
        os.write((byte) value);
        return i + 1;
    }

    public int variadicInt(long value) throws IOException {
        long unsigned = value << 1;
        if (unsigned < 0) {
            unsigned = ~unsigned;
        }
        return variadicUInt(unsigned);
    }
}
