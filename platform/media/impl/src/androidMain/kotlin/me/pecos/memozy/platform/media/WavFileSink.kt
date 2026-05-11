package me.pecos.memozy.platform.media

import java.io.File
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder

internal class WavFileSink(
    private val path: String,
    private val sampleRate: Int,
    private val channels: Int,
    private val bitsPerSample: Int,
) {
    private var raf: RandomAccessFile? = null
    private var dataSize: Long = 0

    fun open() {
        val f = File(path)
        f.parentFile?.mkdirs()
        if (f.exists()) f.delete()
        raf = RandomAccessFile(f, "rw").apply {
            setLength(0)
            write(buildHeader(0))
        }
        dataSize = 0
    }

    fun write(buf: ByteArray, len: Int) {
        val r = raf ?: return
        r.write(buf, 0, len)
        dataSize += len
    }

    fun close() {
        val r = raf ?: return
        try {
            r.seek(0)
            r.write(buildHeader(dataSize.toInt()))
        } finally {
            try { r.close() } catch (_: Throwable) {}
            raf = null
        }
    }

    private fun buildHeader(dataLen: Int): ByteArray {
        val byteRate = sampleRate * channels * bitsPerSample / 8
        val blockAlign = channels * bitsPerSample / 8
        return ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN).apply {
            put("RIFF".toByteArray(Charsets.US_ASCII))
            putInt(36 + dataLen)
            put("WAVE".toByteArray(Charsets.US_ASCII))
            put("fmt ".toByteArray(Charsets.US_ASCII))
            putInt(16)
            putShort(1.toShort())
            putShort(channels.toShort())
            putInt(sampleRate)
            putInt(byteRate)
            putShort(blockAlign.toShort())
            putShort(bitsPerSample.toShort())
            put("data".toByteArray(Charsets.US_ASCII))
            putInt(dataLen)
        }.array()
    }
}
