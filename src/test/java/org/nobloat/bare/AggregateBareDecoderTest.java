package org.nobloat.bare;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

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


}