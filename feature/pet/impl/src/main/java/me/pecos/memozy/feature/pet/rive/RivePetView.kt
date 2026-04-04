package me.pecos.memozy.feature.pet.rive

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import app.rive.runtime.kotlin.RiveAnimationView
import app.rive.runtime.kotlin.core.Rive

/**
 * Compose wrapper for Rive pet animation.
 *
 * State Machine: "PetBehavior"
 * Inputs:
 *   - mood (number, 0~100)
 *   - timeOfDay (number, 0=morning, 1=day, 2=evening, 3=night)
 *   - isTouching (boolean)
 *   - memoSaved (trigger)
 *   - levelUp (trigger)
 *
 * If the .riv asset doesn't exist, [fallbackContent] is shown.
 * To enable Rive: drop the .riv file into assets/ folder.
 */
@Composable
fun RivePetView(
    riveAssetName: String,
    stateMachineName: String = "PetBehavior",
    mood: Int = 70,
    timeOfDay: Int = 1,
    isTouching: Boolean = false,
    modifier: Modifier = Modifier,
    fallbackContent: @Composable () -> Unit = {}
) {
    val context = LocalContext.current
    val assetExists = remember(riveAssetName) {
        hasAssetFile(context, riveAssetName)
    }

    if (!assetExists) {
        Box(modifier = modifier) {
            fallbackContent()
        }
        return
    }

    var riveView by remember { mutableStateOf<RiveAnimationView?>(null) }

    AndroidView(
        factory = { ctx ->
            Rive.init(ctx)
            RiveAnimationView(ctx).apply {
                try {
                    val inputStream = ctx.assets.open(riveAssetName)
                    setRiveBytes(
                        inputStream.readBytes(),
                        stateMachineName = stateMachineName,
                        autoplay = true
                    )
                    inputStream.close()
                } catch (_: Exception) { }
            }.also { riveView = it }
        },
        modifier = modifier.fillMaxSize(),
        onRelease = { view ->
            riveView = null
        }
    )

    // Reactively update state machine inputs
    LaunchedEffect(mood) {
        riveView?.setNumberState(stateMachineName, "mood", mood.toFloat())
    }
    LaunchedEffect(timeOfDay) {
        riveView?.setNumberState(stateMachineName, "timeOfDay", timeOfDay.toFloat())
    }
    LaunchedEffect(isTouching) {
        riveView?.setBooleanState(stateMachineName, "isTouching", isTouching)
    }
}

/**
 * Fire a one-shot trigger on the state machine.
 */
fun RiveAnimationView.firePetTrigger(
    triggerName: String,
    stateMachineName: String = "PetBehavior"
) {
    try {
        fireState(stateMachineName, triggerName)
    } catch (_: Exception) { }
}

private fun hasAssetFile(context: android.content.Context, fileName: String): Boolean {
    return try {
        context.assets.open(fileName).use { true }
    } catch (_: Exception) {
        false
    }
}
