package com.ash.mahjong.data.player.local

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PlayerDaoTest {

    private lateinit var context: Context
    private lateinit var database: MahjongDatabase
    private lateinit var playerDao: PlayerDao
    private lateinit var dbName: String

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        dbName = "player-dao-test-${System.currentTimeMillis()}.db"
        database = buildDatabase(dbName)
        playerDao = database.playerDao()
    }

    @After
    fun tearDown() {
        database.close()
        context.deleteDatabase(dbName)
    }

    @Test
    fun insertPlayer_canBeQueried() = runBlocking {
        playerDao.insertPlayer(
            PlayerEntity(
                displayName = "Alice",
                normalizedName = "alice",
                initialScore = 100,
                createdAt = 1L
            )
        )

        val players = playerDao.observeAllPlayers().first()
        assertEquals(1, players.size)
        assertEquals("Alice", players.first().displayName)
        assertEquals(100, players.first().initialScore)
    }

    @Test
    fun insertPlayer_withDuplicateNormalizedName_throwsConstraint() = runBlocking {
        playerDao.insertPlayer(
            PlayerEntity(
                displayName = "Alice",
                normalizedName = "alice",
                initialScore = 100,
                createdAt = 1L
            )
        )

        try {
            playerDao.insertPlayer(
                PlayerEntity(
                    displayName = " alice ",
                    normalizedName = "alice",
                    initialScore = 110,
                    createdAt = 2L
                )
            )
            fail("Expected SQLiteConstraintException")
        } catch (_: SQLiteConstraintException) {
            // Expected.
        }
    }

    @Test
    fun data_persistsAfterDatabaseReopen() = runBlocking {
        playerDao.insertPlayer(
            PlayerEntity(
                displayName = "Bob",
                normalizedName = "bob",
                initialScore = 120,
                createdAt = 10L
            )
        )

        database.close()
        database = buildDatabase(dbName)
        playerDao = database.playerDao()

        val players = playerDao.observeAllPlayers().first()
        assertEquals(1, players.size)
        assertEquals("Bob", players.first().displayName)
        assertEquals(true, players.first().isActive)
    }

    @Test
    fun updatePlayerActiveStatus_updatesPlayer() = runBlocking {
        val playerId = playerDao.insertPlayer(
            PlayerEntity(
                displayName = "Carol",
                normalizedName = "carol",
                initialScore = 100,
                createdAt = 3L
            )
        ).toInt()

        playerDao.updatePlayerActiveStatus(playerId = playerId, isActive = false)

        val players = playerDao.observeAllPlayers().first()
        assertEquals(1, players.size)
        assertEquals(false, players.first().isActive)
    }

    private fun buildDatabase(name: String): MahjongDatabase {
        return Room.databaseBuilder(
            context,
            MahjongDatabase::class.java,
            name
        )
            .allowMainThreadQueries()
            .build()
    }
}
