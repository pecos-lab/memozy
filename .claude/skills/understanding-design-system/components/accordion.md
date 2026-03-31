# Accordion

Accordion uses AccordionItemData with id, header (composable), content (composable). Usage: Accordion(items = accordionItems)

## Usage

```kotlin
import com.shadcn.ui.components.Accordion
import com.shadcn.ui.components.AccordionItemData

fun Example() {
    val accordionItems = listOf(
        AccordionItemData(
            id = "item-1",
            header = { Text("Is it accessible?") },
            content = { Text("Yes. It adheres to the WAI-ARIA design pattern.") }
        ),
        AccordionItemData(
            id = "item-2",
            header = { Text("Is it styled?") },
            content = { Text("Yes. It comes with default styles that matches the other components' aesthetic.") }
        ),
        AccordionItemData(
            id = "item-3",
            header = { Text("Is it animated?") },
            content = { Text("Yes. It's animated by default, but you can disable it if you prefer.") }
        )
    )
    Column {
        Accordion(items = accordionItems)
    }
}
```

> 코드 예시는 공식 문서에서 확인: [Accordion](https://shadcn-compose.site/docs/components/accordion)
