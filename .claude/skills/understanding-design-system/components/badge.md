# Badge

Displays a badge or a component that looks like a badge.

## Basic Usage

```kotlin
import androidx.compose.material3.BadgedBox
import com.shadcn.ui.components.Badge
import com.shadcn.ui.components.BadgeVariant
import com.shadcn.ui.components.Button
import com.shadcn.ui.components.ButtonSize

@Composable
fun Example() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BadgedBox(
            badge = {
                Badge(
                    variant = BadgeVariant.Destructive
                ) {
                    Text("8")
                }
            }
        ) {
            Button(
                size = ButtonSize.Icon,
                onClick = {}
            ) {
                Icon(Icons.Default.Notifications, contentDescription = "icon")
            }
        }
    }
}
```

## Default

```kotlin
import androidx.compose.material3.BadgedBox
import com.shadcn.ui.components.Badge

@Composable
fun Example() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Badge {
            Text("Default")
        }
    }
}
```

## Secondary

```kotlin
import androidx.compose.material3.BadgedBox
import com.shadcn.ui.components.Badge
import com.shadcn.ui.components.BadgeVariant

@Composable
fun Example() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Badge(variant = BadgeVariant.Secondary) {
            Text("Secondary")
        }
    }
}
```

## Destructive

```kotlin
import androidx.compose.material3.BadgedBox
import com.shadcn.ui.components.Badge
import com.shadcn.ui.components.BadgeVariant

@Composable
fun Example() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Badge(variant = BadgeVariant.Destructive) {
            Text("Destructive")
        }
    }
}
```

## Outline

```kotlin
import androidx.compose.material3.BadgedBox
import com.shadcn.ui.components.Badge
import com.shadcn.ui.components.BadgeVariant

@Composable
fun Example() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Badge(variant = BadgeVariant.Outline) {
            Text("Outline")
        }
    }
}
```

## Custom Styles

```kotlin
import androidx.compose.material3.BadgedBox
import com.shadcn.ui.components.Badge

@Composable
fun Example() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Badge(backgroundColor = colors.chart3, roundedSize = 8.dp) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "icon",
                    tint = Color.White
                )
                Text("Custom background")
            }
        }
    }
}
```

> 코드 예시는 공식 문서에서 확인: [Badge](https://shadcn-compose.site/docs/components/badge)
