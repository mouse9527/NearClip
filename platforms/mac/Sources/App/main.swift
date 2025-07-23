import SwiftUI

@_silgen_name("clip_core_ble_pair")
func clip_core_ble_pair() -> Int32

@_silgen_name("clip_core_ble_send")
func clip_core_ble_send(_ msg: UnsafePointer<CChar>) -> Int32

struct ContentView: View {
    @State private var status = "Not paired"
    @State private var message = ""

    var body: some View {
        VStack(spacing: 12) {
            Text("BLE Pairing Demo")
            Button("Pair") {
                if clip_core_ble_pair() == 0 {
                    status = "Paired"
                } else {
                    status = "Pair failed"
                }
            }
            Text(status)
            TextField("Message", text: $message)
                .textFieldStyle(.roundedBorder)
            Button("Send") {
                message.withCString { ptr in
                    if clip_core_ble_send(ptr) == 0 {
                        status = "Sent";
                    } else {
                        status = "Send failed";
                    }
                }
            }
        }
        .padding()
        .frame(width: 300)
    }
}

@main
struct ClipboardMacApp: App {
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
