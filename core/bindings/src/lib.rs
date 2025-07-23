use clip_core::{hello, pairing::PairingClient, sync::SyncClient, ble::BleClient};
use std::ffi::{c_char, CStr};

fn runtime() -> tokio::runtime::Runtime {
    tokio::runtime::Builder::new_multi_thread()
        .enable_all()
        .build()
        .expect("failed to build runtime")
}
#[no_mangle]
pub extern "C" fn clip_core_hello() -> *const u8 {
    hello().as_ptr()
}

/// Initiates pairing with a remote device. Returns 0 on success.
#[no_mangle]
pub extern "C" fn clip_core_pair(addr: *const c_char) -> i32 {
    let addr = unsafe { CStr::from_ptr(addr) };
    let Ok(addr) = addr.to_str() else { return -1 };
    let rt = runtime();
    let client = PairingClient::new(addr);
    match rt.block_on(client.pair()) {
        Ok(_) => 0,
        Err(_) => -1,
    }
}

/// Sends a clipboard message to a remote device. Returns 0 on success.
#[no_mangle]
pub extern "C" fn clip_core_sync_send(addr: *const c_char, msg: *const c_char) -> i32 {
    let addr = unsafe { CStr::from_ptr(addr) };
    let msg = unsafe { CStr::from_ptr(msg) };
    let (Ok(addr), Ok(msg)) = (addr.to_str(), msg.to_str()) else { return -1 };
    let rt = runtime();
    let client = SyncClient::new(addr);
    match rt.block_on(client.send_message(msg)) {
        Ok(_) => 0,
        Err(_) => -1,
    }
}

/// Performs BLE pairing with a nearby device. Returns 0 on success.
#[no_mangle]
pub extern "C" fn clip_core_ble_pair() -> i32 {
    let rt = runtime();
    let client = BleClient::default();
    match rt.block_on(client.pair()) {
        Ok(_) => 0,
        Err(_) => -1,
    }
}

/// Sends a message via BLE to a paired device. Returns 0 on success.
#[no_mangle]
pub extern "C" fn clip_core_ble_send(msg: *const c_char) -> i32 {
    let msg = unsafe { CStr::from_ptr(msg) };
    let Ok(msg) = msg.to_str() else { return -1 };
    let rt = runtime();
    let client = BleClient::default();
    match rt.block_on(client.send_message(msg.as_bytes())) {
        Ok(_) => 0,
        Err(_) => -1,
    }
}
