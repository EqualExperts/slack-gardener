package com.equalexperts.slack.gardener.rest

import com.equalexperts.slack.gardener.rest.model.*
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.net.URI
import java.net.URLEncoder
import java.util.*

class SlackBotApiTest {

    val wiremock = WireMockServer()

    @Before
    fun setup() = wiremock.start()

    @After
    fun tearDown() {
        try {
            Assertions.assertThat(wiremock.allServeEvents).withFailMessage("No api calls were made").isNotEmpty
            Assertions.assertThat(wiremock.findAllUnmatchedRequests()).withFailMessage("Expected api call wasn't made").isEmpty()
        } finally {
            wiremock.stop() //stop the server even if we throw an exception
        }
    }

    private val expectedToken = UUID.randomUUID().toString()
    private val api by lazy { SlackBotApi.factory(URI("http://localhost:${wiremock.port()}/"), expectedToken) }

    @Test
    fun `authenticate should return a populated AuthInfo instance given a client with a valid token`() {
        //setup
        val expectedUserId = "U4CX8XFJQ"
        stubRequest(
            "/api/auth.test?token=${expectedToken}",
            """
            {
                "ok": true,
                "url": "https://equalexperts.slack.com/",
                "team": "Equal Experts",
                "user": "some-user-name",
                "team_id": "T1234567",
                "user_id": "${expectedUserId}"
            }
            """.trimIndent()
        )

        //execute
        val result = api.authenticate()

        //assert
        Assertions.assertThat(result.id).isEqualTo(UserId(expectedUserId))
    }

    @Test
    fun `authenticate should throw an exception given a client with an invalid token`() {
        //setup
        stubRequest(
            "/api/auth.test?token=${expectedToken}",
            """
            {
                "ok": false,
                "error": "invalid_auth"
            }
            """.trimIndent()
        )

        //execute
        val error = Assertions.catchThrowable { api.authenticate() }
        Assertions.assertThat(error).hasMessage("invalid_auth")

    }

    @Test
    fun `getUserInfo should return a populated UserInfo given a bot user`() {
        //setup
        val expectedUserId = UserId("U12345678")
        val expectedUsername = "some-user-name"
        val expectedBotId = BotId("C5T1UMUHV")
        stubRequest(
            "/api/users.info?user=${expectedUserId}&token=${expectedToken}",
            """
            {
                "ok": true,
                "user": {
                    "id": "${expectedUserId}",
                    "team_id": "T12345678",
                    "name": "${expectedUsername}",
                    "deleted": false,
                    "status": null,
                    "color": "bd9336",
                    "real_name": "",
                    "tz": null,
                    "tz_label": "Pacific Daylight Time",
                    "tz_offset": -25200,
                    "profile": {
                        "bot_id": "${expectedBotId}",
                        "api_app_id": "",
                        "always_active": false,
                        "avatar_hash": "148001443980",
                        "image_24": "https://avatars.slack-edge.com/2017-03-30/162681699527_14800144398069cc879b_24.png",
                        "image_32": "https://avatars.slack-edge.com/2017-03-30/162681699527_14800144398069cc879b_32.png",
                        "image_48": "https://avatars.slack-edge.com/2017-03-30/162681699527_14800144398069cc879b_48.png",
                        "image_72": "https://avatars.slack-edge.com/2017-03-30/162681699527_14800144398069cc879b_72.png",
                        "image_192": "https://avatars.slack-edge.com/2017-03-30/162681699527_14800144398069cc879b_192.png",
                        "image_512": "https://avatars.slack-edge.com/2017-03-30/162681699527_14800144398069cc879b_512.png",
                        "image_1024": "https://avatars.slack-edge.com/2017-03-30/162681699527_14800144398069cc879b_1024.png",
                        "image_original": "https://avatars.slack-edge.com/2017-03-30/162681699527_14800144398069cc879b_original.png",
                        "title": "Periodically maintains slack channels in the background",
                        "real_name": "",
                        "real_name_normalized": ""
                    },
                    "is_admin": false,
                    "is_owner": false,
                    "is_primary_owner": false,
                    "is_restricted": false,
                    "is_ultra_restricted": false,
                    "is_bot": true,
                    "updated": 1490869214
                }
            }
            """.trimIndent()
        )

        //execute
        val result = api.getUserInfo(expectedUserId)

        //assert
        assertThat(result.user.name).isEqualTo(expectedUsername)
        assertThat(result.user.profile.botId).isEqualTo(expectedBotId)
    }

