# Alert

Displays a callout for user attention.

## Default

```kotlin
Alert(
    title = { Text("Heads up!") },
    description = { Text("You can add components to your app using the cli.") }
)
```

## Destructive

```kotlin
Alert(
    variant = AlertVariant.Destructive,
    title = { Text("Error") },
    description = { Text("Your session has expired. Please log in again.") }
)
```

## With Icon

```kotlin
Alert(
    icon = {
        // Placeholder for an actual icon
        // For demonstration, a simple text icon
        Text("💡", fontSize = 24.sp)
    },
    title = { Text("Tip!") },
    description = { Text("This is a helpful tip for using the application effectively.") }
)
```

## Custom Styles

```kotlin
Alert(
    icon = {
        Icon(Icons.Default.Info, contentDescription = "Info", tint = MaterialTheme.shadcnColors.primaryForeground)
    },
    title = { Text("Information") },
    description = { Text("This is a helpful information that you need to know") },
    colors = AlertDefaults.colors().copy(
        backgroundColor = MaterialTheme.shadcnColors.chart2,
        borderColors = MaterialTheme.shadcnColors.chart2,
        titleColor = MaterialTheme.shadcnColors.primaryForeground,
        descriptionColor = MaterialTheme.shadcnColors.primaryForeground
    )
)
```

> 코드 예시는 공식 문서에서 확인: [Alert](https://shadcn-compose.site/docs/components/alert)
