package com.panomc.plugins.mediapage.util

import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import javax.imageio.ImageReader

/**
 * Reads image sizes from the file header without decoding the pixels, and decodes thumbnails with subsampling, so a
 * small file with huge dimensions cannot fill the server's memory.
 */
object ImageDecoding {
    const val MAX_PIXELS = 50_000_000L

    /** Width and height from the header, or null when no reader understands the file. */
    fun readDimensions(file: File): Pair<Int, Int>? = withReader(file) { reader ->
        reader.getWidth(0) to reader.getHeight(0)
    }

    fun isTooLarge(width: Int, height: Int) = width.toLong() * height.toLong() > MAX_PIXELS

    /** The image decoded at roughly [targetWidth] pixels wide, reading only every n-th pixel of a bigger source. */
    fun readSubsampled(file: File, targetWidth: Int): BufferedImage? = withReader(file) { reader ->
        val width = reader.getWidth(0)
        val step = (width / targetWidth).coerceAtLeast(1)
        val param = reader.defaultReadParam
        param.setSourceSubsampling(step, step, 0, 0)
        reader.read(0, param)
    }

    private fun <T> withReader(file: File, block: (ImageReader) -> T): T? {
        ImageIO.createImageInputStream(file)?.use { input ->
            val readers = ImageIO.getImageReaders(input)
            if (!readers.hasNext()) {
                return null
            }

            val reader = readers.next()
            try {
                reader.setInput(input, true, true)
                return block(reader)
            } finally {
                reader.dispose()
            }
        }

        return null
    }
}
