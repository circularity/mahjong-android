package com.ash.mahjong.data.player

import kotlin.random.Random

object PlayerAnimalAvatarCatalog {

    private enum class AvatarType {
        ANIMAL,
        IMAGE
    }

    private data class PlayerAvatar(
        val key: String,
        val emoji: String?,
        val type: AvatarType
    )

    private val avatars = listOf(
        PlayerAvatar(key = "cat", emoji = "🐱", type = AvatarType.ANIMAL),
        PlayerAvatar(key = "dog", emoji = "🐶", type = AvatarType.ANIMAL),
        PlayerAvatar(key = "horse", emoji = "🐴", type = AvatarType.ANIMAL),
        PlayerAvatar(key = "rabbit", emoji = "🐰", type = AvatarType.ANIMAL),
        PlayerAvatar(key = "fox", emoji = "🦊", type = AvatarType.ANIMAL),
        PlayerAvatar(key = "panda", emoji = "🐼", type = AvatarType.ANIMAL),
        PlayerAvatar(key = "koala", emoji = "🐨", type = AvatarType.ANIMAL),
        PlayerAvatar(key = "frog", emoji = "🐸", type = AvatarType.ANIMAL),
        PlayerAvatar(key = "penguin", emoji = "🐧", type = AvatarType.ANIMAL),
        PlayerAvatar(key = "image_01", emoji = null, type = AvatarType.IMAGE),
        PlayerAvatar(key = "image_02", emoji = null, type = AvatarType.IMAGE),
        PlayerAvatar(key = "image_03", emoji = null, type = AvatarType.IMAGE),
        PlayerAvatar(key = "image_05", emoji = null, type = AvatarType.IMAGE),
        PlayerAvatar(key = "image_06", emoji = null, type = AvatarType.IMAGE),
        PlayerAvatar(key = "image_07", emoji = null, type = AvatarType.IMAGE),
        PlayerAvatar(key = "image_08", emoji = null, type = AvatarType.IMAGE),
        PlayerAvatar(key = "image_09", emoji = null, type = AvatarType.IMAGE)
    )
    private val avatarByKey = avatars.associateBy { it.key }

    fun allAvatarKeys(): List<String> = avatars.map { avatar -> avatar.key }

    fun firstAvatarKey(): String = avatars.first().key

    fun randomAvatarKey(random: Random = Random.Default): String {
        return avatars[random.nextInt(avatars.size)].key
    }

    fun normalizeAvatarKey(avatarKey: String?): String? {
        return avatarKey?.takeIf { key -> avatarByKey.containsKey(key) }
    }

    fun resolveAvatarKeyOrFallback(
        avatarKey: String?,
        playerId: Int,
        createdAt: Long
    ): String {
        return normalizeAvatarKey(avatarKey) ?: fallbackAvatarKey(playerId, createdAt)
    }

    fun nextAvatarKey(currentAvatarKey: String): String {
        val currentIndex = avatars.indexOfFirst { avatar -> avatar.key == currentAvatarKey }
        if (currentIndex == -1) {
            return avatars.first().key
        }
        return avatars[(currentIndex + 1) % avatars.size].key
    }

    fun isImageAvatarKey(avatarKey: String): Boolean {
        return avatarByKey[avatarKey]?.type == AvatarType.IMAGE
    }

    fun emojiForKey(avatarKey: String): String {
        if (isImageAvatarKey(avatarKey)) {
            return ""
        }
        return avatarByKey[avatarKey]?.emoji ?: avatars.first().emoji.orEmpty()
    }

    private fun fallbackAvatarKey(playerId: Int, createdAt: Long): String {
        val seed = playerId.toLong() * 31L + createdAt
        val hash = (seed xor (seed ushr 32)).toInt()
        val index = (hash and Int.MAX_VALUE) % avatars.size
        return avatars[index].key
    }
}
