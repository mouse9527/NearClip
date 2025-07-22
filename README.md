# ClipSync Monorepo Skeleton

Generated on 2025-07-22

## Generating C Bindings

The `clip_core_bindings` crate produces a C header using
[cbindgen](https://github.com/eqrion/cbindgen) during its build script. Run

```bash
cargo build -p clip_core_bindings
```

from the `core` directory. The resulting `bindings.h` file will appear inside
`core/bindings` once the build completes.
