// swift-tools-version:5.10
import PackageDescription

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
            path: "Sources/App"
        )
    ]
)
