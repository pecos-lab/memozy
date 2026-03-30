# Avatar

An image element with a fallback for representing the user.

## Basic Usage

```kotlin
import com.shadcn.ui.components.Avatar

@Composable
fun Example() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Avatar(
            imageUrl = "https://avatars.githubusercontent.com/u/124599?v=4",
            contentDescription = "Avatar",
            fallbackText = "CN"
        )
    }
}
```

## With Text Fallback

```kotlin
import com.shadcn.ui.components.Avatar

@Composable
fun Example() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Avatar(
            fallbackText = "CN"
        )
    }
}
```

## With Custom Fallback

```kotlin
import com.shadcn.ui.components.Avatar

@Composable
fun Example() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Avatar(
            imageUrl = "https://invalid-image-url.com/nonexistent.png",
            contentDescription = "Invalid Avatar",
            fallbackText = "ERR",
            errorContent = {
                Text("❌")
            }
        )
    }
}
```

## With Loading

```kotlin
import com.shadcn.ui.components.Avatar

@Composable
fun Example() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Avatar(
            imageUrl = "https://picsum.photos/200/300",
            contentDescription = "Random Image",
            fallbackText = "RI",
            loadingContent = {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.shadcnColors.primary)
            }
        )
    }
}
```

> 코드 예시는 공식 문서에서 확인: [Avatar](https://shadcn-compose.site/docs/components/avatar)
