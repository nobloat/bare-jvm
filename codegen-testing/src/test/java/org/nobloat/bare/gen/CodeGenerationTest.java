package org.nobloat.bare.gen;

import org.junit.jupiter.api.Test;
import org.nobloat.bare.AggregateBareDecoder;
import org.nobloat.bare.AggregateBareEncoder;
import org.nobloat.bare.Array;
import org.nobloat.bare.BareException;
import org.nobloat.bare.test.Dtos;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CodeGenerationTest {

    @Test
    public void testEncodeAndDecode() throws IOException, BareException {
        Dtos.Employee employee = createEmployee();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        AggregateBareEncoder encoder = new AggregateBareEncoder(outputStream);

        employee.encode(encoder);

        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
        AggregateBareDecoder decoder = new AggregateBareDecoder(inputStream);

        Dtos.Employee decodedEmployee = Dtos.Employee.decode(decoder);

        assertEquals(employee.height, decodedEmployee.height);
        assertEquals(employee.name, decodedEmployee.name);
        assertEquals(employee.email, decodedEmployee.email);

        assertArrayEquals(employee.arbitraryData, decodedEmployee.arbitraryData);

        Dtos.Address address = employee.address;
        Dtos.Address decodedAddress = decodedEmployee.address;
        assertEquals(address.address.size, decodedAddress.address.size);
//    TODO assert array
        assertEquals(address.city, decodedAddress.city);
        assertEquals(address.state, decodedAddress.state);
        assertEquals(address.country, decodedAddress.country);

        assertEquals(employee.department, decodedEmployee.department);

        assertEquals(employee.hireDate.value, decodedEmployee.hireDate.value);

        assertEquals(employee.publicKey.isPresent(), decodedEmployee.publicKey.isPresent());
        // TODO assert array

        assertEquals(employee.metadata.size(), decodedEmployee.metadata.size());
        assertArrayEquals(employee.metadata.get("key1"), decodedEmployee.metadata.get("key1"));
    }

    @Test
    public void testToString() {
        assertEquals("Employee{height=0x02, " +
                        "name=asdf, " +
                        "email=asdf@org.com, " +
                        "address=Address{address=Array{size=4, values=[Breiten Straße 23, null, null, null]}, city=Villach, state=Austria, country=Kärnten}, " +
                        "department=ADMINISTRATION, hireDate=Time{value=23.23.1999}, " +
                        "arbitraryData=0x80 0x03, " +
                        "publicKey=Optional[PublicKey{value=Array{size=3, values=[null, null, null]}}], " +
                        "metadata={key1=[B@35a50a4c}}",
                createEmployee().toString());
        
        // fails because of metadata's byte[] prints object id
    }

    private Dtos.Employee createEmployee() {
        Dtos.Employee employee = new Dtos.Employee();
        employee.height = 2;
        employee.name = "asdf";
        employee.email = "asdf@org.com";
        employee.arbitraryData = new byte[]{(byte) 0x80, 0x3};

        Dtos.Address address = new Dtos.Address();
        address.address.set(0, "Breiten Straße 23");
        address.city = "Villach";
        address.state = "Austria";
        address.country = "Kärnten";

        employee.address = address;

        employee.department = Dtos.Department.ADMINISTRATION;

        Dtos.Time time = new Dtos.Time();
        time.value = "23.23.1999";

        employee.hireDate = time;

        Dtos.PublicKey publicKey = new Dtos.PublicKey();
        publicKey.value = new Array<>(3);

        employee.publicKey = Optional.of(publicKey);
        
        employee.metadata = new HashMap<>();
        employee.metadata.put("key1", new byte[]{(byte) 0x80});

        return employee;
    }
}
