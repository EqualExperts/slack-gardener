package com.equalexperts.slack.profile.rules

import com.equalexperts.slack.api.users.model.User

interface ProfileFieldRule {
    fun checkProfile(user: User): ProfileFieldRuleResult
}

data class ProfileFieldRuleResult(val field: String, val result: Boolean)
