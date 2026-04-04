package me.pecos.memozy.feature.pet.rive

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
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
 * Rive wrapper for egg hatching animation.
 *
 * State Machine: "EggHatch"
 * Inputs:
 *   - tapCount (number, 0~3)
 *   - hatch (trigger)
 *
 * If egg.riv doesn't exist, [fallbackContent] is shown.
 */
@Composable
fun RiveEggView(
    tapCount: Int = 0,
    modifier: Modifier = Modifier,
    fallbackContent: @Composable () -> Unit = {}
) {
    val context = LocalContext.current
    val assetExists = remember {
        hasAssetFile(context, "egg.riv")
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
                    val inputStream = ctx.assets.open("egg.riv")
                    setRiveBytes(
                        inputStream.readBytes(),
                        stateMachineName = "EggHatch",
                        autoplay = true
                    )
                    inputStream.close()
                } catch (_: Exception) { }
            }.also { riveView = it }
        },
        modifier = modifier.fillMaxSize(),
        update = { _ ->
            riveView?.let { view ->
                try {
                    view.setNumberState("EggHatch", "tapCount", tapCount.toFloat())
                } catch (_: Exception) { }
            }
        },
        onRelease = { riveView = null }
    )
}

private fun hasAssetFile(context: android.content.Context, fileName: String): Boolean {
    return try {
        context.assets.open(fileName).use { true }
    } catch (_: Exception) {
        false
    }
}
