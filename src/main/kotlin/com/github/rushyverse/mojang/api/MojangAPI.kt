package com.github.rushyverse.mojang.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import io.ktor.http.appendPathSegments
import io.ktor.http.contentType
import io.ktor.http.takeFrom

private val mojangApiURL = Url("https://api.mojang.com")
private val mojangSessionApiURL = Url("https://sessionserver.mojang.com")

/**
 * Allows interacting with Mojang API.
 */
public interface MojangAPI {
    /**
     * Allows users to supply a username to be checked and
     * get its UUID if the username resolves to a valid Minecraft profile.
     * https://mojang-api-docs.netlify.app/no-auth/username-to-uuid-get.html
     * @param name Player's name.
     * @return Instance of [ProfileId] linked to the player if found, `null` otherwise.
     */
    public suspend fun getUUID(name: String): ProfileId?

    /**
     * Allows users to send up to 10 usernames in an array and return all valid UUIDs
     * https://mojang-api-docs.netlify.app/no-auth/username-to-uuid-post.html
     * @param names Players' names
     * @return Instances of [ProfileId] linked to each existing player.
     */
    public suspend fun getUUID(names: Collection<String>): List<ProfileId>

    /**
     * Allows users to supply a UUID to be checked and
     * get its username if the UUID resolves to a valid Minecraft profile.
     * https://mojang-api-docs.netlify.app/no-auth/uuid-to-username.html
     * @param uuid Player's uuid.
     * @return Instance of [ProfileId] linked to the player if found, `null` otherwise.
     */
    public suspend fun getName(uuid: String): ProfileId?

    /**
     * Retrieve the skin data for a player.
     * A player is represented by his UUID.
     * https://mojang-api-docs.netlify.app/no-auth/uuid-to-profile.html
     * @param uuid Player's UUID.
     * @return Information about player's skin.
     */
    public suspend fun getSkin(uuid: String): ProfileSkin?
}

/**
 * Implementation to interact with Mojang API using a custom coroutine client.
 * @property client Coroutine http client used to interact with api.
 */
public class MojangAPIImpl(private val client: HttpClient) : MojangAPI {
    override suspend fun getUUID(name: String): ProfileId? {
        val response =
            client.get {
                url {
                    takeFrom(mojangApiURL)
                    appendPathSegments("users", "profiles", "minecraft", name)
                }
            }
        return if (response.status == HttpStatusCode.OK) response.body() else null
    }

    override suspend fun getUUID(names: Collection<String>): List<ProfileId> {
        val response =
            client.post {
                url {
                    takeFrom(mojangApiURL)
                    appendPathSegments("profiles", "minecraft")
                }
                contentType(ContentType.Application.Json)
                setBody(names)
            }
        return if (response.status == HttpStatusCode.OK) {
            response.body()
        } else {
            throw ClientRequestException(response, response.bodyAsText())
        }
    }

    override suspend fun getName(uuid: String): ProfileId? {
        val response =
            client.get {
                url {
                    takeFrom(mojangApiURL)
                    appendPathSegments("user", "profile", uuid)
                }
            }
        return if (response.status == HttpStatusCode.OK) response.body() else null
    }

    override suspend fun getSkin(uuid: String): ProfileSkin? {
        val response =
            client.get {
                url {
                    takeFrom(mojangSessionApiURL)
                    appendPathSegments("session", "minecraft", "profile", uuid)
                    parameter("unsigned", "false")
                }
            }
        return if (response.status == HttpStatusCode.OK) response.body() else null
    }
}
