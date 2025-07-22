use std::path::PathBuf;

fn main() {
    let crate_dir = std::env::var("CARGO_MANIFEST_DIR").unwrap();
    let out_path = PathBuf::from(&crate_dir).join("bindings.h");

    cbindgen::generate(&crate_dir)
        .expect("Unable to generate bindings")
        .write_to_file(out_path);
}
