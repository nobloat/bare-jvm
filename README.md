#bare-jvm

This is an implementation of [baremessages](https://baremessages.org/) for the JVM.

## Features
- Decoding primitive datatypes from an InputStream
- Decoding aggregate datatype from an InputStream

## Problems
- Java has no concept of unsigned primitive data types, hence types like u64 or u32 need to be converted to BigInteger or Integer.