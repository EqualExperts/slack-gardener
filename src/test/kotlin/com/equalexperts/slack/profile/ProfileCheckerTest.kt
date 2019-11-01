package com.equalexperts.slack.profile

import com.equalexperts.slack.api.chat.ChatSlackApi
import com.equalexperts.slack.api.conversations.ChannelId
import com.equalexperts.slack.api.conversations.ConversationHistoriesForTesting
import com.equalexperts.slack.api.conversations.ConversationsSlackApi
import com.equalexperts.slack.api.conversations.OpenConversationResponse
import com.equalexperts.slack.api.profile.ProfilesSlackApi
import com.equalexperts.slack.api.profile.model.UserProfileWrapper
import com.equalexperts.slack.api.rest.model.Message
import com.equalexperts.slack.api.rest.model.MessagesForTesting
import com.equalexperts.slack.api.users.UsersForTesting
import com.equalexperts.slack.api.users.UsersSlackApi
import com.equalexperts.slack.api.users.model.User
import com.equalexperts.slack.api.users.model.UserId
import com.equalexperts.slack.api.users.model.UserListsForTesting
import com.equalexperts.slack.profile.rules.UserProfilesRulesForTesting
import com.nhaarman.mockitokotlin2.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.ZonedDateTime

internal class ProfileCheckerTest {

    private lateinit var mockUserSlackApi: UsersSlackApi

    private lateinit var mockProfilesSlackApi: ProfilesSlackApi

    private lateinit var mockConversationSlackApi: ConversationsSlackApi

    private lateinit var mockChatSlackApi: ChatSlackApi

    private lateinit var mockBotUser: User

    private var dryRun: Boolean = false

    @BeforeEach
    fun `setup`() {
        mockUserSlackApi = mock()
        mockProfilesSlackApi = mock()
        mockConversationSlackApi = mock()
        mockChatSlackApi = mock()
        mockBotUser = mock()
    }

    @Test
    fun `should not send a message to people with valid profiles`() {

        val threshold = ZonedDateTime.now()

        val profile = UserProfilesForTesting.testUserProfile()
        val user = UsersForTesting.testUser(profile)
        val userList = UserListsForTesting.withEmptyCursorToken(user)
        whenever(mockUserSlackApi.list(any())).thenReturn(userList)

        val userProfileWrapper = UserProfileWrapper(profile)
        val userId = UserId(user.id)
        whenever(mockProfilesSlackApi.userProfile(userId)).thenReturn(userProfileWrapper)

        val rules = listOf(UserProfilesRulesForTesting.testPassingRule("TEST_FIELD_NAME"))
        val warningMessage = "WARNING_MESSAGE"
        val checker = ProfileChecker(dryRun, mockUserSlackApi, mockProfilesSlackApi, mockConversationSlackApi, mockChatSlackApi, rules, mockBotUser, warningMessage, threshold)

        checker.process()

        verify(mockConversationSlackApi, never()).conversationOpen(mockBotUser)
        verify(mockChatSlackApi, never()).postMessage(any<String>(), any(), any())
    }

    @Test
    fun `should send a message to people with invalid profiles and no message history`() {
        val threshold = ZonedDateTime.now()

        val profile = UserProfilesForTesting.testUserProfile()
        val user = UsersForTesting.testUser(profile)
        val userList = UserListsForTesting.withEmptyCursorToken(user)
        whenever(mockUserSlackApi.list(any())).thenReturn(userList)

        val userProfileWrapper = UserProfileWrapper(profile)
        val userId = UserId(user.id)
        whenever(mockProfilesSlackApi.userProfile(userId)).thenReturn(userProfileWrapper)

        val channelId = "TEST_CHANNEL_ID"
        val channel = ChannelId(channelId)
        val openConversationResponse = OpenConversationResponse(channel)
        whenever(mockConversationSlackApi.conversationOpen(user)).thenReturn(openConversationResponse)

        val messages = emptyList<Message>()
        whenever(mockConversationSlackApi.channelHistory(channelId)).thenReturn(ConversationHistoriesForTesting.withEmptyCursorToken(messages))

        val rules = listOf(UserProfilesRulesForTesting.testFailingRule("TEST_FIELD_NAME"))
        val warningMessage = "WARNING_MESSAGE"
        val checker = ProfileChecker(dryRun, mockUserSlackApi, mockProfilesSlackApi, mockConversationSlackApi, mockChatSlackApi, rules, mockBotUser, warningMessage, threshold)

        checker.process()

        verify(mockConversationSlackApi).conversationOpen(user)
        verify(mockChatSlackApi).postMessage(channelId, mockBotUser, warningMessage)
    }

