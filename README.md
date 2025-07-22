# ClipSync Monorepo Skeleton

Generated on 2025-07-22

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
