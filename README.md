**THIS IS WORK IN PROGRESS - DONT USE IT IN PRODUCTION**

# bare-jvm
[![CircleCI](https://circleci.com/gh/nobloat/bare-jvm.svg?style=svg)](https://circleci.com/gh/nobloat/bare-jvm)
[![](https://jitpack.io/v/nobloat/bare-jvm.svg)](https://jitpack.io/#nobloat/bare-jvm)
[![codecov](https://codecov.io/gh/nobloat/bare-jvm/branch/master/graph/badge.svg)](https://codecov.io/gh/nobloat/bare-jvm)
[![](https://tokei.rs/b1/github.com/nobloat/bare-jvm?category=code)](https://github.com/XAMPPRocky/tokei)

![bare-jvm-logo](logo.svg)

This is a [bare messages](https://baremessages.org/) implementation for the JVM.

## Features
- Parsing of [bare schemas](https://baremessages.org/)
- [Code generation](src/main/java/org/nobloat/bare/gen/CodeGenerator.java) for data structures, encoding and decoding methods out ouf [bare schemas](https://baremessages.org/) 

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

Simply copy the required classes from `org.nobloat.bare` or add the dependency via [![](https://jitpack.io/v/nobloat/bare-jvm.svg)](https://jitpack.io/#nobloat/bare-jvm)



## Limitations
- Java primitive types do not work, please use the respective wrapper types (e.g. `int` -> `Integer`)
    - **Rationale:** Java cannot reflect on primitive types.
- All class fields need to be public
    - **Rationale:** It simplifies the reflective code a lot.
- Enum's are currently always set to null
    - **Rationale:** Reflection and enums in Java are a mess.
- Anonymous structs are not supported by the code generator.

## Problems
- Java has no concept of unsigned primitive data types, hence double the amount of memory is required to safely use unsigned types:
    - `u64` becomes `BigInteger`
    - `u32` becomes `long`
    - `u16` becomes `int`
    - `u8` becomes `short`
    
