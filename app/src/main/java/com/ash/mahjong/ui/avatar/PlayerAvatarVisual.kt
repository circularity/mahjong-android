package com.ash.mahjong.ui.avatar

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import com.ash.mahjong.R
import com.ash.mahjong.data.player.PlayerAnimalAvatarCatalog

@DrawableRes
private fun avatarImageResOrNull(avatarKey: String): Int? {
    return when (avatarKey) {
        "image_01" -> R.drawable.player_avatar_image_01
        "image_02" -> R.drawable.player_avatar_image_02
        "image_03" -> R.drawable.player_avatar_image_03
        "image_04" -> R.drawable.player_avatar_image_04
        "image_05" -> R.drawable.player_avatar_image_05
        "image_06" -> R.drawable.player_avatar_image_06
        "image_07" -> R.drawable.player_avatar_image_07
        "image_08" -> R.drawable.player_avatar_image_08
        "image_09" -> R.drawable.player_avatar_image_09
        else -> null
    }
}

@Composable
fun PlayerAvatarVisual(
    avatarKey: String,
    avatarEmoji: String,
    fallbackText: String,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    textStyle: TextStyle = MaterialTheme.typography.titleMedium,
    textColor: Color = MaterialTheme.colorScheme.onSurface
) {
    val imageRes = if (PlayerAnimalAvatarCatalog.isImageAvatarKey(avatarKey)) {
        avatarImageResOrNull(avatarKey)
    } else {
        null
    }

    if (imageRes != null) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
    } else {
        val textModifier = if (contentDescription == null) {
            modifier
        } else {
            modifier.semantics { this.contentDescription = contentDescription }
        }
        Text(
            text = avatarEmoji.ifBlank { fallbackText },
            style = textStyle,
            color = textColor,
            textAlign = TextAlign.Center,
            modifier = textModifier
        )
    }
}
