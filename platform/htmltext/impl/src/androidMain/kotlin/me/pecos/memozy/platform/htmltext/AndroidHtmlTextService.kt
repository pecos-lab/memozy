package me.pecos.memozy.platform.htmltext

import android.text.Html

class AndroidHtmlTextService : HtmlTextService {
    override fun fromHtml(html: String): String =
        Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY).toString()
}
