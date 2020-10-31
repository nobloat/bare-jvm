package org.nobloat.bare;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.nobloat.bare.TestUtil.bytesFromInts;

class AggregateBareEncoderTest {

    ByteArrayOutputStream bos;
    AggregateBareEncoder encoder;

    @BeforeEach
    void setup() {
        bos = new ByteArrayOutputStream();
        encoder = new AggregateBareEncoder(bos);
    }

    @Test
    void array() throws IOException, BareException {
        try(var stream = bos) {
            var array = new String[3];
            array[0] = "こんにちは、世界！";
            array[1] = "こんにちは、世界！";
            array[2] = "こんにちは、世界！";
            encoder.array(array, encoder::string);
        }

        var expected = bytesFromInts(0x1B, 0xE3, 0x81, 0x93, 0xE3, 0x82, 0x93, 0xE3,
                0x81, 0xAB, 0xE3, 0x81, 0xA1, 0xE3, 0x81, 0xAF, 0xE3, 0x80, 0x81, 0xE4,
                0xB8, 0x96, 0xE7, 0x95, 0x8C, 0xEF, 0xBC, 0x81, 0x1B, 0xE3, 0x81, 0x93, 0xE3, 0x82, 0x93, 0xE3,
                0x81, 0xAB, 0xE3, 0x81, 0xA1, 0xE3, 0x81, 0xAF, 0xE3, 0x80, 0x81, 0xE4,
                0xB8, 0x96, 0xE7, 0x95, 0x8C, 0xEF, 0xBC, 0x81, 0x1B, 0xE3, 0x81, 0x93, 0xE3, 0x82, 0x93, 0xE3,
                0x81, 0xAB, 0xE3, 0x81, 0xA1, 0xE3, 0x81, 0xAF, 0xE3, 0x80, 0x81, 0xE4,
                0xB8, 0x96, 0xE7, 0x95, 0x8C, 0xEF, 0xBC, 0x81);

        assertArrayEquals(expected, bos.toByteArray());

        var u32Array = new int[]{7,8,9};
        bos = new ByteArrayOutputStream();
        encoder.array(u32Array, encoder::u32);

    }

    @Test
    void arrayU32() throws IOException, BareException {
        var u32Array = new int[]{7,8,9};
        encoder.array(u32Array, encoder::u32);
        assertArrayEquals(new byte[]{7,0,0,0,8,0,0,0,9,0,0,0}, bos.toByteArray());
    }

    @Test
    void slice() throws IOException, BareException {
        try(var stream = bos) {
            var slice = new ArrayList<String>(3);
            slice.add("こんにちは、世界！");
            slice.add("こんにちは、世界！");
            slice.add("こんにちは、世界！");
            encoder.slice(slice, encoder::string);
        }

        var expected = bytesFromInts(0x03, 0x1B, 0xE3, 0x81, 0x93, 0xE3, 0x82, 0x93, 0xE3,
                0x81, 0xAB, 0xE3, 0x81, 0xA1, 0xE3, 0x81, 0xAF, 0xE3, 0x80, 0x81, 0xE4,
                0xB8, 0x96, 0xE7, 0x95, 0x8C, 0xEF, 0xBC, 0x81, 0x1B, 0xE3, 0x81, 0x93, 0xE3, 0x82, 0x93, 0xE3,
                0x81, 0xAB, 0xE3, 0x81, 0xA1, 0xE3, 0x81, 0xAF, 0xE3, 0x80, 0x81, 0xE4,
                0xB8, 0x96, 0xE7, 0x95, 0x8C, 0xEF, 0xBC, 0x81, 0x1B, 0xE3, 0x81, 0x93, 0xE3, 0x82, 0x93, 0xE3,
                0x81, 0xAB, 0xE3, 0x81, 0xA1, 0xE3, 0x81, 0xAF, 0xE3, 0x80, 0x81, 0xE4,
                0xB8, 0x96, 0xE7, 0x95, 0x8C, 0xEF, 0xBC, 0x81);

        assertEquals(expected.length, bos.size());
        assertArrayEquals(expected, bos.toByteArray());
    }

    @Test
    void testLongArray() throws IOException, BareException {
        encoder.array(new long[]{1L,2L}, encoder::i64);
        assertArrayEquals(new byte[]{1,0,0,0,0,0,0,0,2,0,0,0,0,0,0,0},bos.toByteArray());
    }

