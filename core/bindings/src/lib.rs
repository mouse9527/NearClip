use clip_core::hello;
#[no_mangle]
pub extern "C" fn clip_core_hello() -> *const u8 {
    hello().as_ptr()
}