    @Test
    fun `getUserInfo should return a populated UserInfo given a non-bot user`() {
        //setup
        val expectedUserId = UserId("U12345678")
        val expectedUsername = "some--other-user-name"
        stubRequest(
            "/api/users.info?user=${expectedUserId}&token=${expectedToken}",
            """
            {
                "ok": true,
                "user": {
                    "id": "${expectedUserId}",
                    "team_id": "T02QA1EAG",
                    "name": "${expectedUsername}",
                    "deleted": false,
                    "status": null,
                    "color": "e7392d",
                    "real_name": "Joe Bloggs",
                    "tz": "Europe/London",
                    "tz_label": "British Summer Time",
                    "tz_offset": 3600,
                    "profile": {
                        "first_name": "Joe",
                        "last_name": "Bloggs",
                        "skype": "askypeid",
                        "phone": "09991234576",
                        "title": "Partner",
                        "avatar_hash": "g55f2074998e",
                        "real_name": "Joe Bloggs",
                        "real_name_normalized": "Joe Bloggs",
                        "email": "jbloggs@equalexperts.com",
                        "image_24": "https://secure.gravatar.com/avatar/55f2074998e90cfafe4e4c91efa433f8.jpg?s=24&d=https%3A%2F%2Fa.slack-edge.com%2F66f9%2Fimg%2Favatars%2Fava_0015-24.png",
                        "image_32": "https://secure.gravatar.com/avatar/55f2074998e90cfafe4e4c91efa433f8.jpg?s=32&d=https%3A%2F%2Fa.slack-edge.com%2F66f9%2Fimg%2Favatars%2Fava_0015-32.png",
                        "image_48": "https://secure.gravatar.com/avatar/55f2074998e90cfafe4e4c91efa433f8.jpg?s=48&d=https%3A%2F%2Fa.slack-edge.com%2F66f9%2Fimg%2Favatars%2Fava_0015-48.png",
                        "image_72": "https://secure.gravatar.com/avatar/55f2074998e90cfafe4e4c91efa433f8.jpg?s=72&d=https%3A%2F%2Fa.slack-edge.com%2F66f9%2Fimg%2Favatars%2Fava_0015-72.png",
                        "image_192": "https://secure.gravatar.com/avatar/55f2074998e90cfafe4e4c91efa433f8.jpg?s=192&d=https%3A%2F%2Fa.slack-edge.com%2F7fa9%2Fimg%2Favatars%2Fava_0015-192.png",
                        "image_512": "https://secure.gravatar.com/avatar/55f2074998e90cfafe4e4c91efa433f8.jpg?s=512&d=https%3A%2F%2Fa.slack-edge.com%2F7fa9%2Fimg%2Favatars%2Fava_0015-512.png"
                    },
                    "is_admin": false,
                    "is_owner": false,
                    "is_primary_owner": false,
                    "is_restricted": false,
                    "is_ultra_restricted": false,
                    "is_bot": false,
                    "updated": 1488192517
                }
            }
            """.trimIndent()
        )

        //execute
        val result = api.getUserInfo(expectedUserId)

        //assert
        assertThat(result.user.name).isEqualTo(expectedUsername)
        assertThat(result.user.profile.botId).isNull()
    }

    @Test
    fun `postMessage should post a message`() {
        //setup
        val expectedChannel = ChannelInfo("C4VJP2DL4", "gardener-test-b", 0L, 4)
        val expectedUser = User("mr-foo", UserProfile("B4S0TLTGU"))
        val expectedMessage = "Hi <!channel>. This is a test message."

        val expectedUrl = "/api/chat.postMessage?channel=${expectedChannel.id}&username=${expectedUser.name}&text=${URLEncoder.encode(expectedMessage, "UTF-8")}&token=${expectedToken}"
        stubRequest(
            expectedUrl,
            """
            {
                "ok": true,
                "channel": "${expectedChannel.id}",
                "ts": "1491508419.254324",
                "message": {
                    "text": "${expectedMessage}",
                    "username": "${expectedUser.name}",
                    "bot_id": "${expectedUser.profile.botId}",
                    "type": "message",
                    "subtype": "bot_message",
                    "ts": "1491508419.254324"
                }
            }
            """.trimIndent()
        )

        //execute
        api.postMessage(expectedChannel, expectedUser, expectedMessage)

        //assert
        wiremock.verify(1, getRequestedFor(urlEqualTo(expectedUrl)))
    }

    private fun stubRequest(url: String, body: String) {
        wiremock.stubFor(WireMock.get(WireMock.urlEqualTo(url))
            .willReturn(
                WireMock.aResponse()
                    .withHeader("Content-Type", "application/json; charset=utf-8")
                    .withBody(body)
            )
        )
    }
}