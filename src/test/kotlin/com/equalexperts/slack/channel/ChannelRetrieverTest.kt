package com.equalexperts.slack.channel

import com.equalexperts.slack.gardener.rest.SlackApi
import com.equalexperts.slack.gardener.rest.model.ChannelInfo
import com.equalexperts.slack.gardener.rest.model.ChannelList
import com.equalexperts.slack.gardener.rest.model.ResponseMetadata
import com.nhaarman.mockito_kotlin.*
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import java.time.Instant

internal class ChannelRetrieverTest {

    @Test
    fun `should retrieve channels`() {
        val mockSlackApi : SlackApi = mock()

        val testChannel = ChannelInfo("TEST_ID", "CHANNEL_NAME", Instant.EPOCH.epochSecond, 1)
        val channelList = ChannelList(listOf(testChannel), ResponseMetadata("") )
        whenever(mockSlackApi.listChannels()).thenReturn(channelList)

        val channels = ChannelRetriever(mockSlackApi).getChannels()

        verify(mockSlackApi, atMost(1)).listChannels()

        assertEquals(setOf(testChannel), channels)
    }

    @Test
    fun `should use cursor token to get next page of channels if non-blank`() {
        val mockSlackApi : SlackApi = mock()

        val testChannel = ChannelInfo("TEST_ID", "CHANNEL_NAME_1", Instant.EPOCH.epochSecond, 1)

        val cursorToken = "CURSOR TOKEN"
        val firstResponse = ChannelList(listOf(testChannel), ResponseMetadata(cursorToken) )

        whenever(mockSlackApi.listChannels()).thenReturn(firstResponse)

        val testChannelTwo = ChannelInfo("TEST_ID_2", "CHANNEL_NAME_2", Instant.EPOCH.epochSecond, 1)
        val emptyCursorToken = ""
        val secondResponse = ChannelList(listOf(testChannelTwo), ResponseMetadata(emptyCursorToken) )

        whenever(mockSlackApi.listChannels(cursorToken)).thenReturn(secondResponse)

        val channels = ChannelRetriever(mockSlackApi).getChannels()

        verify(mockSlackApi, atMost(1)).listChannels()
        verify(mockSlackApi, atMost(1)).listChannels(cursorToken)

        assertEquals(setOf(testChannel, testChannelTwo), channels)
    }
}