    @Test
    void testShortArray() throws IOException, BareException {
        encoder.array(new short[]{1,2}, encoder::i16);
        assertArrayEquals(new byte[]{1,0,2,0,},bos.toByteArray());
    }

    @Test
    void testBooleanArray() throws IOException, BareException {
        encoder.array(new boolean[]{false, true}, encoder::bool);
        assertArrayEquals(new byte[]{0,1},bos.toByteArray());
    }

    @Test
    void testByteArray() throws IOException, BareException {
        encoder.array(new Byte[]{1,2}, encoder::i8);
        assertArrayEquals(new byte[]{1,2},bos.toByteArray());
    }

    @Test
    void testbyteArray() throws IOException, BareException {
        encoder.array(new byte[]{1,2});
        assertArrayEquals(new byte[]{1,2},bos.toByteArray());
    }

    @Test
    void testDoubleArray() throws IOException, BareException {
        encoder.array(new double[]{133713371337.42424242,133713371337.42424242}, encoder::f64);
        assertArrayEquals(new byte[]{(byte) 0x9B, 0x6C, (byte) 0xC9, 0x20, (byte) 0xF0, 0x21, 0x3F, 0x42,(byte) 0x9B, 0x6C, (byte) 0xC9, 0x20, (byte) 0xF0, 0x21, 0x3F, 0x42},bos.toByteArray());
    }

    @Test
    void testFloatArray() throws IOException, BareException {
        encoder.array(new float[]{1337.42f,1337.42f}, encoder::f32);
        assertArrayEquals(new byte[]{0x71, 0x2D, (byte) 0xA7, 0x44, 0x71, 0x2D, (byte) 0xA7, 0x44},bos.toByteArray());
    }

    @Test
    void map() throws IOException, BareException {
        var map = new HashMap<Byte, Byte>();
        map.put((byte)0x01, (byte) 0x11);
        map.put((byte)0x02, (byte) 0x22);
        map.put((byte)0x03, (byte) 0x33);
        encoder.map(map, encoder::u8, encoder::u8);

        var expected = bytesFromInts(0x03, 0x01, 0x11, 0x02, 0x22, 0x03, 0x33);
        assertEquals(expected.length, bos.size());
        assertArrayEquals(expected, bos.toByteArray());
    }

    @Test
    void optional() throws IOException, BareException {
        encoder.optional(Optional.of("こんにちは、世界！"), encoder::string);
        var expected = bytesFromInts(0x01, 0x1B, 0xE3, 0x81, 0x93, 0xE3, 0x82, 0x93, 0xE3,
                0x81, 0xAB, 0xE3, 0x81, 0xA1, 0xE3, 0x81, 0xAF, 0xE3, 0x80, 0x81, 0xE4,
                0xB8, 0x96, 0xE7, 0x95, 0x8C, 0xEF, 0xBC, 0x81);
        assertEquals(expected.length, bos.size());
    }

    @Test
    void emptyOptional() throws IOException, BareException {
        var expected = bytesFromInts(0x00);
        encoder.optional(Optional.empty(), encoder::string);
        assertEquals(expected.length, bos.size());
        assertArrayEquals(expected, bos.toByteArray());
    }

    @Test
    void union() throws IOException, BareException {
        var value = new Union(1, "こんにちは、世界！");
        encoder.union(value, Map.of(1, e -> encoder.string((String) e)));
        var expected = bytesFromInts(0x01, 0x1B, 0xE3, 0x81, 0x93, 0xE3, 0x82, 0x93, 0xE3,
                0x81, 0xAB, 0xE3, 0x81, 0xA1, 0xE3, 0x81, 0xAF, 0xE3, 0x80, 0x81, 0xE4,
                0xB8, 0x96, 0xE7, 0x95, 0x8C, 0xEF, 0xBC, 0x81);
        assertEquals(expected.length, bos.size());
        assertArrayEquals(expected, bos.toByteArray());
    }

    @Test
    void union2() throws IOException, BareException {
        encoder.union(new Union(0, 1337.42f), Map.of(0, e -> encoder.f32((Float) e)));
        var expected = bytesFromInts(0x00, 0x71, 0x2D, 0xA7, 0x44);
        assertEquals(expected.length, bos.size());
        assertArrayEquals(expected, bos.toByteArray());
    }

    @Test
    void unmappedUnion() {
        assertThrows(BareException.class, () -> encoder.union(new Union(1, 1337.42f), Map.of(0, e -> encoder.f32((Float) e))));
    }
}