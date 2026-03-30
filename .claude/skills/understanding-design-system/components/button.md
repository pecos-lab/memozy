# Button

작업을 수행하는 클릭 가능한 버튼 컴포넌트.

## Default

```kotlin
import com.shadcn.ui.components.Button

@Composable
fun Example() {
    Button(onClick = { }) {
        Text("Normal")
    }
}
```

## Destructive

```kotlin
import com.shadcn.ui.components.Button
import com.shadcn.ui.components.ButtonVariant

@Composable
fun Example() {
    Button(onClick = { }, variant = ButtonVariant.Destructive) {
        Text("Destructive")
    }
}
```

## Secondary

```kotlin
import com.shadcn.ui.components.Button
import com.shadcn.ui.components.ButtonVariant

@Composable
fun Example() {
    Button(onClick = { }, variant = ButtonVariant.Secondary) {
        Text("Secondary")
    }
}
```

## Link

```kotlin
import com.shadcn.ui.components.Button
import com.shadcn.ui.components.ButtonVariant

@Composable
fun Example() {
    Button(onClick = { }, variant = ButtonVariant.Link) {
        Text("Link")
    }
}
```

## Ghost

```kotlin
import com.shadcn.ui.components.Button
import com.shadcn.ui.components.ButtonVariant

@Composable
fun Example() {
    Button(onClick = { }, variant = ButtonVariant.Ghost) {
        Text("Ghost")
    }
}
```

## Outline

```kotlin
import com.shadcn.ui.components.Button
import com.shadcn.ui.components.ButtonVariant

@Composable
fun Example() {
    Button(onClick = { }, variant = ButtonVariant.Outline) {
        Text("Outline")
    }
}
```

## Icon

```kotlin
import com.shadcn.ui.components.Button
import com.shadcn.ui.components.ButtonVariant
import com.shadcn.ui.components.ButtonSize

@Composable
fun Example() {
    Button(onClick = { }, variant = ButtonVariant.Outline, size = ButtonSize.Icon) {
        Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings")
    }
}
```

## With Icon

```kotlin
import com.shadcn.ui.components.Button

@Composable
fun Example() {
    Button(onClick = { }) {
        Icon(imageVector = Icons.Default.Add, contentDescription = "Add")
        Spacer(modifier = Modifier.width(8.dp))
        Text("Add Item")
    }
}
```

> 코드 예시는 공식 문서에서 확인: [Button](https://shadcn-compose.site/docs/components/button)
