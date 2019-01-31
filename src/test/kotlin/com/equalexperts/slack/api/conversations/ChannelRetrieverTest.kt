package com.equalexperts.slack.api.conversations

import com.equalexperts.slack.api.conversations.model.Conversation
import com.equalexperts.slack.api.conversations.model.ConversationList
import com.equalexperts.slack.api.rest.model.ResponseMetadata
import com.nhaarman.mockitokotlin2.*
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import java.time.Instant

internal class ChannelRetrieverTest {

    @Test
    fun `should retrieve channels`() {
        val mockConversationsSlackApi : ConversationsSlackApi = mock()

        val testChannel = Conversation("TEST_ID", "CHANNEL_NAME", Instant.EPOCH.epochSecond, 1)
        val channelList = ConversationList(listOf(testChannel), ResponseMetadata(""))
        whenever(mockConversationsSlackApi.list()).thenReturn(channelList)

        val channels = ConversationsSlackApi.listAll(mockConversationsSlackApi)

        verify(mockConversationsSlackApi, atMost(1)).list()

        assertEquals(setOf(testChannel), channels)
    }

    @Test
    fun `should use cursor token to get next page of channels if non-blank`() {
        val mockConversationsSlackApi : ConversationsSlackApi = mock()

        val testChannel = Conversation("TEST_ID", "CHANNEL_NAME_1", Instant.EPOCH.epochSecond, 1)

        val cursorToken = "CURSOR TOKEN"
        val firstResponse = ConversationList(listOf(testChannel), ResponseMetadata(cursorToken))

        whenever(mockConversationsSlackApi.list()).thenReturn(firstResponse)

        val testChannelTwo = Conversation("TEST_ID_2", "CHANNEL_NAME_2", Instant.EPOCH.epochSecond, 1)
        val emptyCursorToken = ""
        val secondResponse = ConversationList(listOf(testChannelTwo), ResponseMetadata(emptyCursorToken))

        whenever(mockConversationsSlackApi.list(cursorToken)).thenReturn(secondResponse)

        val channels = ConversationsSlackApi.listAll(mockConversationsSlackApi)

        verify(mockConversationsSlackApi, atMost(1)).list()
        verify(mockConversationsSlackApi, atMost(1)).list(cursorToken)

        assertEquals(setOf(testChannel, testChannelTwo), channels)
    }
}