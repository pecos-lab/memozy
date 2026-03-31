# Switch

A control that allows the user to toggle between checked and not checked.

## Usage

```kotlin
import com.shadcn.ui.components.Switch

@Composable
fun Example() {
    var switchChecked by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 24.dp, start = 16.dp, end = 16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Switch(
                checked = switchChecked,
                onCheckedChange = { switchChecked = it }
            )
            Text("Enable feature", color = MaterialTheme.colors.foreground)
        }
    }
}
```

## Disabled

```kotlin
import com.shadcn.ui.components.Switch

@Composable
fun Example() {
    var switchChecked by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 24.dp, start = 16.dp, end = 16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Switch(
                checked = switchChecked,
                onCheckedChange = { switchChecked = it },
                enabled = false
            )
            Text("Disabled option", color = MaterialTheme.colors.mutedForeground)
        }
    }
}
```

> 코드 예시는 공식 문서에서 확인: [Switch](https://shadcn-compose.site/docs/components/switch)
