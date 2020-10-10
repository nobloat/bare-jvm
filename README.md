# bare-jvm

![](logo.svg)

This is an implementation of [baremessages](https://baremessages.org/) for the JVM.

## Features
- Decoding primitive datatypes from `InputStream`
- Decoding aggregate datatype from `InputStream`

## Problems
- Java has no concept of unsigned primitive data types, hence double the amount of memory is required to safely use unsigned types:
    - `u64` becomes `BigInteger`
    - `u32` becomes `Integer`
    - `u16` becomes `int`