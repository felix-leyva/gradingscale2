import SwiftUI
import FirebaseCore
import ComposeApp

class AppDelegate: NSObject, UIApplicationDelegate {
  func application(_ application: UIApplication,
                   didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey : Any]? = nil) -> Bool {
    FirebaseApp.configure()
    DependenciesProviderHelper().setupKoin()
    return true
  }
}

@main
struct iOSApp: App {
   @UIApplicationDelegateAdaptor(AppDelegate.self) var delegate


    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}

func get<A: AnyObject>() -> A {
    return DependenciesProviderHelper.companion.koin.get(objCClass: A.self) as! A
}

func get<A: AnyObject>(_ type: A.Type) -> A {
    return DependenciesProviderHelper.companion.koin.get(objCClass: A.self) as! A
}

func get<A: AnyObject>(_ type: A.Type, qualifier: (any Koin_coreQualifier)? = nil, parameter: Any) -> A {
    return DependenciesProviderHelper.companion.koin.get(objCClass: A.self, qualifier: qualifier, parameter: parameter) as! A
}
