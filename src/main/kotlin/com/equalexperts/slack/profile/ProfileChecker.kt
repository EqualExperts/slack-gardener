package com.equalexperts.slack.profile

import com.equalexperts.slack.api.users.ProfilesSlackApi
import com.equalexperts.slack.api.users.TeamProfileFieldMetadata
import com.equalexperts.slack.api.users.UsersSlackApi
import com.equalexperts.slack.api.users.model.User
import com.equalexperts.slack.api.users.model.UserId
import org.slf4j.LoggerFactory
import kotlin.reflect.full.declaredMemberProperties

class ProfileChecker(private val usersSlackApi: UsersSlackApi, private val userProfilesSlackApi: ProfilesSlackApi) {

    private val logger = LoggerFactory.getLogger(this::class.java.name)

    fun process() {
        val users = UsersSlackApi.listAll(usersSlackApi)
        val teamCustomProfileFields = userProfilesSlackApi.teamProfile()
        val usersWithDetailedProfiles = users.filter { it.name == "adam" }.map { user -> addDetailedProfileToUser(user) }

        val requiredProfileFields = listOf("title", "real_name", "display_name")
        val requiredProfileFieldLabels = listOf("Home Base".toLowerCase())
        val requiredCustomProfileFields = teamCustomProfileFields.profile.fields.filter { profileField -> profileField.label.toLowerCase() in requiredProfileFieldLabels }


        val userProfileResults = mutableMapOf<User, ProfileCheckerResults>()

        for (user in usersWithDetailedProfiles) {
            val results = ProfileRequiredFieldsChecker().checkProfile(user, requiredProfileFields, requiredCustomProfileFields)
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


class ProfileRequiredFieldsChecker {

    private val logger = LoggerFactory.getLogger(this::class.java.name)

    fun checkProfile(user: User, requiredProfileFields: List<String>, requiredCustomProfileFields: List<TeamProfileFieldMetadata>): ProfileCheckerResults {
        val results = mutableMapOf<String, Boolean>()

        logger.debug("Checking profile fields for ${user.name}")
        val userProfile = user.profile

        for (profileField in requiredProfileFields) {
            val propertyValue = readProperty<String?>(
                    userProfile,
                    profileField
            )
            val propertyPresent = propertyValue?.let { true } ?: false
            results[profileField] = propertyPresent
        }
        logger.debug("Checked profile fields")

        logger.debug("Checking profile custom fields")
        val userProfileCustomFields = userProfile.fields
        userProfileCustomFields?.let { if (it.isEmpty()) logger.debug("No profile custom fields present") }
                ?: logger.debug("No profile custom fields found")

        for (profileCustomField in requiredCustomProfileFields) {

            val fieldPresent = userProfileCustomFields?.let {
                val matchingUserProfileCustomField = it[profileCustomField.id]
                val fieldPresent = matchingUserProfileCustomField?.let { true } ?: false
                fieldPresent
            } ?: false

            results[profileCustomField.label] = fieldPresent
        }
        logger.debug("Checked profile custom fields")


        return ProfileCheckerResults(results)
    }

    private fun <R : Any?> readProperty(instance: Any, propertyName: String): R {
        val clazz = instance.javaClass.kotlin
        @Suppress("UNCHECKED_CAST")
        return clazz.declaredMemberProperties.first { it.name == propertyName }.get(instance) as R
    }

}

data class ProfileCheckerResults(val results: Map<String, Boolean>) {
    fun getNumberOfFailedFields(): Int {
        return results.filter { !it.value }.size
    }

}