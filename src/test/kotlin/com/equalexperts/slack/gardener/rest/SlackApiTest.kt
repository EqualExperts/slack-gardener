package com.equalexperts.slack.gardener.rest

import com.equalexperts.slack.gardener.rest.model.BotId
import com.equalexperts.slack.gardener.rest.model.ChannelInfo
import com.equalexperts.slack.gardener.rest.model.Timestamp
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
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
    private val sleeper : (Long) -> Unit = mock()
    private val slackApi by lazy { SlackApi.factory(URI("http://localhost:${wiremock.port()}/"), expectedToken, sleeper) }

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

    @Test
    fun `calls should not retry when a 429 isn't returned`() {
        //setup
        stubRequest("/api/channels.list?exclude_archived=true&exclude_members=true&token=${expectedToken}",
        """
            {
                "ok": true,
                "channels": []
            }
        """.trimIndent()
        )

        //execute
        val result = slackApi.listChannels()

        //assert
        assertThat(result.channels).isEmpty()
        verify(sleeper, never()).invoke(any())
    }

    @Test
    fun `calls should retry when the server responds with a 429`() {
        //setup
        val url = "/api/channels.list?exclude_archived=true&exclude_members=true&token=${expectedToken}"

        //the first call should fail due to rate limiting
        wiremock.stubFor(get(urlEqualTo(url))
            .inScenario("retry")
            .whenScenarioStateIs(STARTED)
            .willReturn(
                aResponse()
                    .withStatus(429)
                    .withHeader("Content-Type", "application/json; charset=utf-8")
                    .withHeader("Retry-After", "2")
                    .withBody("{\"ok\": \"false\"}")
            )
            .willSetStateTo("shouldSucceed")
        )

        //the second second call should succeed
        wiremock.stubFor(get(urlEqualTo(url))
            .inScenario("retry")
            .whenScenarioStateIs("shouldSucceed")
            .willReturn(
                aResponse()
                    .withHeader("Content-Type", "application/json; charset=utf-8")
                    .withBody("""
            {
                "ok": true,
                "channels": [
                    {
                        "id": "C0GRY5SJZ",
                        "name": "channel_a"
                    }
                ]
            }
            """.trimIndent()
                    )
            )
        )

        //execute
        val result = slackApi.listChannels()

        //assert
        verify(sleeper).invoke(2000)
        assertThat(result.channels.size).isEqualTo(1)
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