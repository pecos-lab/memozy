import Foundation
import UIKit
import GoogleSignIn
import Shared

final class IosGoogleSignInBridge: NSObject, GoogleSignInHandler {
    func signIn(callback: GoogleSignInCallback) {
        let work: () -> Void = { [weak self] in
            guard let rootVC = Self.topViewController() else {
                callback.onError(message: "No presenting view controller")
                return
            }
            GIDSignIn.sharedInstance.signIn(withPresenting: rootVC) { signInResult, error in
                if let error = error as NSError? {
                    if error.domain == kGIDSignInErrorDomain &&
                        error.code == GIDSignInError.canceled.rawValue {
                        callback.onCancelled()
                    } else {
                        callback.onError(message: error.localizedDescription)
                    }
                    return
                }
                guard let idToken = signInResult?.user.idToken?.tokenString else {
                    callback.onError(message: "No id token returned")
                    return
                }
                callback.onSuccess(idToken: idToken)
            }
            _ = self
        }
        // 이미 메인 스레드면 즉시 실행 (DispatchQueue.main.async 의 1프레임 지연 제거)
        if Thread.isMainThread {
            work()
        } else {
            DispatchQueue.main.async { work() }
        }
    }

    private static func topViewController(
        base: UIViewController? = nil
    ) -> UIViewController? {
        let baseVC = base ?? UIApplication.shared.connectedScenes
            .compactMap { $0 as? UIWindowScene }
            .first?.windows
            .first(where: { $0.isKeyWindow })?
            .rootViewController
        if let nav = baseVC as? UINavigationController {
            return topViewController(base: nav.visibleViewController)
        }
        if let tab = baseVC as? UITabBarController, let selected = tab.selectedViewController {
            return topViewController(base: selected)
        }
        if let presented = baseVC?.presentedViewController {
            return topViewController(base: presented)
        }
        return baseVC
    }
}
