package me.pecos.memozy.platform.intent

/**
 * 최소한의 application/x-www-form-urlencoded 디코더.
 * 기존 java.net.URLDecoder.decode(s, "UTF-8") 대체용.
 * '+' → ' ', '%HH' → UTF-8 byte 로 해석.
 */
fun percentDecodeUtf8(input: String): String {
    val out = StringBuilder()
    val pending = ArrayList<Byte>()
    fun flushPending() {
        if (pending.isNotEmpty()) {
            out.append(pending.toByteArray().decodeToString())
            pending.clear()
        }
    }
    var i = 0
    while (i < input.length) {
        val c = input[i]
        when {
            c == '%' && i + 2 < input.length -> {
                val hex = input.substring(i + 1, i + 3)
                val byte = hex.toInt(16).toByte()
                pending.add(byte)
                i += 3
            }
            c == '+' -> {
                flushPending()
                out.append(' ')
                i++
            }
            else -> {
                flushPending()
                out.append(c)
                i++
            }
        }
    }
    flushPending()
    return out.toString()
}
