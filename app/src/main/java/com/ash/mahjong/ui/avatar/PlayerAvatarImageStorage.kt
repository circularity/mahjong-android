package com.ash.mahjong.ui.avatar

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import com.ash.mahjong.data.player.PlayerAnimalAvatarCatalog
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import kotlin.math.max
import kotlin.math.roundToInt

object PlayerAvatarImageStorage {
    private const val AVATAR_DIRECTORY = "player_avatars"
    private const val MAX_IMAGE_EDGE_PX = 512
    private const val JPEG_QUALITY = 82

    fun saveCompressedAvatar(
        context: Context,
        sourceUri: Uri
    ): String? {
        val bitmap = decodeBitmap(context, sourceUri) ?: return null
        val scaledBitmap = scaleBitmapIfNeeded(bitmap)
        val avatarDir = File(context.filesDir, AVATAR_DIRECTORY).apply { mkdirs() }
        val fileName = "avatar_${System.currentTimeMillis()}_${UUID.randomUUID()}.jpg"
        val outputFile = File(avatarDir, fileName)
        val saved = FileOutputStream(outputFile).use { output ->
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, output)
        }
        return if (saved) {
            PlayerAnimalAvatarCatalog.localAvatarKey(fileName)
        } else {
            null
        }
    }

    fun resolveLocalAvatarFile(
        context: Context,
        avatarKey: String
    ): File? {
        val avatarName = PlayerAnimalAvatarCatalog.localAvatarFileNameOrNull(avatarKey) ?: return null
        return File(File(context.filesDir, AVATAR_DIRECTORY), avatarName).takeIf { it.exists() }
    }

    private fun decodeBitmap(context: Context, sourceUri: Uri): Bitmap? {
        return runCatching {
            val imageSource = ImageDecoder.createSource(context.contentResolver, sourceUri)
            ImageDecoder.decodeBitmap(imageSource)
        }.getOrNull()
    }

    private fun scaleBitmapIfNeeded(bitmap: Bitmap): Bitmap {
        val maxEdge = max(bitmap.width, bitmap.height)
        if (maxEdge <= MAX_IMAGE_EDGE_PX) {
            return bitmap
        }
        val ratio = MAX_IMAGE_EDGE_PX.toFloat() / maxEdge.toFloat()
        val targetWidth = (bitmap.width * ratio).roundToInt().coerceAtLeast(1)
        val targetHeight = (bitmap.height * ratio).roundToInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
    }
}
