package me.pecos.memozy.platform.intent

import android.content.Context
import android.content.Intent
import android.net.Uri

class AndroidUrlLauncher(
    private val context: Context,
) : UrlLauncher {
    override fun open(url: String): Boolean = runCatching {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
        true
    }.getOrDefault(false)

    override fun openPreferringPackage(url: String, preferredPackage: String): Boolean {
        val parsed = Uri.parse(url)
        val preferred = Intent(Intent.ACTION_VIEW, parsed)
            .setPackage(preferredPackage)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return runCatching {
            context.startActivity(preferred)
            true
        }.getOrElse {
            runCatching {
                context.startActivity(
                    Intent(Intent.ACTION_VIEW, parsed)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                )
                true
            }.getOrDefault(false)
        }
    }
}
