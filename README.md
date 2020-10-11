# bare-jvm
[![CircleCI](https://circleci.com/gh/nobloat/bare-jvm.svg?style=svg)](https://circleci.com/gh/nobloat/bare-jvm)
[![](https://jitpack.io/v/nobloat/bare-jvm.svg)](https://jitpack.io/#nobloat/bare-jvm)
[![codecov](https://codecov.io/gh/nobloat/bare-jvm/branch/master/graph/badge.svg)](https://codecov.io/gh/nobloat/bare-jvm)

![bare-jvm-logo](logo.svg)

This is an implementation of [baremessages](https://baremessages.org/) for the JVM.

## Features
- Decoding primitive data types from `InputStream`
- Decoding aggregate data types from `InputStream`
- Encoding aggregate data types to `OutputStream`

## Problems
- Java has no concept of unsigned primitive data types, hence double the amount of memory is required to safely use unsigned types:
    - `u64` becomes `BigInteger`
    - `u32` becomes `Integer`
    - `u16` becomes `int`