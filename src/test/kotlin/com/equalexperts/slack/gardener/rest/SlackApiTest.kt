package com.equalexperts.slack.gardener.rest

import com.equalexperts.slack.gardener.rest.model.BotId
import com.equalexperts.slack.gardener.rest.model.ChannelInfo
import com.equalexperts.slack.gardener.rest.model.Timestamp
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.net.URI
import java.util.*

class SlackApiTest {
    val wiremock = WireMockServer()

    @Before
    fun setup() = wiremock.start()

    @After
    fun tearDown() {
        try {
            assertThat(wiremock.allServeEvents).withFailMessage("No api calls were made").isNotEmpty
            assertThat(wiremock.findAllUnmatchedRequests()).withFailMessage("Expected api call wasn't made").isEmpty()
        } finally {
            wiremock.stop() //stop the server even if we throw an exception
        }
    }

    private val expectedToken = UUID.randomUUID().toString()
    private val slackApi by lazy { SlackApi.factory(URI("http://localhost:${wiremock.port()}/"), expectedToken) }

/*
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
        val result = slackApi.authenticate()

        //assert
        assertThat(result.id).isEqualTo(UserId(expectedUserId))
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
        val error = catchThrowable { slackApi.authenticate() }
        assertThat(error).hasMessage("invalid_auth")

    }
*/

    @Test
    fun `listChannels should return a populated channel list of non-archived channels`() {
        //setup
        stubRequest(
            "/api/channels.list?exclude_archived=true&exclude_members=true&token=${expectedToken}",
            """
            {
                "ok": true,
                "channels": [
                    {
                        "id": "C0GRY5SJZ",
                        "name": "channel_a",
                        "is_channel": true,
                        "created": 1450332946,
                        "creator": "U02S8QG15",
                        "is_archived": false,
                        "is_general": false,
                        "name_normalized": "channel_a",
                        "is_shared": false,
                        "is_org_shared": false,
                        "is_member": false,
                        "topic": {
                            "value": "",
                            "creator": "",
                            "last_set": 0
                        },
                        "purpose": {
                            "value": "",
                            "creator": "",
                            "last_set": 0
                        },
                        "previous_names": [],
                        "num_members": 0
                    },
                    {
                        "id": "C37GJCAAY",
                        "name": "channel_b",
                        "is_channel": true,
                        "created": 1480273937,
                        "creator": "U02QA70LY",
                        "is_archived": false,
                        "is_general": false,
                        "name_normalized": "channel_b",
                        "is_shared": false,
                        "is_org_shared": false,
                        "is_member": false,
                        "topic": {
                            "value": "",
                            "creator": "",
                            "last_set": 0
                        },
                        "purpose": {
                            "value": "A channel for some stuff",
                            "creator": "U02QA70LY",
                            "last_set": 1480273938
                        },
                        "previous_names": [],
                        "num_members": 2
                    }
                ]
            }
            """.trimIndent()
        )

        //execute
        val result = slackApi.listChannels()

        //assert
        assertThat(result.channels.size).isEqualTo(2)
        result.channels[0].let {
            assertThat(it.created).isEqualTo("2015-12-17T06:15:46Z")
            assertThat(it.name).isEqualTo("channel_a")
            assertThat(it.id).isEqualTo("C0GRY5SJZ")
            assertThat(it.members).isEqualTo(0)
        }

        result.channels[1].let {
            assertThat(it.created).isEqualTo("2016-11-27T19:12:17Z")
            assertThat(it.name).isEqualTo("channel_b")
            assertThat(it.id).isEqualTo("C37GJCAAY")
            assertThat(it.members).isEqualTo(2)
        }
    }

    /*
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
        val result = slackApi.getUserInfo(expectedUserId)

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
        val result = slackApi.getUserInfo(expectedUserId)

        //assert
        assertThat(result.user.name).isEqualTo(expectedUsername)
        assertThat(result.user.profile.botId).isNull()
    }
    */

    /*
    @Test
    fun `postMessage should post a message`() {
        //setup
        val expectedChannel = ChannelInfo("C4VJP2DL4", "gardener-test-b", 0L)
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
        slackApi.postMessage(expectedChannel, expectedUser, expectedMessage)

        //assert
        wiremock.verify(1, getRequestedFor(urlEqualTo(expectedUrl)))
    }
    */

