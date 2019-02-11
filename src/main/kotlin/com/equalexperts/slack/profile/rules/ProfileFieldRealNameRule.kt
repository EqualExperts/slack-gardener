package com.equalexperts.slack.profile.rules

import com.equalexperts.slack.api.users.model.User
import org.slf4j.LoggerFactory

class ProfileFieldRealNameRule : ProfileFieldRule {

    private val logger = LoggerFactory.getLogger(this::class.java.name)

    override fun checkProfile(user: User): ProfileFieldRuleResult {

        logger.debug("Checking $FIELD_NAME field for ${user.name}")

        val result = ! user.profile.real_name.isNullOrBlank()

        logger.debug("Checked $FIELD_NAME field for ${user.name}: $result")
        return ProfileFieldRuleResult(FIELD_NAME, result)
    }

    companion object {
        const val FIELD_NAME = "Real Name"
    }

}
