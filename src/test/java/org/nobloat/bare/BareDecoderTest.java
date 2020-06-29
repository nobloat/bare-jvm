package org.nobloat.bare;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

class BareDecoderTest {

    private InputStream fromBytes(byte... bytes) {
        return new ByteArrayInputStream(bytes);
    }


    @Test
    void u8() throws IOException {
        InputStream stream = fromBytes((byte)3);
        assertEquals(3, new BareDecoder(stream).u8());
        assertEquals(-1, stream.read());
    }

    @Test
    void u16() throws IOException {
        InputStream stream = fromBytes((byte)0xfe, (byte)0xca);
        assertEquals(0xCAFE, new BareDecoder(stream).u16());
    }

    @Test
    void u32() {
    }

    @Test
    void u64() {
    }

    @Test
    void i8() {
    }

    @Test
    void i16() {
    }

    @Test
    void i32() {
    }

    @Test
    void i64() {
    }

    @Test
    void f32() throws IOException {
        InputStream stream = fromBytes((byte)0x71, (byte)0x2D, (byte)0xA7, (byte)0x44);
        assertEquals(1337.42, new BareDecoder(stream).f32());
        assertEquals(-1, stream.read());
    }

    @Test
    void f64() throws IOException {
        InputStream stream = fromBytes((byte)0x9B, (byte)0x6C,(byte) 0xC9, (byte)0x20, (byte)0xF0, (byte)0x21, (byte)0x3F, (byte)0x42);
        assertEquals(133713371337.42424242, new BareDecoder(stream).f64());
        assertEquals(-1, stream.read());
    }

    @Test
    void bool() throws IOException {
        InputStream stream = fromBytes((byte)0x00, (byte)0x01, (byte)0x02);
        assertEquals(false, new BareDecoder(stream).bool());
        assertEquals(true, new BareDecoder(stream).bool());
        assertEquals(true, new BareDecoder(stream).bool());
        assertEquals(-1, stream.read());
    }

    @Test
    void variadicInt() throws IOException {
        InputStream stream = fromBytes((byte)0x54, (byte)0xf1, (byte)0x14);
        assertEquals(42, new BareDecoder(stream).variadicInt());
        assertEquals(-1337, new BareDecoder(stream).variadicInt());
        assertEquals(-1, stream.read());
    }

    @Test
    void variadicUint() throws IOException {
        InputStream stream = fromBytes((byte)0x7F, (byte)0xB7, (byte)0x26);
        assertEquals(0x7F, new BareDecoder(stream).variadicUint().longValue());
        assertEquals(0x1337, new BareDecoder(stream).variadicUint().longValue());
        assertEquals(-1, stream.read());
    }

    @Test
    void string() throws IOException {
        InputStream stream = fromBytes((byte)0x1B, (byte)0xE3, (byte)0x81, (byte)0x93, (byte)0xE3, (byte)0x82, (byte)0x93, (byte)0xE3,
                (byte)0x81, (byte)0xAB, (byte)0xE3, (byte)0x81, (byte)0xA1, (byte)0xE3, (byte)0x81, (byte)0xAF, (byte)0xE3, (byte)0x80, (byte)0x81, (byte)0xE4,
                (byte)0xB8, (byte)0x96, (byte)0xE7, (byte)0x95, (byte)0x8C, (byte)0xEF, (byte)0xBC, (byte)0x81);
        assertEquals("こんにちは、世界！", new BareDecoder(stream).string());
    }

    @Test
    void data() {
    }
}