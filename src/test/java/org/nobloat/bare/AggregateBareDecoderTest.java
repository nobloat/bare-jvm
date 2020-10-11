package org.nobloat.bare;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AggregateBareDecoderTest {

    private InputStream fromInts(int... bytes) {
        byte[] b = new byte[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            b[i] = (byte) bytes[i];
        }
        return new ByteArrayInputStream(b);
    }

    @Test
    public void testOptional() throws IOException {
        var stream = fromInts(0x01, 0x1B, 0xE3, 0x81, 0x93, 0xE3, 0x82, 0x93, 0xE3,
                0x81, 0xAB, 0xE3, 0x81, 0xA1, 0xE3, 0x81, 0xAF, 0xE3, 0x80, 0x81, 0xE4,
                0xB8, 0x96, 0xE7, 0x95, 0x8C, 0xEF, 0xBC, 0x81);


        var decoder = new AggregateBareDecoder(stream);
        Optional<String> result = decoder.optional(String.class);
        assertEquals("こんにちは、世界！", result.get());

        stream = fromInts(0x00);
        decoder = new AggregateBareDecoder(stream);
        result = decoder.optional(String.class);
        assertFalse(result.isPresent());
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

        var decoder = new AggregateBareDecoder(stream);
        var result = decoder.values(String.class, 3);

        assertEquals(3, result.size);
        assertEquals("こんにちは、世界！", result.get(0));
        assertEquals("こんにちは、世界！", result.get(1));
        assertEquals("こんにちは、世界！", result.get(2));
    }

    @Test
    public void testSlice() throws IOException, ReflectiveOperationException {
        var stream = fromInts(0x03, 0x1B, 0xE3, 0x81, 0x93, 0xE3, 0x82, 0x93, 0xE3,
                0x81, 0xAB, 0xE3, 0x81, 0xA1, 0xE3, 0x81, 0xAF, 0xE3, 0x80, 0x81, 0xE4,
                0xB8, 0x96, 0xE7, 0x95, 0x8C, 0xEF, 0xBC, 0x81, 0x1B, 0xE3, 0x81, 0x93, 0xE3, 0x82, 0x93, 0xE3,
                0x81, 0xAB, 0xE3, 0x81, 0xA1, 0xE3, 0x81, 0xAF, 0xE3, 0x80, 0x81, 0xE4,
                0xB8, 0x96, 0xE7, 0x95, 0x8C, 0xEF, 0xBC, 0x81, 0x1B, 0xE3, 0x81, 0x93, 0xE3, 0x82, 0x93, 0xE3,
                0x81, 0xAB, 0xE3, 0x81, 0xA1, 0xE3, 0x81, 0xAF, 0xE3, 0x80, 0x81, 0xE4,
                0xB8, 0x96, 0xE7, 0x95, 0x8C, 0xEF, 0xBC, 0x81);

        var decoder = new AggregateBareDecoder(stream);
        var result = decoder.values(String.class);

        assertEquals(3, result.size());
        assertEquals("こんにちは、世界！", result.get(0));
        assertEquals("こんにちは、世界！", result.get(1));
        assertEquals("こんにちは、世界！", result.get(2));
    }

    @Test
    public void testMap() throws IOException, ReflectiveOperationException {
        var stream = fromInts(0x03, 0x01, 0x11, 0x02, 0x22, 0x03, 0x33);
        var decoder = new AggregateBareDecoder(stream);
        var result = decoder.map(Byte.class, Byte.class);

        assertEquals(3, result.size());
        assertEquals((byte)0x11, result.get((byte)0x01));
        assertEquals((byte)0x22, result.get((byte)0x02));
        assertEquals((byte)0x33, result.get((byte)0x03));
    }

    @Test
    public void testUnion() throws IOException, ReflectiveOperationException {
        var stream = fromInts(0x01, 0x1B, 0xE3, 0x81, 0x93, 0xE3, 0x82, 0x93, 0xE3,
                0x81, 0xAB, 0xE3, 0x81, 0xA1, 0xE3, 0x81, 0xAF, 0xE3, 0x80, 0x81, 0xE4,
                0xB8, 0x96, 0xE7, 0x95, 0x8C, 0xEF, 0xBC, 0x81);

        var decoder = new AggregateBareDecoder(stream);

        var result = decoder.union(Map.of(0L, Float.class, 1L, String.class));
        assertEquals(String.class, result.type());
        assertEquals("こんにちは、世界！", result.get(String.class));


        stream = fromInts(0x00, 0x71, 0x2D, 0xA7, 0x44);
        decoder = new AggregateBareDecoder(stream);
        result = decoder.union(Map.of(0L, Float.class, 1L, String.class));
        assertEquals(Float.class, result.type());
        assertEquals(1337.42, result.get(Float.class), 0.001);

        assertThrows(UnsupportedOperationException.class, () ->  new AggregateBareDecoder(fromInts(0x03, 0x71, 0x2D, 0xA7, 0x44))
                .union(Map.of(0L, Float.class, 1L, String.class)));
    }

    @Test
    public void testStruct() throws ReflectiveOperationException, IOException {
        var stream = fromInts(0x05, 0x50, 0x65, 0x74, 0x65, 0x72,     0x0C, 0x53, 0x70, 0x69, 0x65, 0x73, 0x73, 0x2d, 0x4b, 0x6e, 0x61, 0x66, 0x6c,
                0x02,    0x10, 0x6e, 0x6f, 0x62, 0x6c, 0x6f, 0x61, 0x74, 0x2f, 0x62, 0x61, 0x72, 0x65, 0x2d, 0x6a, 0x76, 0x6d, 0x10, 0x6e, 0x6f, 0x62, 0x6c, 0x6f, 0x61, 0x74, 0x2f, 0x62, 0x61, 0x72, 0x65, 0x2d, 0x6a, 0x64, 0x6b);
        var decoder = new AggregateBareDecoder(stream);
        var person = decoder.struct(Person.class);

        assertEquals("Peter", person.firstName);
        assertEquals("Spiess-Knafl", person.lastName);
        assertEquals("nobloat/bare-jvm", person.repositories.get(0));
        assertEquals("nobloat/bare-jdk", person.repositories.get(1));
    }

    public static class Person {
        public String firstName;
        public String lastName;
        public List<String> repositories;
    }

    @Test
    public void testCustomerStruct() throws IOException, ReflectiveOperationException {
        try(var is = openFile("customer.bin")) {
            var decoder = new AggregateBareDecoder(is);
            var customer = decoder.union(TestClasses.Customer.class).get(TestClasses.Customer.class);

            assertEquals("James Smith", customer.name);
            assertEquals("jsmith@example.org", customer.email);
            assertEquals("123 Main St", customer.address.addressLines.get(0));
            assertEquals("Philadelphia", customer.address.city);
            assertEquals("PA", customer.address.sate);
            assertEquals("United States", customer.address.country);
            assertEquals(1, customer.orders.size());
            assertEquals(4242424242L, customer.orders.get(0).id);
            assertEquals(5, customer.orders.get(0).quantity);
        }
    }

    @Test
    public void testEmployeeStruct() throws IOException, ReflectiveOperationException {
        try(var is = openFile("employee.bin")) {
            var decoder = new AggregateBareDecoder(is);
            var employee = decoder.union(TestClasses.Employee.class).get(TestClasses.Employee.class);
            assertEquals("Tiffany Doe", employee.name);
            assertEquals("tiffanyd@acme.corp", employee.email);
            assertEquals("123 Main St", employee.address.addressLines.get(0));
            assertEquals("Philadelphia", employee.address.city);
            assertEquals("PA", employee.address.sate);
            assertEquals("United States", employee.address.country);
            assertEquals(TestClasses.Department.ADMINISTRATION, employee.department);
            assertEquals("2020-06-21T21:18:05Z", employee.hireDate);
        }
    }

    @Test
    public void testTerminatedStruct() throws IOException, ReflectiveOperationException {
        try(var is = openFile("terminated.bin")) {
            var decoder = new AggregateBareDecoder(is);
            var customer = decoder.union(TestClasses.TerminatedEmployee.class).get(TestClasses.TerminatedEmployee.class);
        }
    }

   /* @Test
    public void testPeople() throws IOException, ReflectiveOperationException {
        try(var is = openFile("people.bin")) {
            var decoder = new AggregateBareDecoder(is);
            var employee = decoder.union(TestClasses.Customer.class).get(TestClasses.Customer.class);

            assertEquals("James Smith", employee.name);
            assertEquals("jsmith@example.org", employee.email);
            assertEquals("123 Main St", employee.address.addressLines.get(0));
            assertEquals("Philadelphia", employee.address.city);
            assertEquals("PA", employee.address.sate);
            assertEquals("United States", employee.address.country);
            assertEquals(1, employee.orders.size());
            assertEquals(4242424242L, employee.orders.get(0).id);
            assertEquals(5, employee.orders.get(0).quantity);
        }
    }*/

    public static InputStream openFile(String name) throws FileNotFoundException {
        String path = "src/test/resources";
        File file = new File(path);
        return new FileInputStream(file.getAbsolutePath() + "/" + name);
    }

}