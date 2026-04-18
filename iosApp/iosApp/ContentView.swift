import SwiftUI
import Shared

struct ContentView: View {
    private let viewModel: MainViewModel = SharedKoinKt.provideMainViewModel()

    var body: some View {
        VStack(spacing: 16) {
            Text("Memozy iOS shell")
                .font(.title2)
                .bold()
            Text("KMP framework wired")
            Text("MainViewModel: \(String(describing: type(of: viewModel)))")
                .font(.caption)
                .foregroundColor(.secondary)
        }
        .padding()
    }
}

#Preview {
    ContentView()
}
