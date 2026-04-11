package com.ash.mahjong.data.player

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PlayerAnimalAvatarCatalogTest {

    @Test
    fun normalizeAvatarKey_acceptsAnimalAndImageKeys() {
        assertEquals("cat", PlayerAnimalAvatarCatalog.normalizeAvatarKey("cat"))
        assertEquals("image_01", PlayerAnimalAvatarCatalog.normalizeAvatarKey("image_01"))
        assertEquals(
            "local_avatar:avatar_001.jpg",
            PlayerAnimalAvatarCatalog.normalizeAvatarKey("local_avatar:avatar_001.jpg")
        )
        assertNull(PlayerAnimalAvatarCatalog.normalizeAvatarKey("unknown_avatar"))
        assertNull(PlayerAnimalAvatarCatalog.normalizeAvatarKey("local_avatar:../avatar.jpg"))
    }

    @Test
    fun nextAvatarKey_rotatesAcrossAnimalAndImagePools() {
        assertEquals("image_01", PlayerAnimalAvatarCatalog.nextAvatarKey("penguin"))
        assertEquals("cat", PlayerAnimalAvatarCatalog.nextAvatarKey("image_09"))
    }

    @Test
    fun randomAvatarKey_returnsOnlyKnownKeys() {
        val random = Random(7)
        repeat(200) {
            val key = PlayerAnimalAvatarCatalog.randomAvatarKey(random)
            assertTrue(PlayerAnimalAvatarCatalog.normalizeAvatarKey(key) != null)
        }
    }

    @Test
    fun isImageAvatarKey_detectsImagePool() {
        assertTrue(PlayerAnimalAvatarCatalog.isImageAvatarKey("image_05"))
        assertTrue(!PlayerAnimalAvatarCatalog.isImageAvatarKey("dog"))
    }

    @Test
    fun localAvatarKey_resolvesFileNameAndImageState() {
        val avatarKey = PlayerAnimalAvatarCatalog.localAvatarKey("photo_002.jpg")
        assertEquals("photo_002.jpg", PlayerAnimalAvatarCatalog.localAvatarFileNameOrNull(avatarKey))
        assertTrue(PlayerAnimalAvatarCatalog.isLocalAvatarKey(avatarKey))
        assertTrue(PlayerAnimalAvatarCatalog.isVisualImageAvatarKey(avatarKey))
        assertEquals("", PlayerAnimalAvatarCatalog.emojiForKey(avatarKey))
    }
}
