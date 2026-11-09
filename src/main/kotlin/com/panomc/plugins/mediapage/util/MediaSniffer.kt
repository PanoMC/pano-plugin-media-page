package com.panomc.plugins.mediapage.util

import java.io.File

/**
 * Detects the media type of an uploaded file from its first bytes. The client's Content-Type and file name are never
 * trusted, so only real images and videos are stored, and the stored extension always matches the content.
 */
object MediaSniffer {
    data class Detected(val type: String, val extension: String, val mimeType: String)

    private val MIME_BY_EXTENSION = mapOf(
        "png" to "image/png",
        "jpg" to "image/jpeg",
        "jpeg" to "image/jpeg",
        "gif" to "image/gif",
        "webp" to "image/webp",
        "bmp" to "image/bmp",
        "mp4" to "video/mp4",
        "m4v" to "video/mp4",
        "mov" to "video/quicktime",
        "webm" to "video/webm",
        "ogv" to "video/ogg"
    )

    fun detect(file: File): Detected? {
        val header = ByteArray(16)
        val read = file.inputStream().use { it.read(header) }

        if (read < 12) {
            return null
        }

        fun at(index: Int, vararg bytes: Int) = bytes.withIndex().all { (i, b) -> header[index + i] == b.toByte() }
        fun ascii(index: Int, text: String) = at(index, *text.map { it.code }.toIntArray())

        return when {
            at(0, 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A) -> image("png")
            at(0, 0xFF, 0xD8, 0xFF) -> image("jpg")
            ascii(0, "GIF8") -> image("gif")
            ascii(0, "RIFF") && ascii(8, "WEBP") -> image("webp")
            ascii(0, "BM") -> image("bmp")
            ascii(4, "ftyp") && ascii(8, "qt  ") -> video("mov")
            ascii(4, "ftyp") -> video("mp4")
            at(0, 0x1A, 0x45, 0xDF, 0xA3) -> video("webm")
            ascii(0, "OggS") -> video("ogv")
            else -> null
        }
    }

    /** The Content-Type for a stored file, or null when its extension is not one of the allowed media types. */
    fun mimeTypeOf(extension: String): String? = MIME_BY_EXTENSION[extension.lowercase()]

    private fun image(extension: String) = Detected("IMAGE", extension, MIME_BY_EXTENSION.getValue(extension))

    private fun video(extension: String) = Detected("VIDEO", extension, MIME_BY_EXTENSION.getValue(extension))
}
