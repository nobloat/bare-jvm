# bare-jvm
[![CircleCI](https://circleci.com/gh/nobloat/bare-jvm.svg?style=svg)](https://circleci.com/gh/nobloat/bare-jvm)
[![](https://jitpack.io/v/nobloat/bare-jvm.svg)](https://jitpack.io/#nobloat/bare-jvm)
[![codecov](https://codecov.io/gh/nobloat/bare-jvm/branch/master/graph/badge.svg)](https://codecov.io/gh/nobloat/bare-jvm)
[![](https://tokei.rs/b1/github.com/nobloat/bare-jvm?category=code)](https://github.com/XAMPPRocky/tokei)

![bare-jvm-logo](logo.svg)

This is a [bare messages](https://baremessages.org/) implementation for the JVM.

## Features
- Zero external dependencies and small, well structured code base
- Parsing of [bare schemas](https://baremessages.org/)
- [Code generation](schema/src/main/java/org/nobloat/bare/org.nobloat.bare.gen/CodeGenerator.java) for data structures, encoding and decoding methods out ouf [bare schemas](https://baremessages.org/) 
- Decoding primitive data types from `InputStream`
- Decoding aggregate data types from `InputStream`
- Encoding primitive data types to `OutputStream`
- Encoding aggregate data types to `OutputStream`
- Reflective decoding of data types from `InputStream`

## Type mappings

### Primitive type mappings

| bare-jvm                        | bare spec      |
|---------------------------------|----------------|
| `String`                        | `string`       |
| `byte`                          | `byte`         |
| `boolean`                       | `bool`         |
| `byte[]`                    | `data`         |
| `byte[]`                    | `data<length>` |
| `float`                         | `f32`          |
| `double`                        | `f64`          |
| `@Int(Int.Type.i64) long`       | `i64`          |
| `@Int(Int.Type.u64) BigInteger` | `u64`          |
| `@Int(Int.Type.i32) int`    | `i32`          |
| `@Int(Int.Type.u32) long`       | `u32`          |
| `@Int(Int.Type.i16) short`      | `i16`          |
| `@Int(Int.Type.u16) int`     | `i16`          |
| `@Int(Int.Type.u8) byte`         | `u8`           |
| `@Int(Int.Type.i8) byte`         | `i8`           |
| `@Int(Int.Type.i) long`         | `int`          |
| `@Int(Int.Type.ui) BigInteger`    | `uint`         |

### Aggregate types

| bare-jvm                        | bare spec      |
|---------------------------------|----------------|
| `List<T>`                        | `[]type`       |
| `Array<T>`                        | `[length]type`       |
| `Map<K,V>`                        | `map[type A]type B`       |
| `Optional<T>`                        | `optional<type>`       |
| `class Person {public String name;}`                        | `struct`       |
| `Union u = new Union(Person.class, Account.class)`                        | `tagged union`       |


## Usage

- Simply copy the required classes from `org.nobloat.bare` or add the dependency via [![](https://jitpack.io/v/nobloat/bare-jvm.svg)](https://jitpack.io/#nobloat/bare-jvm)

- Generate the data structures, encoding and decoding structures with
  - `java -jar bare-jvm.jar schema.bare org.example.Messages`
  - This will create all data types in a class `Messages` under `org/example/Messages.java`

## Limitations
- Java has no concept of unsigned primitive data types, hence double the amount of memory is required to safely use unsigned types:
    - `u64` becomes `BigInteger`
    - `u32` becomes `long`
    - `u16` becomes `int`
    - `u8` becomes `short`
- Anonymous structs are not supported by the code generator.
