package com.equalexperts.slack.profile

import com.equalexperts.slack.api.auth.AuthSlackApi
import com.equalexperts.slack.api.chat.ChatSlackApi
import com.equalexperts.slack.api.conversations.ConversationsSlackApi
import com.equalexperts.slack.api.profile.ProfilesSlackApi
import com.equalexperts.slack.api.users.UsersSlackApi
import com.equalexperts.slack.api.users.model.User
import com.equalexperts.slack.api.users.model.UserId
import com.equalexperts.slack.profile.rules.*
import org.slf4j.LoggerFactory
import java.net.URI
import java.time.Clock
import java.time.Period
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.util.stream.Collectors

class ProfileChecker(private val usersSlackApi: UsersSlackApi,
                     private val userProfilesSlackApi: ProfilesSlackApi,
                     private val conversationsSlackApi: ConversationsSlackApi,
                     private val chatSlackApi: ChatSlackApi,
                     private val rules: List<ProfileFieldRule>,
                     private val botUser: User,
                     private val warningMessage: String,
                     private val warningThreshold: ZonedDateTime) {

    private val logger = LoggerFactory.getLogger(this::class.java.name)

    fun process() {
        val users = UsersSlackApi.listAll(usersSlackApi)
                .filter { !it.is_bot }
                .filter { !it.is_deleted }
        val usersWithDetailedProfiles = users.map { user -> addDetailedProfileToUser(user) }
                .filter { it.profile.bot_id.isNullOrBlank() }

        val userProfileResults = mutableMapOf<User, ProfileCheckerResults>()

        val profileRequiredFieldsChecker = ProfileRequiredFieldsChecker(rules)

        for (user in usersWithDetailedProfiles) {
            val results = profileRequiredFieldsChecker.checkMissingFields(user)
            userProfileResults[user] = results
        }

        userProfileResults.entries.parallelStream()
                .peek{ logger.info("${it.key.name} has values set: ${it.value.results}")}
                .filter { it.value.getNumberOfFailedFields() > 0 }
                .map { Triple(it.key, it.value, conversationsSlackApi.conversationOpen(it.key).channel.id) }
                .filter { haveWeMessagedThemRecently(it.third) }
                .map { sendMessage(it.first, it.second, it.third) }
                .collect(Collectors.toList())

        logger.info("$userProfileResults")
    }

    private fun haveWeMessagedThemRecently(conversationId: String): Boolean {
        val channelHistory = conversationsSlackApi.channelHistory(conversationId)
        if (channelHistory.messages.isEmpty()) return false

        val lastMessage = channelHistory.messages.first()

        return lastMessage.timestamp.toZonedDateTime() < warningThreshold
    }

    private fun sendMessage(user: User, results: ProfileCheckerResults, conversationId: String) {
        logger.info("Sending Message to ${user.name} with $results using conversation $conversationId}")
        chatSlackApi.postMessage(conversationId, botUser, warningMessage)
    }

    private fun addDetailedProfileToUser(user: User): User {
        val userProfile = userProfilesSlackApi.userProfile(UserId(user.id))
        user.profile.fields = userProfile.profile.fields
        return user
    }

    companion object {
        fun build(slackUri: URI, slackOauthAccessToken: String, slackBotOauthAccessToken: String, warningMessage: String, warningWaitDays: Int) : ProfileChecker {

            val authSlackApi = AuthSlackApi.factory(slackUri, slackBotOauthAccessToken, Thread::sleep)
            val chatSlackApi = ChatSlackApi.factory(slackUri, slackBotOauthAccessToken, Thread::sleep)
            val conversationsSlackApi = ConversationsSlackApi.factory(slackUri, slackBotOauthAccessToken, Thread::sleep)


            val usersSlackApi = UsersSlackApi.factory(slackUri, slackOauthAccessToken, Thread::sleep)
            val userProfilesSlackApi = ProfilesSlackApi.factory(slackUri, slackOauthAccessToken, Thread::sleep)

            val teamCustomProfileFields = userProfilesSlackApi.teamProfile()

            val rules = listOf(ProfileFieldRealNameRule(),
                                                    ProfileFieldDisplayNameRule(),
                                                    ProfileFieldTitleRule(),
                                                    ProfileFieldHomeBaseRule(teamCustomProfileFields))

            val botUserId = authSlackApi.authenticate().id
            val botUser = usersSlackApi.getUserInfo(botUserId).user

            val clock = Clock.systemUTC()
            val defaultWaitingPeriod = Period.ofDays(warningWaitDays)
            val threshold = ZonedDateTime.now(clock).truncatedTo(ChronoUnit.DAYS) - defaultWaitingPeriod

            return ProfileChecker(usersSlackApi, userProfilesSlackApi, conversationsSlackApi, chatSlackApi, rules, botUser, warningMessage, threshold)
        }
    }

}


class ProfileRequiredFieldsChecker(private val rules: List<ProfileFieldRule>) {

    private val logger = LoggerFactory.getLogger(this::class.java.name)

    fun checkMissingFields(user: User): ProfileCheckerResults {
        val results = mutableMapOf<String, Boolean>()

        for (rule in rules) {
            val result = rule.checkProfile(user)
            results[result.field] = result.result
        }
        return ProfileCheckerResults(results)
    }

}

data class ProfileCheckerResults(val results: Map<String, Boolean>) {
    fun getNumberOfFailedFields(): Int {
        return results.filter { !it.value }.size
    }

}