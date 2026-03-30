# Input

Displays a form input field or a component that looks like an input field.

## Basic Usage

```kotlin
import com.shadcn.ui.components.Input

@Composable
fun Example() {
    var nameTxt by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 24.dp, start = 16.dp, end = 16.dp),
    ) {
        Input(
            value = nameTxt,
            onValueChange = { nameTxt = it },
            placeholder = "Enter your name",
        )
    }
}
```

## With Leading Icon

```kotlin
import com.shadcn.ui.components.Input
import com.shadcn.ui.components.InputVariant

@Composable
fun Example() {
    var email by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 24.dp, start = 16.dp, end = 16.dp),
    ) {
        Input(
            value = email,
            onValueChange = { email = it },
            placeholder = "Enter your email",
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email icon") },
            variant = InputVariant.Outlined,
            supportingText = { Text("This is a helpful supporting text.") }
        )
    }
}
```

## With Visual Transformation

```kotlin
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.shadcn.ui.components.Input

@Composable
fun Example() {
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 24.dp, start = 16.dp, end = 16.dp),
    ) {
        Input(
            value = password,
            onValueChange = { password = it },
            placeholder = "Password",
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = {
                // Handle done action, e.g., login
                println("Password entered: $password")
            }),
            trailingIcon = {
                val image = if (passwordVisible)
                    Icons.Filled.Visibility
                else Icons.Filled.VisibilityOff
                Box(
                    modifier = Modifier.clickable { passwordVisible = !passwordVisible }
                ) {
                    Icon(imageVector = image, contentDescription = if (passwordVisible) "Hide password" else "Show password")
                }
            }
        )
    }
}
```

## Underline

```kotlin
import com.shadcn.ui.components.Input
import com.shadcn.ui.components.InputVariant

@Composable
fun Example() {
    var multiLineText by remember {
        mutableStateOf("This is a multi-line text field example.\nIt can expand to show more content.")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 24.dp, start = 16.dp, end = 16.dp),
    ) {
        Input(
            value = multiLineText,
            onValueChange = { multiLineText = it },
            placeholder = "Enter multi-line text",
            singleLine = false,
            maxLines = 5,
            variant = InputVariant.Underlined,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Default)
        )
    }
}
```

## Disabled

```kotlin
import com.shadcn.ui.components.Input

@Composable
fun Example() {
    var disabledText by remember { mutableStateOf("Disabled input") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 24.dp, start = 16.dp, end = 16.dp),
    ) {
        Input(
            value = disabledText,
            onValueChange = { },
            enabled = false
        )
    }
}
```

## Read-only

```kotlin
import com.shadcn.ui.components.Input

@Composable
fun Example() {
    var readOnly by remember { mutableStateOf("Read-only input") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 24.dp, start = 16.dp, end = 16.dp),
    ) {
        Input(
            value = readOnly,
            onValueChange = { },
            readOnly = true
        )
    }
}
```

## With Error

```kotlin
import com.shadcn.ui.components.Input

@Composable
fun Example() {
    var text by remember { mutableStateOf("Input with error") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 24.dp, start = 16.dp, end = 16.dp),
    ) {
        Input(
            value = text,
            onValueChange = { text = it },
            isError = true,
            supportingText = { Text("Invalid input", color = MaterialTheme.colors.destructive) }
        )
    }
}
```

> 코드 예시는 공식 문서에서 확인: [Input](https://shadcn-compose.site/docs/components/input)
