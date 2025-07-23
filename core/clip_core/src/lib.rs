//! Core library for Clipboard

pub mod pairing;
pub mod sync;
pub mod ble;

/// Example function verifying the library was linked correctly.
pub fn hello() -> &'static str {
    "Hello from clip_core!"
}
