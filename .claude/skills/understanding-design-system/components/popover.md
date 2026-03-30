# Popover

Displays rich content in a portal, triggered by a button.

## Usage

```kotlin
import com.shadcn.ui.components.Button
import com.shadcn.ui.components.ButtonSize
import com.shadcn.ui.components.ButtonVariant
import com.shadcn.ui.components.Popover

@Composable
fun Example() {
    var showPopover by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 24.dp, start = 16.dp, end = 16.dp),
    ) {
        Popover(
            open = showPopover,
            trigger = {
                Button(onClick = { showPopover = !showPopover }) {
                    Text("Open Popover")
                }
            },
        ) {
            Column(
                modifier = Modifier.padding(8.dp), // Inner padding for content
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Place any content here.")
                Text("This is your popover content.")
                Button(
                    onClick = { showPopover = false },
                    variant = ButtonVariant.Secondary,
                    size = ButtonSize.Sm
                ) {
                    Text("Close")
                }
            }
        }
    }
}
```

> 코드 예시는 공식 문서에서 확인: [Popover](https://shadcn-compose.site/docs/components/popover)
