# DatePicker

A date picker component with range and presets.

## Usage

```kotlin
import com.shadcn.ui.components.DatePicker

@Composable
fun Example() {
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 24.dp, start = 16.dp, end = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        DatePicker(
            selectedDate = selectedDate,
            onDateSelected = { selectedDate = it },
            modifier = Modifier.width(280.dp),
            placeholder = "Booking date",
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Pick date",
                    tint = MaterialTheme.colors.mutedForeground,
                    modifier = Modifier.size(20.dp)
                )
            }
        )

        Text(
            text = "Selected Date: ${selectedDate?.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) ?: "None"}",
            color = MaterialTheme.colors.foreground,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
```

> 코드 예시는 공식 문서에서 확인: [DatePicker](https://shadcn-compose.site/docs/components/date-picker)
