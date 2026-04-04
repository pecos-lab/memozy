package me.pecos.memozy.feature.pet.rive

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import app.rive.runtime.kotlin.RiveAnimationView
import app.rive.runtime.kotlin.core.Rive

/**
 * Mini Rive pet for bottom navigation bar.
 * Shows a small version of the pet character.
 * Falls back to emoji if no .riv asset.
 */
@Composable
fun RiveMiniPetView(
    riveAssetName: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val assetExists = remember(riveAssetName) {
        try {
            context.assets.open(riveAssetName).use { true }
        } catch (_: Exception) { false }
    }

    if (!assetExists) {
        // Emoji fallback — small pet face
        Box(
            modifier = modifier.size(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "\uD83D\uDC3E", fontSize = 16.sp)
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
                        stateMachineName = "PetBehavior",
                        autoplay = true
                    )
                    inputStream.close()
                } catch (_: Exception) { }
            }.also { riveView = it }
        },
        modifier = modifier.size(24.dp),
        onRelease = { riveView = null }
    )
}
