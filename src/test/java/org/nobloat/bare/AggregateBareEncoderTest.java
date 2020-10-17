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
            var array = new Array<String>(3);
            array.set(0, "こんにちは、世界！");
            array.set(1, "こんにちは、世界！");
            array.set(2, "こんにちは、世界！");
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