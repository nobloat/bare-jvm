package org.nobloat.bare;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.nobloat.bare.TestUtil.fromInts;

class AggregateBareDecoderTest {


    @Test
    void testEmptyOptional() throws Exception {
        var bytes = TestUtil.fromInts(0x00);
        var decoder = new AggregateBareDecoder(bytes);

        var result = decoder.optional(TestClasses.SimplePerson::decode);
        assertFalse(result.isPresent());
    }

    @Test
    void testOptional() throws Exception {
        var bytes = TestUtil.fromInts(0x01, 0x05, 0x50, 0x65, 0x74, 0x65, 0x72, 0x1E);
        var decoder = new AggregateBareDecoder(bytes);

        var result = decoder.optional(TestClasses.SimplePerson::decode);
        assertEquals("Peter", result.get().name);
        assertEquals(30, result.get().age);
    }

    @Test
    public void testStaticArray() throws IOException, ReflectiveOperationException {
        var stream = fromInts(0x1B, 0xE3, 0x81, 0x93, 0xE3, 0x82, 0x93, 0xE3,
                0x81, 0xAB, 0xE3, 0x81, 0xA1, 0xE3, 0x81, 0xAF, 0xE3, 0x80, 0x81, 0xE4,
                0xB8, 0x96, 0xE7, 0x95, 0x8C, 0xEF, 0xBC, 0x81, 0x1B, 0xE3, 0x81, 0x93, 0xE3, 0x82, 0x93, 0xE3,
                0x81, 0xAB, 0xE3, 0x81, 0xA1, 0xE3, 0x81, 0xAF, 0xE3, 0x80, 0x81, 0xE4,
                0xB8, 0x96, 0xE7, 0x95, 0x8C, 0xEF, 0xBC, 0x81, 0x1B, 0xE3, 0x81, 0x93, 0xE3, 0x82, 0x93, 0xE3,
                0x81, 0xAB, 0xE3, 0x81, 0xA1, 0xE3, 0x81, 0xAF, 0xE3, 0x80, 0x81, 0xE4,
                0xB8, 0x96, 0xE7, 0x95, 0x8C, 0xEF, 0xBC, 0x81);

        var decoder = new ReflectiveBareDecoder(stream);
        var result = decoder.values(String.class, 3);

        assertEquals(3, result.size);
        assertEquals("こんにちは、世界！", result.get(0));
        assertEquals("こんにちは、世界！", result.get(1));
        assertEquals("こんにちは、世界！", result.get(2));
    }

    @Test
    public void testSlice() throws IOException, BareException {
        var stream = fromInts(0x03, 0x1B, 0xE3, 0x81, 0x93, 0xE3, 0x82, 0x93, 0xE3,
                0x81, 0xAB, 0xE3, 0x81, 0xA1, 0xE3, 0x81, 0xAF, 0xE3, 0x80, 0x81, 0xE4,
                0xB8, 0x96, 0xE7, 0x95, 0x8C, 0xEF, 0xBC, 0x81, 0x1B, 0xE3, 0x81, 0x93, 0xE3, 0x82, 0x93, 0xE3,
                0x81, 0xAB, 0xE3, 0x81, 0xA1, 0xE3, 0x81, 0xAF, 0xE3, 0x80, 0x81, 0xE4,
                0xB8, 0x96, 0xE7, 0x95, 0x8C, 0xEF, 0xBC, 0x81, 0x1B, 0xE3, 0x81, 0x93, 0xE3, 0x82, 0x93, 0xE3,
                0x81, 0xAB, 0xE3, 0x81, 0xA1, 0xE3, 0x81, 0xAF, 0xE3, 0x80, 0x81, 0xE4,
                0xB8, 0x96, 0xE7, 0x95, 0x8C, 0xEF, 0xBC, 0x81);

        var decoder = new AggregateBareDecoder(stream);
        var result = decoder.slice(AggregateBareDecoder::string);

        assertEquals(3, result.size());
        assertEquals("こんにちは、世界！", result.get(0));
        assertEquals("こんにちは、世界！", result.get(1));
        assertEquals("こんにちは、世界！", result.get(2));
    }

    @Test
    public void testMap() throws IOException, BareException {
        var stream = fromInts(0x03, 0x01, 0x11, 0x02, 0x22, 0x03, 0x33);
        var decoder = new AggregateBareDecoder(stream);
        var result = decoder.map(AggregateBareDecoder::u8, AggregateBareDecoder::u8);

        assertEquals(3, result.size());
        assertEquals((byte)0x11, result.get((byte)0x01));
        assertEquals((byte)0x22, result.get((byte)0x02));
        assertEquals((byte)0x33, result.get((byte)0x03));
    }


    @Test
    public void testUnion() throws IOException, BareException {
        var stream = fromInts(0x01, 0x1B, 0xE3, 0x81, 0x93, 0xE3, 0x82, 0x93, 0xE3,
                0x81, 0xAB, 0xE3, 0x81, 0xA1, 0xE3, 0x81, 0xAF, 0xE3, 0x80, 0x81, 0xE4,
                0xB8, 0x96, 0xE7, 0x95, 0x8C, 0xEF, 0xBC, 0x81);

        var decoder = new AggregateBareDecoder(stream);

        var result = decoder.union(Map.of(0, PrimitiveBareDecoder::f32, 1, PrimitiveBareDecoder::string));
        assertEquals(1, result.type());
        assertEquals("こんにちは、世界！", result.get(String.class));


        stream = fromInts(0x00, 0x71, 0x2D, 0xA7, 0x44);
        decoder = new AggregateBareDecoder(stream);
        result = decoder.union(Map.of(0, PrimitiveBareDecoder::f32, 1, PrimitiveBareDecoder::string));
        assertEquals(0, result.type());
        assertEquals(1337.42, result.get(Float.class), 0.001);

        assertThrows(BareException.class, () ->  new AggregateBareDecoder(fromInts(0x03, 0x71, 0x2D, 0xA7, 0x44))
                .union(Map.of(0, PrimitiveBareDecoder::f32, 1, PrimitiveBareDecoder::string)));
    }
}