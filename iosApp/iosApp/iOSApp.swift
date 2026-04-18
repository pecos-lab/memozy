import SwiftUI
import Shared

@main
struct iOSApp: App {
    init() {
        SharedKoinKt.doInitKoin()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
