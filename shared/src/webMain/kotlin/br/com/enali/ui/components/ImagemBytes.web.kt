package br.com.enali.ui.components

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import org.jetbrains.skia.Image

actual fun ByteArray.toComposeImageBitmap(): ImageBitmap? {
    if (isEmpty()) return null
    return runCatching {
        Image.makeFromEncoded(this).toComposeImageBitmap()
    }.getOrNull()
}
