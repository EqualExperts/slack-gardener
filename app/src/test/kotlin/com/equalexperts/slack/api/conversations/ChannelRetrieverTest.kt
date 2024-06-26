package com.equalexperts.slack.api.conversations

import com.equalexperts.slack.api.conversations.model.Conversation
import com.equalexperts.slack.channel.ConversationListsForTesting
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Instant

internal class ChannelRetrieverTest {

    @Test
    fun `should retrieve channels`() {
        val mockConversationsSlackApi: ConversationsSlackApi = mock()

        val testChannel = Conversation("TEST_ID", "CHANNEL_NAME", Instant.EPOCH.epochSecond, 1)
        val channelList = ConversationListsForTesting.withEmptyCursorToken(testChannel)
        whenever(mockConversationsSlackApi.list()).thenReturn(channelList)

        val channels = mockConversationsSlackApi.listAll()

        verify(mockConversationsSlackApi).list()

        assertEquals(setOf(testChannel), channels)
    }

    @Test
    fun `should use cursor token to get next page of channels if non-blank`() {
        val mockConversationsSlackApi: ConversationsSlackApi = mock()

        val testChannel = Conversation("TEST_ID", "CHANNEL_NAME_1", Instant.EPOCH.epochSecond, 1)

        val cursorToken = "CURSOR TOKEN"
        val firstResponse = ConversationListsForTesting.withCursorToken(testChannel, cursorToken)

        whenever(mockConversationsSlackApi.list()).thenReturn(firstResponse)

        val testChannelTwo = Conversation("TEST_ID_2", "CHANNEL_NAME_2", Instant.EPOCH.epochSecond, 1)

        val secondResponse = ConversationListsForTesting.withEmptyCursorToken(testChannelTwo)

        whenever(mockConversationsSlackApi.list(cursorToken)).thenReturn(secondResponse)

        val channels = mockConversationsSlackApi.listAll()

        verify(mockConversationsSlackApi).list()
        verify(mockConversationsSlackApi).list(cursorToken)

        assertEquals(setOf(testChannel, testChannelTwo), channels)
    }
}
