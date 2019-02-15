package com.equalexperts.slack.channel

import com.equalexperts.slack.api.chat.ChatSlackApi
import com.equalexperts.slack.api.conversations.ConversationsSlackApi
import com.equalexperts.slack.api.conversations.model.Conversation
import com.equalexperts.slack.api.conversations.model.ConversationList
import com.equalexperts.slack.api.users.model.User
import com.equalexperts.slack.api.users.model.UserInfo
import com.equalexperts.slack.profile.UserProfilesForTesting
import com.nhaarman.mockitokotlin2.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant

class ChannelCheckerTest {

    private val whitelistedChannelName = "WHITELISTED"
    private val whitelistedChannelNames: Set<String> = setOf(whitelistedChannelName)
    private val whitelistedConversationInfo = Conversation("TEST_ID", whitelistedChannelName, Instant.EPOCH.epochSecond, 1)
    private val whitelistedConversationList = ConversationList.withEmptyCursorToken(whitelistedConversationInfo)

    private val nonWhitelistedConversationList = ConversationList.withEmptyCursorToken(Conversation("TEST_ID", "CHANNEL_NAME", Instant.EPOCH.epochSecond, 1))

    private val channelInfoBreakingRules: Conversation = Conversation("TEST_ID", "RULE_BREAKING_CHANNEL_NAME", Instant.EPOCH.epochSecond, 1)
    private val channelInfoObeyingHelpRule = Conversation("TEST_ID", "help-me", Instant.EPOCH.epochSecond, 1)

    private val user = User(name = "TEST_USER",
            profile = UserProfilesForTesting.testBotProfile(),
            id = "id",
            team_id = "team_id",
            is_deleted = false,
            is_admin = false,
            is_owner = false,
            is_primary_owner = false,
            is_restricted = false,
            is_ultra_restricted = false,
            is_bot = true,
            is_app_user = false)
    private val userInfo: UserInfo = UserInfo(user)

    private val warningMessage: String = "WARNING"

    private lateinit var mockConversationsApi: ConversationsSlackApi
    private lateinit var mockChatSlackApi: ChatSlackApi

    @BeforeEach
    fun setup() {
        mockConversationsApi = mock()
        mockChatSlackApi = mock()
    }


    @Test
    fun `should not warn whitelisted channels`() {
        whenever(mockConversationsApi.list()).thenReturn(whitelistedConversationList)

        val rules = setOf(ChannelChecker.HELP_RULE, ChannelChecker.COMM_RULE)

        val checker = ChannelChecker(mockConversationsApi, mockChatSlackApi, userInfo, whitelistedChannelNames, rules, warningMessage)
        checker.process()

        verify(mockChatSlackApi, never()).postMessage(any<Conversation>(), any(), any())
    }

    @Test
    fun `should default to allowing all channel names with no rules`() {
        whenever(mockConversationsApi.list()).thenReturn(nonWhitelistedConversationList)

        val rules = emptySet<ChannelChecker.ChannelNamingRule>()

        val checker = ChannelChecker(mockConversationsApi, mockChatSlackApi, userInfo, whitelistedChannelNames, rules, warningMessage)
        checker.process()

        verify(mockChatSlackApi, never()).postMessage(any<Conversation>(), any(), any())
    }

    @Test
    fun `should warn when rule is broken`() {
        whenever(mockConversationsApi.list()).thenReturn(ConversationList.withEmptyCursorToken(channelInfoBreakingRules))

        val rules = setOf(ChannelChecker.HELP_RULE)

        val checker = ChannelChecker(mockConversationsApi, mockChatSlackApi, userInfo, whitelistedChannelNames, rules, warningMessage)
        checker.process()

        verify(mockChatSlackApi, atMost(1)).postMessage(channelInfoBreakingRules, user, warningMessage)
    }

    @Test
    fun `shouldnt warn if it obeys one of the rules`() {
        whenever(mockConversationsApi.list()).thenReturn(ConversationList.withEmptyCursorToken(channelInfoObeyingHelpRule))

        val rules = setOf(ChannelChecker.HELP_RULE, ChannelChecker.COMM_RULE)

        val checker = ChannelChecker(mockConversationsApi, mockChatSlackApi, userInfo, whitelistedChannelNames, rules, warningMessage)
        checker.process()

        verify(mockChatSlackApi, never()).postMessage(channelInfoObeyingHelpRule, user, warningMessage)
    }


}