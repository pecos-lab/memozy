# Carousel

A carousel with motion and swipe.

## Basic Usage

```kotlin
import com.shadcn.ui.components.Card
import com.shadcn.ui.components.Carousel

fun Example() {
    val verticalAsset = listOf(R.drawable.store_1, R.drawable.store_2, R.drawable.store_3)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Carousel(
            itemCount = verticalAsset.size,
            modifier = Modifier.fillMaxWidth(),
            showIndicator = true
        ) { position ->
            Card {
                AsyncImage(
                    verticalAsset[position],
                    contentDescription = "asset-${position}",
                    modifier = Modifier
                        .height(400.dp)
                )
            }
        }
    }
}
```

## Auto Scroll

```kotlin
import com.shadcn.ui.components.Card
import com.shadcn.ui.components.Carousel

fun Example() {
    val verticalAsset = listOf(R.drawable.store_1, R.drawable.store_2, R.drawable.store_3)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Carousel(
            itemCount = verticalAsset.size,
            autoScroll = true,
            showIndicator = true,
            autoScrollDelayMillis = 1000L,
            modifier = Modifier.fillMaxWidth()
        ) { position ->
            Card {
                AsyncImage(
                    verticalAsset[position],
                    contentDescription = "asset-${position}",
                    modifier = Modifier.height(400.dp)
                )
            }
        }
    }
}
```

## Customize Width Item

```kotlin
import com.shadcn.ui.components.Card
import com.shadcn.ui.components.Carousel

fun Example() {
    val horizontalAsset = listOf(R.drawable.store_h_1, R.drawable.store_h_2, R.drawable.store_h_3)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Carousel(
            itemCount = horizontalAsset.size,
            showIndicator = true,
            pageSize = PageSize.Fixed(300.dp),
            modifier = Modifier.fillMaxWidth(),
            itemSpacing = 12.dp
        ) { position ->
            Card {
                AsyncImage(
                    horizontalAsset[position],
                    contentDescription = "asset-${position}",
                    modifier = Modifier.height(200.dp)
                )
            }
        }
    }
}
```

> 코드 예시는 공식 문서에서 확인: [Carousel](https://shadcn-compose.site/docs/components/carousel)
