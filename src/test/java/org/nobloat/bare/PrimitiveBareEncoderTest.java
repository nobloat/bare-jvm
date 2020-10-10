package org.nobloat.bare;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.*;

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
            encoder.u16(20652);
        }
        assertEquals(2, bos.size());
        assertEquals((byte)0xAC, bos.toByteArray()[0]);
        assertEquals((byte)0x50, bos.toByteArray()[1]);
    }

    @Test
    void u32() throws IOException {
        try(var stream = bos) {
            encoder.u32(20652);
        }
        assertEquals(4, bos.size());
        assertEquals((byte)0xAC, bos.toByteArray()[0]);
        assertEquals((byte)0x50, bos.toByteArray()[1]);
        assertEquals((byte)0x00, bos.toByteArray()[2]);
        assertEquals((byte)0x00, bos.toByteArray()[3]);
    }

    @Test
    void u64() throws IOException {
        try(var stream = bos) {
            encoder.u64(BigInteger.TEN.multiply(BigInteger.TEN).pow(3));
        }
        assertEquals(8, bos.size());
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
    void data() {
    }

    @Test
    void string() {
    }

    @Test
    void variadicUInt() {
    }
}