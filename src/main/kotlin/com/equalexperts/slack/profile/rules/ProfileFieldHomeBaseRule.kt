package com.equalexperts.slack.profile.rules

import com.equalexperts.slack.api.profile.model.TeamProfile
import com.equalexperts.slack.api.users.model.User
import org.slf4j.LoggerFactory

class ProfileFieldHomeBaseRule(private val teamCustomProfileFields: TeamProfile) : ProfileFieldRule {

    private val logger = LoggerFactory.getLogger(this::class.java.name)

    override fun checkProfile(user: User): ProfileFieldRuleResult {
        logger.debug("Checking $FIELD_NAME field for ${user.name}")

        val homeBaseCustomField = teamCustomProfileFields.profile.fields.single { it.label.toLowerCase() == Companion.FIELD_NAME.toLowerCase() }

        val result = user.profile.fields?.let {
            val userProfileField = it[homeBaseCustomField.id]
            userProfileField?.let {
                val fieldPresent = !it.value.isNullOrBlank()
                fieldPresent
            } ?: false
        } ?: false

        logger.debug("Checked $FIELD_NAME field for ${user.name}: $result")
        return ProfileFieldRuleResult(FIELD_NAME, result)
    }

    companion object {
        const val FIELD_NAME = "Home Base"
    }

}