    @Test
    fun `should not message if last message was less than two days ago`() {
        val threshold = ZonedDateTime.now().minusDays(2)

        val profile = UserProfilesForTesting.testUserProfile()
        val user = UsersForTesting.testUser(profile)
        val userList = UserListsForTesting.withEmptyCursorToken(user)
        whenever(mockUserSlackApi.list(any())).thenReturn(userList)

        val userProfileWrapper = UserProfileWrapper(profile)
        val userId = UserId(user.id)
        whenever(mockProfilesSlackApi.userProfile(userId)).thenReturn(userProfileWrapper)

        val channelId = "TEST_CHANNEL_ID"
        val channel = ChannelId(channelId)
        val openConversationResponse = OpenConversationResponse(channel)
        whenever(mockConversationSlackApi.conversationOpen(user)).thenReturn(openConversationResponse)

        val yesterday = ZonedDateTime.now().minusDays(1)
        val messages = listOf(MessagesForTesting.botMessage(user.id, user.id, yesterday, "TEST_MESSAGE"))
        whenever(mockConversationSlackApi.channelHistory(channelId)).thenReturn(ConversationHistoriesForTesting.withEmptyCursorToken(messages))

        val rules = listOf(UserProfilesRulesForTesting.testFailingRule("TEST_FIELD_NAME"))
        val warningMessage = "WARNING_MESSAGE"
        val checker = ProfileChecker(dryRun, mockUserSlackApi, mockProfilesSlackApi, mockConversationSlackApi, mockChatSlackApi, rules, mockBotUser, warningMessage, threshold)

        checker.process()

        verify(mockConversationSlackApi).conversationOpen(user)
        verify(mockChatSlackApi, never()).postMessage(channelId, mockBotUser, warningMessage)
    }

    @Test
    fun `should message if last message was greater than two days ago`() {
        val threshold = ZonedDateTime.now().minusDays(2)

        val profile = UserProfilesForTesting.testUserProfile()
        val user = UsersForTesting.testUser(profile)
        val userList = UserListsForTesting.withEmptyCursorToken(user)
        whenever(mockUserSlackApi.list(any())).thenReturn(userList)

        val userProfileWrapper = UserProfileWrapper(profile)
        val userId = UserId(user.id)
        whenever(mockProfilesSlackApi.userProfile(userId)).thenReturn(userProfileWrapper)

        val channelId = "TEST_CHANNEL_ID"
        val channel = ChannelId(channelId)
        val openConversationResponse = OpenConversationResponse(channel)
        whenever(mockConversationSlackApi.conversationOpen(user)).thenReturn(openConversationResponse)

        val yesterday = ZonedDateTime.now().minusDays(3)
        val messages = listOf(MessagesForTesting.botMessage(user.id, user.id, yesterday, "TEST_MESSAGE"))
        whenever(mockConversationSlackApi.channelHistory(channelId)).thenReturn(ConversationHistoriesForTesting.withEmptyCursorToken(messages))

        val rules = listOf(UserProfilesRulesForTesting.testFailingRule("TEST_FIELD_NAME"))
        val warningMessage = "WARNING_MESSAGE"
        val checker = ProfileChecker(dryRun, mockUserSlackApi, mockProfilesSlackApi, mockConversationSlackApi, mockChatSlackApi, rules, mockBotUser, warningMessage, threshold)

        checker.process()

        verify(mockConversationSlackApi).conversationOpen(user)
        verify(mockChatSlackApi).postMessage(channelId, mockBotUser, warningMessage)
    }

    @Test
    fun `should not message bots`() {
        val threshold = ZonedDateTime.now()

        val profile = UserProfilesForTesting.testBotProfile()
        val user = UsersForTesting.testBot(profile)
        val userList = UserListsForTesting.withEmptyCursorToken(user)
        whenever(mockUserSlackApi.list(any())).thenReturn(userList)

        val userProfileWrapper = UserProfileWrapper(profile)
        val userId = UserId(user.id)
        whenever(mockProfilesSlackApi.userProfile(userId)).thenReturn(userProfileWrapper)

        val channelId = "TEST_CHANNEL_ID"
        val channel = ChannelId(channelId)
        val openConversationResponse = OpenConversationResponse(channel)
        whenever(mockConversationSlackApi.conversationOpen(user)).thenReturn(openConversationResponse)

        val rules = listOf(UserProfilesRulesForTesting.testPassingRule("TEST_FIELD_NAME"))
        val warningMessage = "WARNING_MESSAGE"
        val checker = ProfileChecker(dryRun, mockUserSlackApi, mockProfilesSlackApi, mockConversationSlackApi, mockChatSlackApi, rules, mockBotUser, warningMessage, threshold)

        checker.process()

        verify(mockConversationSlackApi, never()).conversationOpen(mockBotUser)
        verify(mockChatSlackApi, never()).postMessage(any<String>(), any(), any())
    }

    @Test
    fun `should not message deactivated accounts`() {
        val threshold = ZonedDateTime.now()

        val profile = UserProfilesForTesting.testUserProfile()
        val user = UsersForTesting.testUser(profile).copy(deleted = true)
        val userList = UserListsForTesting.withEmptyCursorToken(user)
        whenever(mockUserSlackApi.list(any())).thenReturn(userList)

        val userProfileWrapper = UserProfileWrapper(profile)
        val userId = UserId(user.id)
        whenever(mockProfilesSlackApi.userProfile(userId)).thenReturn(userProfileWrapper)

        val channelId = "TEST_CHANNEL_ID"
        val channel = ChannelId(channelId)
        val openConversationResponse = OpenConversationResponse(channel)
        whenever(mockConversationSlackApi.conversationOpen(user)).thenReturn(openConversationResponse)

        val rules = listOf(UserProfilesRulesForTesting.testFailingRule("TEST_FIELD_NAME"))
        val warningMessage = "WARNING_MESSAGE"
        val checker = ProfileChecker(dryRun, mockUserSlackApi, mockProfilesSlackApi, mockConversationSlackApi, mockChatSlackApi, rules, mockBotUser, warningMessage, threshold)

        checker.process()

        verify(mockConversationSlackApi, never()).conversationOpen(user)
        verify(mockChatSlackApi, never()).postMessage(channelId, mockBotUser, warningMessage)
    }


}
