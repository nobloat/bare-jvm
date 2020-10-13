package org.nobloat.bare;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.NotSerializableException;
import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PrimitiveBareEncoderTest {
    ByteArrayOutputStream bos;
    PrimitiveBareEncoder encoder;

    @BeforeEach
    void setup() {
        bos = new ByteArrayOutputStream();
        encoder = new PrimitiveBareEncoder(bos);
    }

    @Test
    void u8() throws IOException {
        try(var stream = bos) {
            encoder.u8((byte) 0xDE);
        }
        assertEquals(1, bos.size());
        assertEquals((byte)0xDE, bos.toByteArray()[0]);
    }

    @Test
    void u16() throws IOException {
        try(var stream = bos) {
            encoder.u16(0xCAFE);
        }
        var result = bos.toByteArray();
        assertEquals(2, bos.size());
        assertArrayEquals(new byte[]{(byte) 0xFE, (byte) 0xCA}, result);
    }

    @Test
    void u32() throws IOException {
        try(var stream = bos) {
            encoder.u32(0xDEADBEEF);
        }
        var result = bos.toByteArray();
        assertEquals(4, bos.size());
        assertArrayEquals(new byte[]{(byte) 0xEF, (byte) 0xBE, (byte) 0xAD, (byte) 0xDE},result);
    }

    @Test
    void u64() throws IOException {
        try(var stream = bos) {
            encoder.u64(new BigInteger("CAFEBABEDEADBEEF", 16));
        }
        var result = bos.toByteArray();
        assertEquals(8, bos.size());
        assertArrayEquals(new byte[]{(byte) 0xEF, (byte) 0xBE, (byte) 0xAD, (byte) 0xDE, (byte) 0xBE, (byte) 0xBA, (byte) 0xFE, (byte) 0xCA},result);
    }

    @Test
    void i8() throws IOException {
        try(var stream = bos) {
            encoder.i8((byte) -42);
        }
        var result = bos.toByteArray();
        assertEquals(1, bos.size());
        assertArrayEquals(new byte[]{(byte) 0xD6},result);
    }

    @Test
    void i16() throws IOException {
        try(var stream = bos) {
            encoder.i16((short) -1234);
        }
        var result = bos.toByteArray();
        assertEquals(2, bos.size());
        assertArrayEquals(new byte[]{(byte) 0x2E, (byte) 0xFB},result);
    }

    @Test
    void i32() throws IOException {
        try(var stream = bos) {
            encoder.i32( -12345678);
        }
        var result = bos.toByteArray();
        assertEquals(4, bos.size());
        assertArrayEquals(new byte[]{(byte) 0xB2, (byte) 0x9E, 0x43, (byte) 0xFF},result);
    }

    @Test
    void i64() throws IOException {
        try(var stream = bos) {
            encoder.i64( -12345678987654321L);
        }
        var result = bos.toByteArray();
        assertEquals(8, bos.size());
        assertArrayEquals(new byte[]{0x4F, 0x0B, 0x6E, (byte) 0x9D, (byte) 0xAB, 0x23, (byte) 0xD4, (byte) 0xFF},result);
    }

    @Test
    void f32() throws IOException {
        try(var stream = bos) {
            encoder.f32(1337.42f);
        }
        var result = bos.toByteArray();
        assertEquals(4, bos.size());
        assertArrayEquals(new byte[]{0x71, 0x2D, (byte) 0xA7, 0x44},result);
    }

    @Test
    void f64() throws IOException {
        try(var stream = bos) {
            encoder.f64( 133713371337.42424242);
        }
        var result = bos.toByteArray();
        assertEquals(8, bos.size());
        assertArrayEquals(new byte[]{(byte) 0x9B, 0x6C, (byte) 0xC9, 0x20, (byte) 0xF0, 0x21, 0x3F, 0x42},result);
    }

    @Test
    void bool() throws IOException {
        try(var stream = bos) {
            encoder.bool( true);
            encoder.bool( false);
        }
        var result = bos.toByteArray();
        assertEquals(2, bos.size());
        assertArrayEquals(new byte[]{(byte) 0x01, 0x00},result);
    }

    @Test
    void string() throws IOException {
        try(var stream = bos) {
            encoder.string("こんにちは、世界！");
        }
        var result = bos.toByteArray();
        assertEquals(28, bos.size());
        assertArrayEquals(new byte[]{(byte) 0x1B, (byte) 0xE3, (byte) 0x81, (byte) 0x93, (byte) 0xE3, (byte) 0x82, (byte) 0x93, (byte) 0xE3, (byte) 0x81, (byte) 0xAB,
                (byte) 0xE3, (byte) 0x81, (byte) 0xA1, (byte) 0xE3, (byte) 0x81, (byte) 0xAF, (byte) 0xE3, (byte) 0x80, (byte) 0x81, (byte) 0xE4, (byte) 0xB8,
                (byte) 0x96, (byte) 0xE7, (byte) 0x95, (byte) 0x8C, (byte) 0xEF, (byte) 0xBC, (byte) 0x81},result);
    }

    @Test
    void variadicUInt() throws IOException {
        try(var stream = bos) {
            assertEquals(5, encoder.variadicUInt( new BigInteger("DEADBEEF", 16)));
        }
        var result = bos.toByteArray();
        assertEquals(5, bos.size());
        assertArrayEquals(new byte[]{(byte) 0xEF, (byte) 0xFD, (byte) 0xB6, (byte) 0xF5, 0x0D},result);


        encoder = new PrimitiveBareEncoder(null, true);
        assertThrows(NotSerializableException.class, () -> assertEquals(5, encoder.variadicUInt( new BigInteger("DEADBEEFDEADBEEFDEADBEEFDEADBEEF", 16))));
        assertThrows(NotSerializableException.class, () -> assertEquals(5, encoder.variadicUInt( new BigInteger("-1", 16))));
    }

    @Test
    void variadicUIntLong() throws IOException {
        try(var stream = bos) {
            assertEquals(5, encoder.variadicUInt(0xDEADBEEFL));
        }
        var result = bos.toByteArray();
        assertEquals(5, bos.size());
        assertArrayEquals(new byte[]{(byte) 0xEF, (byte) 0xFD, (byte) 0xB6, (byte) 0xF5, 0x0D},result);

        encoder = new PrimitiveBareEncoder(null, true);
        assertThrows(NotSerializableException.class, () -> assertEquals(5, encoder.variadicUInt( -1)));
    }

    @Test
    void variadicInt() throws IOException {
        try(var stream = bos) {
            assertEquals(4, encoder.variadicInt(-12345678));
        }
        var result = bos.toByteArray();
        assertEquals(4, bos.size());
        assertArrayEquals(new byte[]{(byte) 0x9B, (byte) 0x85, (byte) 0xE3, 0x0B},result);
    }
}