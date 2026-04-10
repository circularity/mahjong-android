package com.ash.mahjong.data.player

import kotlin.random.Random

object PlayerAnimalAvatarCatalog {

    private data class AnimalAvatar(
        val key: String,
        val emoji: String
    )

    private val avatars = listOf(
        AnimalAvatar(key = "cat", emoji = "🐱"),
        AnimalAvatar(key = "dog", emoji = "🐶"),
        AnimalAvatar(key = "rabbit", emoji = "🐰"),
        AnimalAvatar(key = "fox", emoji = "🦊"),
        AnimalAvatar(key = "panda", emoji = "🐼"),
        AnimalAvatar(key = "koala", emoji = "🐨"),
        AnimalAvatar(key = "frog", emoji = "🐸"),
        AnimalAvatar(key = "penguin", emoji = "🐧")
    )
    private val avatarByKey = avatars.associateBy { it.key }

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

    fun emojiForKey(avatarKey: String): String {
        return avatarByKey[avatarKey]?.emoji ?: avatars.first().emoji
    }

    private fun fallbackAvatarKey(playerId: Int, createdAt: Long): String {
        val seed = playerId.toLong() * 31L + createdAt
        val hash = (seed xor (seed ushr 32)).toInt()
        val index = (hash and Int.MAX_VALUE) % avatars.size
        return avatars[index].key
    }
}
