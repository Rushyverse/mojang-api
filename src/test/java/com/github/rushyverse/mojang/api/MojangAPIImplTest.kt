package com.github.rushyverse.mojang.api

import com.github.rushyverse.mojang.api.utils.generateRandomName
import com.github.rushyverse.mojang.api.utils.generateRandomNameWithInvalidSymbol
import com.github.rushyverse.mojang.api.utils.generateRandomUUID
import com.github.rushyverse.mojang.api.utils.generateUUIDWithInvalidSymbol
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertThrows
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MojangAPIImplTest {
    private lateinit var mojangApi: MojangAPI

    @BeforeTest
    fun onBefore() {
        val jsonInstance =
            Json {
                ignoreUnknownKeys = true
            }
        mojangApi =
            MojangAPIImpl(
                HttpClient(CIO) {
                    install(ContentNegotiation) {
                        json(jsonInstance)
                    }
                    install(Logging) {
                        logger = Logger.DEFAULT
                        level = LogLevel.ALL
                    }
                },
            )
    }

    @Nested
    @DisplayName("Get player uuid")
    inner class GetUUID {
        @Test
        fun `with an existing player name`() =
            runTest {
                val name = "lukethehacker23"
                assertEquals(ProfileId(name, "cdb5aee8-0f90-4fdd-a63b-a16d38cd6b3b"), mojangApi.getUUID(name))
            }

        @Test
        fun `with an unknown player name`() =
            runTest {
                assertNull(mojangApi.getUUID(generateRandomName()))
            }

        @Test
        fun `with a player name with invalid length`() =
            runTest {
                assertNull(mojangApi.getUUID("a"))
            }

        @Test
        fun `with a player name with invalid character`() =
            runTest {
                assertNull(mojangApi.getUUID(generateRandomNameWithInvalidSymbol()))
            }
    }

    @Nested
    @DisplayName("Get players uuid from list")
    inner class GetUUIDs {
        private val limitNumberOfName = 10

        @Test
        fun `without player name`() {
            assertThrows<ClientRequestException> {
                runBlocking { mojangApi.getUUID(emptyList()) }
            }
        }

        @Test
        fun `with non existing player name`() =
            runTest {
                val profiles = mojangApi.getUUID(listOf(generateRandomName()))
                assertTrue { profiles.isEmpty() }
            }

        @Test
        fun `with existing player name`() =
            runTest {
                assertContentEquals(
                    listOf(
                        ProfileId("jeb_", "853c80ef-3c37-49fd-aa49-938b674adae6"),
                        ProfileId("Notch", "069a79f4-44e9-4726-a5be-fca90e38aaf5"),
                    ),
                    mojangApi.getUUID(listOf("Notch", "jeb_")),
                )
            }

        @Test
        fun `with existing and non existing player name`() =
            runTest {
                assertContentEquals(
                    listOf(
                        ProfileId("jeb_", "853c80ef-3c37-49fd-aa49-938b674adae6"),
                    ),
                    mojangApi.getUUID(listOf(generateRandomName(), "jeb_", generateRandomName())),
                )
            }

        @Test
        fun `too many names`() {
            val names = List(limitNumberOfName + 1) { generateRandomName() }
            assertThrows<ClientRequestException> {
                runBlocking { mojangApi.getUUID(names) }
            }
        }

        @Test
        fun `with a player name with invalid length`() =
            runTest {
                assertEquals(emptyList(), mojangApi.getUUID(listOf("a")))
            }

        @Test
        fun `with a player name with invalid character`() {
            val names = listOf(generateRandomNameWithInvalidSymbol())
            assertThrows<ClientRequestException> {
                runBlocking { mojangApi.getUUID(names) }
            }
        }
    }

    @Nested
    @DisplayName("Get player name")
    inner class GetName {
        @Test
        fun `with an existing player uuid`() =
            runTest {
                assertEquals(
                    ProfileId("lukethehacker23", "cdb5aee8-0f90-4fdd-a63b-a16d38cd6b3b"),
                    mojangApi.getName("cdb5aee80f904fdda63ba16d38cd6b3b"),
                )

                assertEquals(
                    ProfileId("lukethehacker23", "cdb5aee8-0f90-4fdd-a63b-a16d38cd6b3b"),
                    mojangApi.getName("cdb5aee8-0f90-4fdd-a63b-a16d38cd6b3b"),
                )
            }

        @Test
        fun `with an unknown player uuid`() =
            runTest {
                assertNull(mojangApi.getName(generateRandomUUID()))
            }

        @Test
        fun `with a player uuid with invalid length`() =
            runTest {
                assertNull(mojangApi.getName("a"))
            }

        @Test
        fun `with a player uuid with invalid character`() =
            runTest {
                assertNull(mojangApi.getName(generateUUIDWithInvalidSymbol()))
            }
    }

    @Nested
    @DisplayName("Get player skin")
    inner class GetSkin {
        @Test
        fun `with an existing player uuid`() =
            runTest {
                val id = "f1bfcbddc68b49bfaac9fb9d8ce5293d"
                val name = "123lmfao4"
                val skin = mojangApi.getSkin(id)
                assertNotNull(skin)
                assertEquals("f1bfcbdd-c68b-49bf-aac9-fb9d8ce5293d", skin.id)
                assertEquals(name, skin.name)

                val properties = skin.properties
                assertEquals(1, properties.size)

                val property = skin.getTexturesProperty()
                assertEquals("textures", property.name)
                assertNotNull(property.signature)

                val decoded = skin.getSkinDecoded()
                assertEquals("f1bfcbdd-c68b-49bf-aac9-fb9d8ce5293d", decoded.profileId)
                assertEquals(name, decoded.profileName)

                val textures = decoded.textures
                val skinTexture = textures.skin
                assertEquals(
                    "http://textures.minecraft.net/texture/" +
                        "e35f3a8df969b56b36f9aa60a736a2f9061de4ccf0fe9657d6c9bc02d77bfd7e",
                    skinTexture.url,
                )
                assertEquals("slim", skinTexture.metadata.model)
                assertNull(textures.cape)
            }

        @Test
        fun `with an unknown player uuid`() =
            runTest {
                assertNull(mojangApi.getSkin(generateRandomUUID()))
            }

        @Test
        fun `with a player uuid with invalid length`() =
            runTest {
                assertNull(mojangApi.getSkin("a"))
            }

        @Test
        fun `with a player uuid with invalid character`() =
            runTest {
                assertNull(mojangApi.getSkin(generateUUIDWithInvalidSymbol()))
            }
    }
}
