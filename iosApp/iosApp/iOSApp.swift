import SwiftUI
import FirebaseCore
import FirebaseAnalytics
import GoogleSignIn
import Shared

@main
struct iOSApp: App {
    init() {
        // Firebase 초기화는 Koin 보다 먼저
        if FirebaseApp.app() == nil {
            FirebaseApp.configure()
        }

        SharedKoinKt.doInitKoin()
        GoogleSignInRegistrar.shared.handler = IosGoogleSignInBridge()
        AnalyticsRegistrar.shared.bridge = IosFirebaseAnalyticsBridge()
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
