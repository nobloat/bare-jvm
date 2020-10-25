package org.nobloat.bare;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.nobloat.bare.TestUtil.fromInts;

class PrimitiveBareDecoderTest {

    @Test
    void u8() throws IOException {
        InputStream stream = fromInts(0x42);
        assertEquals(0x42, new PrimitiveBareDecoder(stream).u8());
        assertEquals(-1, stream.read());
    }

    @Test
    void u16() throws IOException {
        InputStream stream = fromInts(0xfe, 0xca);
        assertEquals(0xCAFE, new PrimitiveBareDecoder(stream).u16());
        assertEquals(-1, stream.read());
    }

    @Test
    void u32() throws IOException {
        InputStream stream = fromInts(0xEF, 0xBE, 0xAD, 0xDE);
        assertEquals(0xDEADBEEF, new PrimitiveBareDecoder(stream).u32());
        assertEquals(-1, stream.read());
    }

    @Test
    void u64() throws IOException {
        InputStream stream = fromInts(0xEF, 0xBE, 0xAD, 0xDE, 0xBE, 0xBA, 0xFE, 0xCA);
        assertEquals("14627333968688430831", new PrimitiveBareDecoder(stream).u64().toString());
        assertEquals(-1, stream.read());
    }

    @Test
    void i8() throws IOException {
        InputStream stream = fromInts(0xd6);
        assertEquals(-42, new PrimitiveBareDecoder(stream).i8());
        assertEquals(-1, stream.read());
    }

    @Test
    void i16() throws IOException {
        InputStream stream = fromInts(0x2E, 0xFB);
        assertEquals(-1234, new PrimitiveBareDecoder(stream).i16());
        assertEquals(-1, stream.read());
    }

    @Test
    void i32() throws IOException {
        InputStream stream = fromInts(0xB2, 0x9E, 0x43, 0xFF);
        assertEquals(-12345678, new PrimitiveBareDecoder(stream).i32());
        assertEquals(-1, stream.read());
    }

    @Test
    void i64() throws IOException {
        InputStream stream = fromInts(0x4F, 0x0B, 0x6E, 0x9D, 0xAB, 0x23, 0xD4, 0xFF);
        assertEquals(-12345678987654321L, new PrimitiveBareDecoder(stream).i64());
        assertEquals(-1, stream.read());
    }

    @Test
    void f32() throws IOException {
        InputStream stream = fromInts(0x71, 0x2D, 0xA7, 0x44);
        assertEquals(1337.42, new PrimitiveBareDecoder(stream).f32(), 0.001);
        assertEquals(-1, stream.read());
    }

    @Test
    void f64() throws IOException {
        InputStream stream = fromInts(0x9B, 0x6C, 0xC9, 0x20, 0xF0, 0x21, 0x3F, 0x42);
        assertEquals(133713371337.42424242, new PrimitiveBareDecoder(stream).f64());
        assertEquals(-1, stream.read());
    }

    @Test
    void bool() throws IOException {
        InputStream stream = fromInts(0x00, 0x01, 0x02);
        assertFalse(new PrimitiveBareDecoder(stream).bool());
        assertTrue(new PrimitiveBareDecoder(stream).bool());
        assertTrue(new PrimitiveBareDecoder(stream).bool());
        assertEquals(-1, stream.read());
    }

    @Test
    void variadicInt() throws IOException {
        InputStream stream = fromInts(0x9B, 0x85, 0xE3, 0x0B);
        assertEquals(-12345678L, new PrimitiveBareDecoder(stream).variadicInt());
        assertEquals(-1, stream.read());
    }

    @Test
    void variadicUint() throws IOException {
        InputStream stream = fromInts(0xEF, 0xFD, 0xB6, 0xF5, 0x0D);
        assertEquals(0xDEADBEEFL, new PrimitiveBareDecoder(stream).variadicUint().longValue());
        assertEquals(-1, stream.read());
    }

    @Test
    void string() throws IOException, BareException {
        InputStream stream = fromInts(0x1B, 0xE3, 0x81, 0x93, 0xE3, 0x82, 0x93, 0xE3,
                0x81, 0xAB, 0xE3, 0x81, 0xA1, 0xE3, 0x81, 0xAF, 0xE3, 0x80, 0x81, 0xE4,
                0xB8, 0x96, 0xE7, 0x95, 0x8C, 0xEF, 0xBC, 0x81);
        assertEquals("こんにちは、世界！", new PrimitiveBareDecoder(stream).string());
        assertEquals(-1, stream.read());
    }

    @Test
    void data() throws IOException, BareException {
        Byte[] ref = {0x13, 0x37, 0x42};
        InputStream stream = fromInts(0x03, 0x13, 0x37, 0x42);
        var data = new PrimitiveBareDecoder(stream).data();
        assertArrayEquals(ref, data);
        assertEquals(-1, stream.read());
    }

    @Test
    void dataTooLong() {
        var decoder = new PrimitiveBareDecoder(fromInts(0x03, 0x13, 0x37, 0x42));
        decoder.MaxSliceLength = 2;
        assertThrows(BareException.class, () -> decoder.data());
    }
}