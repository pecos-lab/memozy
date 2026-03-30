# Slider

An input where the user selects a value from within a given range.

## Usage

```kotlin
import com.shadcn.ui.components.Slider

@Composable
fun Example() {
    var sliderValue by remember { mutableFloatStateOf(0f) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 24.dp, start = 16.dp, end = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Slider(
            value = sliderValue,
            onValueChange = { sliderValue = it },
            valueRange = 0f..20f,
            steps = 0
        )
        Text(
            text = "Value: %.0f".format(sliderValue),
            color = MaterialTheme.colors.foreground,
            fontSize = 14.sp
        )
    }
}
```

> 코드 예시는 공식 문서에서 확인: [Slider](https://shadcn-compose.site/docs/components/slider)
