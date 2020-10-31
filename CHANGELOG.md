# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.2.0] - 2020-11-01
### Added
- Possibility to define fully qualified class name (including package) in `CodeGenerator::main`
- `toString()` method for all generated data classes (#10)
- Testsuite for generated encoding/decoding classes out of bare schemas
- Safety checks for bit limits of the various number types
- Limits for maximum number of map and slice entries (`MaxMapLength` and `MaxSliceLength`)

### Changed
- Split the project into two modules: 
  - `com.github.nobloat.bare-jvm:codec` (bare encoder and decoder)
  - `com.github.nobloat.bare-jvm:schema` (code generator and schema parser)
  - `com.github.nobloat.bare-jvm:bare-jvm` (wrapper that includes both modules)

### Fixed
- u32 decoding of `0xFFFFFFFF` resulted in `-1`

## [0.1.0] - 2020-10-18
### Added
- Schema parser
- Code generator for structs, encoding and decoding
- `PrimitiveBareDecoder`
- `PrimitiveBareEncoder`
- `AggregateBareDecoder`
- `AggregateBareEncoder`
- Tests and CI setup
