# ClipSync

ClipSync is an experimental project to synchronize clipboard contents across multiple devices. This monorepo contains the Rust core library alongside Android and macOS applications.

## Project Structure

- `core/` - Rust workspace containing:
  - `clip_core/` - main library with application logic.
  - `bindings/` - C-compatible bindings used by platform code.
- `platforms/`
  - `android/` - Android app built with Gradle.
  - `mac/` - Swift Package-based macOS app.
- `scripts/justfile` - helper commands for building each component.

## Building

The following commands mirror the recipes in `scripts/justfile`.

### Rust core

```bash
cargo build -p clip_core --manifest-path core/Cargo.toml
```

### Android app

```bash
cd platforms/android && ./gradlew assembleDebug
```

### macOS app

```bash
cd platforms/mac && swift run
```

You can also install [`just`](https://github.com/casey/just) and run:

```bash
just build-all
```

## Future Goals

This repository currently contains placeholder implementations. Planned work includes secure device pairing, clipboard synchronization across devices, and improved user interfaces on each platform.
