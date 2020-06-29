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
    void testVariadicUint() throws IOException {
        InputStream stream = fromBytes((byte)0x7F, (byte)0xB7, (byte)0x26);
        assertEquals(0x7F, new BareDecoder(stream).variadicUint().longValue());
        assertEquals(0x1337, new BareDecoder(stream).variadicUint().longValue());
    }


    @Test
    void u8() throws IOException {
        assertEquals(3, new BareDecoder(fromBytes((byte)3)).u8());
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
    void variadicInt() {
    }

    @Test
    void variadicUint() {
    }

    @Test
    void string() {
    }

    @Test
    void data() {
    }
}