// swift-tools-version:5.10
import PackageDescription
import Foundation

let package = Package(
    name: "ClipboardMac",
    platforms: [.macOS(.v14)],
    products: [
        .executable(name: "ClipboardMac", targets: ["App"]),
    ],
    dependencies: [],
    targets: [
        .executableTarget(
            name: "App",
            path: "Sources/App",
            linkerSettings: {
                let root = URL(fileURLWithPath: #filePath).deletingLastPathComponent()
                let libPath = root.appendingPathComponent("../../core/bindings/target/debug").path
                return [ .unsafeFlags(["-L\(libPath)", "-lclip_core_bindings"]) ]
            }()
        )
    ]
)
