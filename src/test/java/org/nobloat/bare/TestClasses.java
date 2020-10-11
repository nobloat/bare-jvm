package org.nobloat.bare;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class TestClasses {

    @Union.Id(0)
    public static class Customer {
        public String name;
        public String email;
        public Address address;
        public List<Order> orders;
        public Map<String,String> metadata;
    }

    @Union.Id(1)
    public static class Employee {
        public String name;
        public String email;
        public Address address;
        public Department department;
        public String hireDate;
        public List<Byte> publicKey;
        public Map<String,String> metadata;
    }

    @Union.Id(2)
    public static class TerminatedEmployee {

    }

    public static class Address {
        public Array<String> addressLines = new Array<>(4);
        public String city;
        public String sate;
        public String country;
    }

    public static class Order {
        @Int(Int.Type.i64)
        public Long id;
        @Int(Int.Type.i32)
        public Integer quantity;
    }

    public enum Department {
        ACCOUNTING(0), ADMINISTRATION(1), CUSTOMER_SERVICE(2), DEVELOPMENT(3), JSMITH(99);

        public int value;

        Department(int value) {
            this.value= value;
        }
    }

}
