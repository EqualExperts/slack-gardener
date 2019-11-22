package com.equalexperts.slack.profile

import com.equalexperts.slack.api.auth.AuthSlackApi
import com.equalexperts.slack.api.chat.ChatSlackApi
import com.equalexperts.slack.api.conversations.ConversationsSlackApi
import com.equalexperts.slack.api.profile.ProfilesSlackApi
import com.equalexperts.slack.api.users.UsersSlackApi
import com.equalexperts.slack.api.users.listAll
import com.equalexperts.slack.api.users.model.User
import com.equalexperts.slack.api.users.model.UserId
import com.equalexperts.slack.pmap
import com.equalexperts.slack.profile.rules.*
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.apache.commons.codec.digest.DigestUtils
import org.slf4j.LoggerFactory
import java.net.URI
import java.time.Clock
import java.time.Period
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import kotlin.system.measureNanoTime

class ProfileChecker(private val dryRun: Boolean,
                     private val usersSlackApi: UsersSlackApi,
                     private val userProfilesSlackApi: ProfilesSlackApi,
                     private val conversationsSlackApi: ConversationsSlackApi,
                     private val chatSlackApi: ChatSlackApi,
                     private val rules: List<ProfileFieldRule>,
                     private val botUser: User,
                     private val warningMessage: String,
                     private val warningThreshold: ZonedDateTime) {

    private val logger = LoggerFactory.getLogger(this::class.java.name)

    fun getDefaultMd5Hashes(): Set<String> {
        val users = usersSlackApi.listAll()
            .filter { !it.is_bot }
            .filter { !it.deleted }
        val usersWithDetailedProfiles = users.map { user -> addDetailedProfileToUser(user) }
            .filter { it.profile.bot_id.isNullOrBlank() }

        val md5Hashes = mutableMapOf<String, Int>()
        for (user in usersWithDetailedProfiles) {
            user.profile.image_24?.let {
                user.profile.image_24.httpGet().response { _, response, result ->
                    when (result) {
                        is Result.Failure<ByteArray, FuelError> -> {
                            logger.info("Couldn't get image for $user.name")
                        }
                        is Result.Success<ByteArray, FuelError> -> {
                            val md5sum = DigestUtils.md5Hex(response.dataStream)

                            val containsKey = md5Hashes.containsKey(md5sum)
                            if (containsKey) {
                                md5Hashes[md5sum] = md5Hashes.getValue(md5sum) + 1
                            } else {
                                md5Hashes[md5sum] = 1
                            }
                            logger.info("md5 for $user.name is $md5sum")
                        }
                    }
                }
            } ?: logger.info("Couldn't get profile pic url for $user.name")
        }

        logger.info("$md5Hashes")
        val numberOfRealAccountsWithDuplicatedPictures = 3
        return md5Hashes.filter { it.value > numberOfRealAccountsWithDuplicatedPictures }.keys
    }

    fun process() {
        val nanoTime = measureNanoTime {
            val users = usersSlackApi.listAll()
                .filter { !it.is_bot }
                .filter { !it.deleted }
                .filter { it.name != "slackbot" }
                //Add profiles concurrently to respect api rate limits,
                //if we do this in parallel it hits the rate limits really soon
                .map { addDetailedProfileToUser(it) }

            logger.info("${users.size} active users found")

            val profileRequiredFieldsChecker = ProfileRequiredFieldsChecker(rules)
            runBlocking {
                //Process the profiles in parallel, as we now have all the data we need to make our decisions and can avoid the rate limits
                users.pmap { processUser(it, profileRequiredFieldsChecker) }
            }
        }
        logger.info("done in ${nanoTime / 1_000_000} ms")
    }

    private suspend fun processUser(user: User, profileRequiredFieldsChecker: ProfileRequiredFieldsChecker) = withContext(Dispatchers.Default) {
        val results = profileRequiredFieldsChecker.checkMissingFields(user)
        if (results.getNumberOfFailedFields() > 0) {
            val conversation = conversationsSlackApi.conversationOpen(user).channel.id
            if (lastMessageIsAfterWarningFrequencyThreshold(conversation)) {
                sendMessage(user, results, conversation)
            } else {
                logger.info("We've already message ${user.name} recently due to $results ")
            }
        }
    }

    private fun lastMessageIsAfterWarningFrequencyThreshold(conversationId: String): Boolean {
        val channelHistory = conversationsSlackApi.channelHistory(conversationId)
        if (channelHistory.messages.isEmpty()) return true

        val lastMessage = channelHistory.messages.first()

        return lastMessage.timestamp.toZonedDateTime() < warningThreshold
    }

    private fun sendMessage(user: User, results: ProfileCheckerResults, conversationId: String) {
        if (dryRun) {
            logger.info("DRY RUN: Would have messaged ${user.name} with $results using conversation $conversationId")
        } else {
            logger.info("Sending Message to ${user.name} with $results using conversation $conversationId")
            chatSlackApi.postMessage(conversationId, botUser, warningMessage)
        }
    }

    private fun addDetailedProfileToUser(user: User): User {
        val userProfile = userProfilesSlackApi.userProfile(UserId(user.id))
        val newProfileFields = userProfile.profile.fields
        val newUserProfile = user.profile.copy(fields = newProfileFields)
        return user.copy(profile = newUserProfile)
    }

    companion object {
        fun build(dryRun: Boolean, slackUri: URI, slackOauthAccessToken: String, slackBotOauthAccessToken: String, warningMessage: String, warningWaitDays: Int, knownDefaultPictureMd5Hashes: Set<String>): ProfileChecker {

            val authSlackApi = AuthSlackApi.factory(slackUri, slackBotOauthAccessToken, Thread::sleep)
            val chatSlackApi = ChatSlackApi.factory(slackUri, slackBotOauthAccessToken, Thread::sleep)
            val conversationsSlackApi = ConversationsSlackApi.factory(slackUri, slackBotOauthAccessToken, Thread::sleep)


            val usersSlackApi = UsersSlackApi.factory(slackUri, slackOauthAccessToken, Thread::sleep)
            val userProfilesSlackApi = ProfilesSlackApi.factory(slackUri, slackOauthAccessToken, Thread::sleep)

            val teamCustomProfileFields = userProfilesSlackApi.teamProfile()

            val rules = listOf(ProfileFieldRealNameRule(),
                ProfileFieldDisplayNameRule(),
                ProfileFieldTitleRule(),
                ProfileFieldHomeBaseRule(teamCustomProfileFields),
                ProfilePictureRule(knownDefaultPictureMd5Hashes))

            val botUserId = authSlackApi.authenticate().id
            val botUser = usersSlackApi.getUserInfo(botUserId).user

            val clock = Clock.systemUTC()
            val defaultWaitingPeriod = Period.ofDays(warningWaitDays)
            val threshold = ZonedDateTime.now(clock).truncatedTo(ChronoUnit.DAYS) - defaultWaitingPeriod

            return ProfileChecker(dryRun, usersSlackApi, userProfilesSlackApi, conversationsSlackApi, chatSlackApi, rules, botUser, warningMessage, threshold)
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
