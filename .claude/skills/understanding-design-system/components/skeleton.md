# Skeleton

Use to show a placeholder while content is loading.

## Usage

```kotlin
import com.shadcn.ui.components.Skeleton

@Composable
fun Example() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 24.dp, start = 16.dp, end = 16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Skeleton(
                modifier = Modifier
                    .size(64.dp),
                shape = CircleShape
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Skeleton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(20.dp)
                )

                Skeleton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(20.dp)
                )
            }
        }
    }
}
```

> 코드 예시는 공식 문서에서 확인: [Skeleton](https://shadcn-compose.site/docs/components/skeleton)
