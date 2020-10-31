package org.nobloat.bare;

import org.junit.jupiter.api.Test;
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
        assertEquals(address.address.length, decodedAddress.address.length);
        assertArrayEquals(address.address, decodedAddress.address);


        assertEquals(address.city, decodedAddress.city);
        assertEquals(address.state, decodedAddress.state);
        assertEquals(address.country, decodedAddress.country);

        assertEquals(employee.department, decodedEmployee.department);

        assertEquals(employee.hireDate.value, decodedEmployee.hireDate.value);

        assertEquals(employee.publicKey.isPresent(), decodedEmployee.publicKey.isPresent());
        assertArrayEquals(employee.publicKey.get().value, decodedEmployee.publicKey.get().value);


        assertEquals(employee.metadata.size(), decodedEmployee.metadata.size());
        assertArrayEquals(employee.metadata.get("key1"), decodedEmployee.metadata.get("key1"));
    }

    @Test
    public void testToString() {

        String str = "0x00 ".repeat(128);

        assertEquals("Employee{height=0x02, " +
                        "name=asdf, " +
                        "email=asdf@org.com, " +
                        "address=Address{address=[Breiten Straße 23, , , ], city=Villach, state=Austria, country=Kärnten}, " +
                        "department=ADMINISTRATION, hireDate=Time{value=23.23.1999}, " +
                        "arbitraryData=[0x80 0x03], " +
                        "publicKey=Optional[PublicKey{value=["+str.substring(0, str.length()-1)+"]}], " +
                        "metadata={key1=[0x80]}}",
                createEmployee().toString());
    }

    private Dtos.Employee createEmployee() {
        Dtos.Employee employee = new Dtos.Employee();
        employee.height = 2;
        employee.name = "asdf";
        employee.email = "asdf@org.com";
        employee.arbitraryData = new Byte[]{(byte) 0x80, 0x3};

        Dtos.Address address = new Dtos.Address();
        address.address = new String[]{"Breiten Straße 23", "","",""};
        address.city = "Villach";
        address.state = "Austria";
        address.country = "Kärnten";

        employee.address = address;

        employee.department = Dtos.Department.ADMINISTRATION;

        Dtos.Time time = new Dtos.Time();
        time.value = "23.23.1999";

        employee.hireDate = time;

        Dtos.PublicKey publicKey = new Dtos.PublicKey();

        employee.publicKey = Optional.of(publicKey);
        
        employee.metadata = new HashMap<>();
        employee.metadata.put("key1", new Byte[]{(byte) 0x80});

        return employee;
    }
}
