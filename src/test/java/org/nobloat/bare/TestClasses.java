package org.nobloat.bare;


import java.util.List;
import java.util.Map;

public class TestClasses {

    @Union.Id(0)
    public static class Customer {
        public String name;
        public String email;
        public Address addres;
        public List<Order> orders;
        public Map<String,String> metadata;
    }

    public static class Address {
        public Array<String> addressLines = new Array<>(4);
        public String city;
        public String sate;
        public String country;
    }

    public static class Order {
        public Long id;
        public Integer quantity;
    }

}
