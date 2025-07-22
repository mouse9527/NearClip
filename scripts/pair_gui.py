import ctypes
import os
from tkinter import Tk, Label, Entry, Button, StringVar, messagebox


def _load_library() -> ctypes.CDLL:
    """Load the clip_core_bindings dynamic library."""
    lib_name = {
        'darwin': 'libclip_core_bindings.dylib',
        'linux': 'libclip_core_bindings.so',
        'win32': 'clip_core_bindings.dll',
    }.get(os.sys.platform)
    if lib_name is None:
        raise RuntimeError(f"Unsupported platform: {os.sys.platform}")

    # Look for the library inside core/bindings/target/debug by default
    repo_root = os.path.abspath(os.path.join(os.path.dirname(__file__), '..'))
    candidate = os.path.join(
        repo_root, 'core', 'bindings', 'target', 'debug', lib_name
    )
    if not os.path.exists(candidate):
        raise FileNotFoundError(
            f"Could not find {lib_name}. Build the bindings crate first."
        )
    return ctypes.CDLL(candidate)


lib = _load_library()
_pair = lib.clip_core_pair
_pair.argtypes = [ctypes.c_char_p]
_pair.restype = ctypes.c_int


def pair_device(addr: str) -> bool:
    result = _pair(addr.encode())
    return result == 0


def main() -> None:
    root = Tk()
    root.title("ClipSync Pairing")

    Label(root, text="Server address:").grid(row=0, column=0, padx=5, pady=5)
    addr_var = StringVar()
    Entry(root, textvariable=addr_var, width=40).grid(row=0, column=1, padx=5, pady=5)

    def do_pair():
        addr = addr_var.get().strip()
        if not addr:
            messagebox.showerror("Pairing", "Please enter an address")
            return
        if pair_device(addr):
            messagebox.showinfo("Pairing", "Pairing successful")
        else:
            messagebox.showerror("Pairing", "Pairing failed")

    Button(root, text="Pair", command=do_pair).grid(row=1, column=0, columnspan=2, pady=10)
    root.mainloop()


if __name__ == "__main__":
    main()