    @Test
    fun `getChannelHistory should return a populated ChannelHistory with messages given a channel with messages`() {
        //setup
        val expectedChannel = ChannelInfo("C02QANF4U", "some-channel", 123, 1)
        val expectedTimestamp = Timestamp("1458227233")
        stubRequest(
            "/api/channels.history?channel=${expectedChannel.id}&oldest=${expectedTimestamp}&count=1000&token=${expectedToken}",
            """
            {
                "ok": true,
                "oldest": " 1458227233",
                "messages": [
                    {
                        "user": "U04Q4QRC0",
                        "text": "<@U04Q4QRC0|rbeton> has left the channel",
                        "type": "message",
                        "subtype": "channel_leave",
                        "ts": "1491485475.016457"
                    },
                    {
                        "user": "U02S0USEN",
                        "text": "<@U02S0USEN|phudekar> has left the channel",
                        "type": "message",
                        "subtype": "channel_leave",
                        "ts": "1478754099.000002"
                    },
                    {
                        "text": "",
                        "bot_id": "B076MU2MP",
                        "attachments": [
                            {
                                "fallback": "Build <https://travis-ci.org/EqualExperts/opslogger/builds/161298689|#87> (<https://github.com/EqualExperts/opslogger/compare/cf238e4a4424...9fdf2e64d683|9fdf2e6>) of EqualExperts/opslogger@master by Sean Reilly passed in 1 min 5 sec",
                                "text": "Build <https://travis-ci.org/EqualExperts/opslogger/builds/161298689|#87> (<https://github.com/EqualExperts/opslogger/compare/cf238e4a4424...9fdf2e64d683|9fdf2e6>) of EqualExperts/opslogger@master by Sean Reilly passed in 1 min 5 sec",
                                "id": 1,
                                "color": "36a64f"
                            }
                        ],
                        "type": "message",
                        "subtype": "bot_message",
                        "ts": "1474371028.000002"
                    }
                ],
                "has_more": false
            }
            """.trimIndent()
        )

        //execute
        val result = slackApi.getChannelHistory(expectedChannel, expectedTimestamp)

        //assert
        assertThat(result.messages).hasSize(3)
        result.messages.first().let {
            assertThat(it.user).isEqualTo("U04Q4QRC0")
            assertThat(it.subtype).isEqualTo("channel_leave")
            assertThat(it.type).isEqualTo("message")
            assertThat(it.timestamp).isEqualTo(Timestamp("1491485475.016457"))
            assertThat(it.botId).isNull()
        }

        result.messages[2].let {
            assertThat(it.botId).isEqualTo(BotId("B076MU2MP"))
            assertThat(it.subtype).isEqualTo("bot_message")
            assertThat(it.type).isEqualTo("message")
            assertThat(it.timestamp).isEqualTo(Timestamp("1474371028.000002"))
            assertThat(it.user).isNull()
        }
    }

    @Test
    fun `getChannelHistory should return a populated ChannelHistory with hasMore = true given a channel with more messages`() {
        //setup
        val expectedChannel = ChannelInfo("C02QANF4U", "some-channel", 123, 2)
        val expectedTimestamp = Timestamp("1458227233")
        stubRequest(
            "/api/channels.history?channel=${expectedChannel.id}&oldest=${expectedTimestamp}&count=1000&token=${expectedToken}",
            """
            {
                "ok": true,
                "oldest": " 1458227233",
                "messages": [
                    {
                        "user": "U04Q4QRC0",
                        "text": "<@U04Q4QRC0|rbeton> has left the channel",
                        "type": "message",
                        "subtype": "channel_leave",
                        "ts": "1491485475.016457"
                    }
                ],
                "has_more": true
            }
            """.trimIndent()
        )

        //execute
        val result = slackApi.getChannelHistory(expectedChannel, expectedTimestamp)

        //assert
        assertThat(result.hasMore).isEqualTo(true)
    }

    @Test
    fun `getChannelHistory should return a populated ChannelHistory with hasMore = false given a channel with no more messages`() {
        //setup
        val expectedChannel = ChannelInfo("C02QANF4U", "some-channel", 123, 3)
        val expectedTimestamp = Timestamp("1458227233")
        stubRequest(
            "/api/channels.history?channel=${expectedChannel.id}&oldest=${expectedTimestamp}&count=1000&token=${expectedToken}",
            """
            {
                "ok": true,
                "oldest": " 1458227233",
                "messages": [
                ],
                "has_more": false
            }
            """.trimIndent()
        )

        //execute
        val result = slackApi.getChannelHistory(expectedChannel, expectedTimestamp)

        //assert
        assertThat(result.hasMore).isEqualTo(false)
    }

    @Test
    fun `archiveChannel should archive a channel`() {
        //setup
        val expectedChannel = ChannelInfo("C4VJP2DL4", "gardener-test-b", 0L, 4)

        val expectedUrl = "/api/channels.archive?channel=${expectedChannel.id}&token=${expectedToken}"
        stubRequest(
            expectedUrl,
            """
            {
                "ok": true
            }
            """.trimIndent()
        )

        //execute
        slackApi.archiveChannel(expectedChannel)

        //assert
        wiremock.verify(1, getRequestedFor(urlEqualTo(expectedUrl)))
    }

    private fun stubRequest(url: String, body: String) {
        wiremock.stubFor(get(urlEqualTo(url))
                .willReturn(
                    aResponse()
                        .withHeader("Content-Type", "application/json; charset=utf-8")
                        .withBody(body)
                )
        )
    }
}