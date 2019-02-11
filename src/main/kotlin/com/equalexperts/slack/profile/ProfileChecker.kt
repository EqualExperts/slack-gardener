package com.equalexperts.slack.profile

import com.equalexperts.slack.api.profile.ProfilesSlackApi
import com.equalexperts.slack.api.users.UsersSlackApi
import com.equalexperts.slack.api.users.model.User
import com.equalexperts.slack.api.users.model.UserId
import com.equalexperts.slack.profile.rules.*
import org.slf4j.LoggerFactory

class ProfileChecker(private val usersSlackApi: UsersSlackApi, private val userProfilesSlackApi: ProfilesSlackApi) {

    private val logger = LoggerFactory.getLogger(this::class.java.name)

    fun process() {
        val users = UsersSlackApi.listAll(usersSlackApi)
        val teamCustomProfileFields = userProfilesSlackApi.teamProfile()
        val usersWithDetailedProfiles = users.map { user -> addDetailedProfileToUser(user) }

        val userProfileResults = mutableMapOf<User, ProfileCheckerResults>()

        val rules = listOf(ProfileFieldRealNameRule(),
                ProfileFieldDisplayNameRule(),
                ProfileFieldTitleRule(),
                ProfileFieldHomeBaseRule(teamCustomProfileFields))

        val profileRequiredFieldsChecker = ProfileRequiredFieldsChecker(rules)

        for (user in usersWithDetailedProfiles) {
            val results = profileRequiredFieldsChecker.checkMissingFields(user)
            userProfileResults[user] = results
        }

        logger.info("$userProfileResults")
    }

    private fun addDetailedProfileToUser(user: User): User {
        val userProfile = userProfilesSlackApi.userProfile(UserId(user.id))
        user.profile.fields = userProfile.profile.fields
        return user
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