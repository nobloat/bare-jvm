# bare-jvm
[![CircleCI](https://circleci.com/gh/nobloat/bare-jvm.svg?style=svg)](https://circleci.com/gh/nobloat/bare-jvm)
[![](https://jitpack.io/v/nobloat/bare-jvm.svg)](https://jitpack.io/#nobloat/bare-jvm)
[![codecov](https://codecov.io/gh/nobloat/bare-jvm/branch/master/graph/badge.svg)](https://codecov.io/gh/nobloat/bare-jvm)
[![](https://tokei.rs/b1/github.com/nobloat/bare-jvm?category=code)](https://github.com/XAMPPRocky/tokei)

![bare-jvm-logo](logo.svg)

This is a [baremessages](https://baremessages.org/) implementation for the JVM.

## Features
- Decoding primitive data types from `InputStream`
- Decoding aggregate data types from `InputStream`
- Encoding primitive data types to `OutputStream`

## Type mappings

### Primitive type mappings

| bare-jvm                        | bare spec      |
|---------------------------------|----------------|
| `String`                        | `string`       |
| `Byte`                          | `byte`         |
| `Boolean`                       | `bool`         |
| `List<Byte>`                    | `data`         |
| `Array<Byte`                    | `data<length>` |
| `Float`                         | `f32`          |
| `Double`                        | `f64`          |
| `@Int(Int.Type.i64) Long`       | `i64`          |
| `@Int(Int.Type.u64) BigInteger` | `u64`          |
| `@Int(Int.Type.i32) Integer`    | `i32`          |
| `@Int(Int.Type.u32) Long`       | `u32`          |
| `@Int(Int.Type.i16) Short`      | `i16`          |
| `@Int(Int.Type.u16) Integer`     | `i16`          |
| `@Int(Int.Type.u8) Byte`         | `u8`           |
| `@Int(Int.Type.i8) Byte`         | `i8`           |
| `@Int(Int.Type.i) Long`         | `int`          |
| `@Int(Int.Type.i) BigInteger`    | `uint`         |

### Aggregate types

| bare-jvm                        | bare spec      |
|---------------------------------|----------------|
| `List<T>`                        | `[]type`       |
| `Array<T>`                        | `[length]type`       |
| `Map<K,V>`                        | `map[type A]type B`       |
| `Optional<T>`                        | `optional<type>`       |
| `class Person {public String name;}`                        | `struct`       |
| `Union u = new Union(Person.class, Account.class)`                        | `tagged union`       |

### Unions

**Defining inline tag IDs**
```java
decoder.union(Map.of(0L, Float.class, 1L, String.class));
```

**Defining tag IDs on class level**
```java
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

public static class CustomerOrEmployee { public Union person = new Union(Customer.class, Employee.class); }
```



### Enums

```java
public enum Department {
    ACCOUNTING(0), ADMINISTRATION(1), CUSTOMER_SERVICE(2), DEVELOPMENT(3), JSMITH(99);
    public int value;
    Department(int value) {
        this.value= value;
    }
}
```

## Limitations
- Java primitive types do not work, please use the respective wrapper types (e.g. `int` -> `Integer`)
    - **Rationale:** Java cannot reflect on primitive types.
- All class fields need to be public
    - **Rationale:** It simplifies the reflective code a lot.
- Enum's are currently always set to null
    - **Rationale:** Reflection and enums in Java are a mess.

## Problems
- Java has no concept of unsigned primitive data types, hence double the amount of memory is required to safely use unsigned types:
    - `u64` becomes `BigInteger`
    - `u32` becomes `Integer`
    - `u16` becomes `int`
    
