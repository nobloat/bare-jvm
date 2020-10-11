package org.nobloat.bare;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AggregateBareEncoderTest {

    ByteArrayOutputStream bos;
    AggregateBareEncoder encoder;

    private byte[] fromInts(int... bytes) {
        byte[] b = new byte[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            b[i] = (byte) bytes[i];
        }
        return b;
    }

    @BeforeEach
    void setup() {
        bos = new ByteArrayOutputStream();
        encoder = new AggregateBareEncoder(bos);
    }

    @Test
    void array() throws IOException {
        try(var stream = bos) {
            var array = new Array<String>(3);
            array.set(0, "こんにちは、世界！");
            array.set(1, "こんにちは、世界！");
            array.set(2, "こんにちは、世界！");
            encoder.array(array);
        }

        var expected = fromInts(0x1B, 0xE3, 0x81, 0x93, 0xE3, 0x82, 0x93, 0xE3,
                0x81, 0xAB, 0xE3, 0x81, 0xA1, 0xE3, 0x81, 0xAF, 0xE3, 0x80, 0x81, 0xE4,
                0xB8, 0x96, 0xE7, 0x95, 0x8C, 0xEF, 0xBC, 0x81, 0x1B, 0xE3, 0x81, 0x93, 0xE3, 0x82, 0x93, 0xE3,
                0x81, 0xAB, 0xE3, 0x81, 0xA1, 0xE3, 0x81, 0xAF, 0xE3, 0x80, 0x81, 0xE4,
                0xB8, 0x96, 0xE7, 0x95, 0x8C, 0xEF, 0xBC, 0x81, 0x1B, 0xE3, 0x81, 0x93, 0xE3, 0x82, 0x93, 0xE3,
                0x81, 0xAB, 0xE3, 0x81, 0xA1, 0xE3, 0x81, 0xAF, 0xE3, 0x80, 0x81, 0xE4,
                0xB8, 0x96, 0xE7, 0x95, 0x8C, 0xEF, 0xBC, 0x81);

        assertEquals(expected.length, bos.size());
        assertEquals(expected, bos.toByteArray());
    }

    @Test
    void slice() {
    }

    @Test
    void map() {
    }

    @Test
    void optional() {
    }

    @Test
    void union() {
    }
}