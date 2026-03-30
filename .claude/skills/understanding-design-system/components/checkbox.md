# Checkbox

A control that allows the user to toggle between checked and not checked.

## Basic Usage

```kotlin
import com.shadcn.ui.components.Checkbox

@Composable
fun Example() {
    var checkedState by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Checkbox(
                checked = checkedState,
                onCheckedChange = { checkedState = it }
            )
            Text("Basic Checkbox")
        }
    }
}
```

## Disabled

```kotlin
import com.shadcn.ui.components.Checkbox

fun Example() {
    var checkedState by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Checkbox(
                checked = checkedState,
                onCheckedChange = { checkedState = it },
                enabled = false
            )
            Text("Disabled Checkbox")
        }
    }
}
```

## Custom Styles

```kotlin
import com.shadcn.ui.components.Checkbox

fun Example() {
    var checkedState by remember { mutableStateOf(false) }
    val borderColor = if (checkedState) {
        MaterialTheme.colors.chart3
    } else {
        MaterialTheme.colors.border
    }
    val background = if (checkedState) {
        MaterialTheme.colors.chart1
    } else {
        MaterialTheme.colors.background
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .border(1.dp, borderColor, MaterialTheme.shapes.small)
                .background(background, MaterialTheme.shapes.small)
        ) {
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(12.dp)
            ) {
                Checkbox(
                    checked = checkedState,
                    onCheckedChange = { checkedState = it },
                    colors = CheckboxDefaults.colors().copy(
                        checkedColors = MaterialTheme.colors.chart3,
                        checkedBorderColors = MaterialTheme.colors.chart3
                    )
                )
                Column {
                    Text("Enable Notification")
                    Text(
                        "You can enable or disable anytime",
                        color = MaterialTheme.colors.mutedForeground
                    )
                }
            }
        }
    }
}
```

> 코드 예시는 공식 문서에서 확인: [Checkbox](https://shadcn-compose.site/docs/components/checkbox)
