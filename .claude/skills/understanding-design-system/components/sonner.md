# Sonner

An opinionated toast component for Jetpack Compose.

## Configuration

To use a Sonner (snackbar) shadcn compose, you need to use `Scaffold` as a root composable component. The Sonner will be registered as global component.

```kotlin
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import com.shadcn.ui.themes.ShadcnTheme
import com.shadcn.ui.components.sooner.ObserveAsEvent
import com.shadcn.ui.components.sooner.SonnerHost
import com.shadcn.ui.components.sooner.SonnerProvider
import com.shadcn.ui.components.sooner.showSonner

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ShadcnTheme {
                val scope = rememberCoroutineScope()
                val snackbarHostState = remember { SnackbarHostState() }
                ObserveAsEvent(SonnerProvider.events) { event ->
                    scope.launch {
                        snackbarHostState.currentSnackbarData?.dismiss()
                        val result = snackbarHostState.showSonner(event)

                        if (result == SnackbarResult.ActionPerformed) {
                            event.action?.execute()
                        }
                    }
                }
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    snackbarHost = {
                        SonnerHost(hostState = snackbarHostState)
                    },
                ) { ip ->
                    // Your entire app navigation or root layout
                    AppNavigation(modifier = Modifier.padding(ip))
                }
            }
        }
    }
}
```

## Basic Usage

```kotlin
import com.shadcn.ui.components.Button
import com.shadcn.ui.components.sooner.SonnerAction
import com.shadcn.ui.components.sooner.SonnerProvider

@Composable
fun ExamplePage() {
    var showDropdown by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 16.dp, end = 16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(
            onClick = {
                scope.launch {
                    SonnerProvider.showMessage(
                        "This is a snackbar",
                        "Something important just happened."
                    )
                }
            }
        ) {
            Text("Open snackbar")
        }
    }
}
```

## Destructive

```kotlin
import com.shadcn.ui.components.Button
import com.shadcn.ui.components.sooner.SonnerAction
import com.shadcn.ui.components.sooner.SonnerProvider

@Composable
fun ExamplePage() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 16.dp, end = 16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(
            onClick = {
                scope.launch {
                    SonnerProvider.showError(
                        "This is error event",
                        "Something went wrong with your request",
                        withDismissAction = true,
                    )
                }
            }
        ) {
            Text("Open snackbar")
        }
    }
}
```

## With Action

```kotlin
import com.shadcn.ui.components.Button
import com.shadcn.ui.components.sooner.SonnerAction
import com.shadcn.ui.components.sooner.SonnerProvider

@Composable
fun ExamplePage() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 16.dp, end = 16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(
            onClick = {
                scope.launch {
                    SonnerProvider.showMessage(
                        "This is a snackbar",
                        "Something important just happened.",
                        action = SonnerAction("Action") {
                            Toast.makeText(context, "Action clicked", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        ) {
            Text("Open snackbar with action")
        }
    }
}
```

> 코드 예시는 공식 문서에서 확인: [Sonner](https://shadcn-compose.site/docs/components/sonner)
