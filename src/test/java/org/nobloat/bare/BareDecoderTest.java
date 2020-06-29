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
    void u16() {
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
    void f32() {
    }

    @Test
    void f64() {
    }

    @Test
    void bool() {
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
    void string() {
    }

    @Test
    void data() {
    }
}