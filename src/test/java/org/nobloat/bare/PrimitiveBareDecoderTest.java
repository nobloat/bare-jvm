package org.nobloat.bare;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

class PrimitiveBareDecoderTest {

    private InputStream fromBytes(byte... bytes) {
        return new ByteArrayInputStream(bytes);
    }


    @Test
    void u8() throws IOException {
        InputStream stream = fromBytes((byte)0x42);
        assertEquals(0x42, new PrimitiveBareDecoder(stream).u8());
        assertEquals(-1, stream.read());
    }

    @Test
    void u16() throws IOException {
        InputStream stream = fromBytes((byte)0xfe, (byte)0xca);
        assertEquals(0xCAFE, new PrimitiveBareDecoder(stream).u16());
        assertEquals(-1, stream.read());
    }

    @Test
    void u32() throws IOException {
        InputStream stream = fromBytes((byte)0xEF, (byte)0xBE, (byte)0xAD, (byte)0xDE);
        assertEquals(0xDEADBEEF, new PrimitiveBareDecoder(stream).u32());
        assertEquals(-1, stream.read());
    }

    @Test
    void u64() throws IOException {
        InputStream stream = fromBytes((byte)0xEF, (byte)0xBE, (byte)0xAD, (byte)0xDE, (byte)0xBE, (byte)0xBA, (byte)0xFE, (byte)0xCA);
        assertEquals("14627333968688430831", new PrimitiveBareDecoder(stream).u64().toString());
        assertEquals(-1, stream.read());
    }

    @Test
    void i8() throws IOException {
        InputStream stream = fromBytes((byte)0xd6);
        assertEquals(-42, new PrimitiveBareDecoder(stream).i8());
        assertEquals(-1, stream.read());
    }

    @Test
    void i16() throws IOException {
        InputStream stream = fromBytes((byte)0x2E, (byte)0xFB);
        assertEquals(-1234, new PrimitiveBareDecoder(stream).i16());
        assertEquals(-1, stream.read());
    }

    @Test
    void i32() throws IOException {
        InputStream stream = fromBytes((byte)0xB2, (byte)0x9E, (byte)0x43, (byte)0xFF);
        assertEquals(-12345678, new PrimitiveBareDecoder(stream).i32());
        assertEquals(-1, stream.read());
    }

    @Test
    void i64() throws IOException {
        InputStream stream = fromBytes((byte)0x4F, (byte)0x0B, (byte)0x6E, (byte)0x9D, (byte)0xAB, (byte)0x23, (byte)0xD4, (byte)0xFF);
        assertEquals(-12345678987654321L, new PrimitiveBareDecoder(stream).i64());
        assertEquals(-1, stream.read());
    }

    @Test
    void f32() throws IOException {
        InputStream stream = fromBytes((byte)0x71, (byte)0x2D, (byte)0xA7, (byte)0x44);
        assertEquals(1337.42, new PrimitiveBareDecoder(stream).f32(), 0.001);
        assertEquals(-1, stream.read());
    }

    @Test
    void f64() throws IOException {
        InputStream stream = fromBytes((byte)0x9B, (byte)0x6C,(byte) 0xC9, (byte)0x20, (byte)0xF0, (byte)0x21, (byte)0x3F, (byte)0x42);
        assertEquals(133713371337.42424242, new PrimitiveBareDecoder(stream).f64());
        assertEquals(-1, stream.read());
    }

    @Test
    void bool() throws IOException {
        InputStream stream = fromBytes((byte)0x00, (byte)0x01, (byte)0x02);
        assertEquals(false, new PrimitiveBareDecoder(stream).bool());
        assertEquals(true, new PrimitiveBareDecoder(stream).bool());
        assertEquals(true, new PrimitiveBareDecoder(stream).bool());
        assertEquals(-1, stream.read());
    }

    @Test
    void variadicInt() throws IOException {
        InputStream stream = fromBytes((byte)0x54, (byte)0xf1, (byte)0x14);
        assertEquals(42, new PrimitiveBareDecoder(stream).variadicInt());
        assertEquals(-1337, new PrimitiveBareDecoder(stream).variadicInt());
        assertEquals(-1, stream.read());
    }

    @Test
    void variadicUint() throws IOException {
        InputStream stream = fromBytes((byte)0x7F, (byte)0xB7, (byte)0x26);
        assertEquals(0x7F, new PrimitiveBareDecoder(stream).variadicUint().longValue());
        assertEquals(0x1337, new PrimitiveBareDecoder(stream).variadicUint().longValue());
        assertEquals(-1, stream.read());
    }

    @Test
    void string() throws IOException {
        InputStream stream = fromBytes((byte)0x1B, (byte)0xE3, (byte)0x81, (byte)0x93, (byte)0xE3, (byte)0x82, (byte)0x93, (byte)0xE3,
                (byte)0x81, (byte)0xAB, (byte)0xE3, (byte)0x81, (byte)0xA1, (byte)0xE3, (byte)0x81, (byte)0xAF, (byte)0xE3, (byte)0x80, (byte)0x81, (byte)0xE4,
                (byte)0xB8, (byte)0x96, (byte)0xE7, (byte)0x95, (byte)0x8C, (byte)0xEF, (byte)0xBC, (byte)0x81);
        assertEquals("こんにちは、世界！", new PrimitiveBareDecoder(stream).string());
        assertEquals(-1, stream.read());
    }

    @Test
    void data() throws IOException {
        byte[] ref = {(byte)0x13, (byte)0x37, (byte)0x42};
        InputStream stream = fromBytes((byte)0x03, (byte)0x13, (byte)0x37, (byte)0x42);
        assertArrayEquals(ref, new PrimitiveBareDecoder(stream).data());
        assertEquals(-1, stream.read());
    }

    @Test
    void Void() {
        InputStream stream = fromBytes();
        new PrimitiveBareDecoder(stream).Void();
    }
}