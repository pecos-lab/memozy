package me.pecos.memozy.presentation.util

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/**
 * format args(%d, %s)를 지원하는 stringResource 래퍼.
 *
 * Compose Multiplatform Resources 1.10.3의 stringResource(resource, vararg args) 오버로드가
 * 런타임에 format args를 치환하지 않고 리터럴로 노출하는 이슈가 있어 직접 Regex로 치환한다.
 * 리소스에서 사용 중인 포맷은 단일 %d 또는 %s 뿐이라 positional/%% escape는 지원하지 않는다.
 */
@Composable
fun stringResourceFormatted(resource: StringResource, vararg args: Any): String {
    val template = stringResource(resource)
    return applyFormatArgs(template, args)
}

private val FORMAT_PATTERN = Regex("""%[ds]""")

private fun applyFormatArgs(template: String, args: Array<out Any>): String {
    if (args.isEmpty()) return template
    var index = 0
    return FORMAT_PATTERN.replace(template) { match ->
        if (index < args.size) args[index++].toString() else match.value
    }
}
