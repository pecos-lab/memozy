import SwiftUI
import GoogleSignIn
import Shared

@main
struct iOSApp: App {
    init() {
        SharedKoinKt.doInitKoin()
        GoogleSignInRegistrar.shared.handler = IosGoogleSignInBridge()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
                .onOpenURL { url in
                    GIDSignIn.sharedInstance.handle(url)
                }
        }
    }
}
