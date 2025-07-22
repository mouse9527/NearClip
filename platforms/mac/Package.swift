// swift-tools-version:5.10
import PackageDescription

let package = Package(
    name: "ClipSyncMac",
    platforms: [.macOS(.v14)],
    products: [
        .executable(name: "ClipSyncMac", targets: ["App"]),
    ],
    dependencies: [],
    targets: [
        .executableTarget(
            name: "App",
            path: "Sources/App"
        )
    ]
)
