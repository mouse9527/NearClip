# Clipboard


Clipboard is an experimental project to synchronize clipboard contents across multiple devices. This monorepo contains the Rust core library alongside Android and macOS applications.

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

The Gradle wrapper script is not checked into version control. Run
`gradle wrapper` inside `platforms/android` the first time or use the
system `gradle` command directly.

```bash
cd platforms/android
gradle wrapper    # only needed if ./gradlew does not exist
./gradlew assembleDebug
```

### macOS app

Build the Rust bindings first so the dynamic library is available. The Swift package links
against `libclip_core_bindings` under `core/target/debug`. Run the following commands:

```bash
cargo build -p clip_core_bindings --features ble
cd platforms/mac && DYLD_LIBRARY_PATH=../../core/target/debug swift run
```

You can also install [`just`](https://github.com/casey/just) and run:

```bash
just build-all
```

### GitHub Actions artifacts

Every workflow run uploads the build outputs as artifacts. Visit the run summary
on GitHub Actions to download `android-apk` (the debug APK) and `macos-binary`
(the compiled macOS executable).

### Pairing GUI

An experimental desktop GUI for initiating device pairing is located in
`scripts/pair_gui.py`. Build the bindings crate so the dynamic library is
available and then run the script:

```bash
cargo build -p clip_core_bindings --features ble
python scripts/pair_gui.py
```

Enter the server address and click **Pair** to connect to a remote device.

## Future Goals

This repository currently contains placeholder implementations. Planned work includes secure device pairing, clipboard synchronization across devices, and improved user interfaces on each platform.

## Generating C Bindings

The `clip_core_bindings` crate produces a C header using
[cbindgen](https://github.com/eqrion/cbindgen) during its build script. Run

```bash
cargo build -p clip_core_bindings --features ble
```

from the `core` directory. The resulting `bindings.h` file will appear inside
`core/bindings` once the build completes.

## Design Overview

The `clip_core` crate implements the runtime logic for device pairing and
clipboard message synchronization.  Networking is performed asynchronously with
`tokio` so the same code can run on both servers and client applications.

### Pairing

Pairing is handled by the [`pairing` module](core/clip_core/src/pairing.rs),
which exposes `PairingClient` and `PairingServer` structs.  The client attempts
to connect to a server via TCP while the server waits for incoming connections.
Authentication and persistence are not implemented yet but the stubs show where
the logic will live.

### Synchronization

Message synchronization uses the [`sync` module](core/clip_core/src/sync.rs).
`SyncClient` sends clipboard updates to a remote peer and `SyncServer` listens
for updates.  These interactions are also performed over TCP streams to keep
the example simple.

The C bindings expose three functions:

```c
const uint8_t* clip_core_hello();
int clip_core_pair(const char* addr);
int clip_core_sync_send(const char* addr, const char* msg);
```

The functions create a Tokio runtime internally to execute the asynchronous
operations.

Generated on 2025-07-22

This project is licensed under the MIT License. See the LICENSE file for details.
