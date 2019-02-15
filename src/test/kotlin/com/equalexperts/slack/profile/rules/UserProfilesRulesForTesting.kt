package com.equalexperts.slack.profile.rules

import com.equalexperts.slack.api.users.model.User

object UserProfilesRulesForTesting {

    fun testPassingRule(fieldName: String): ProfileFieldRule {

        class TestRule: ProfileFieldRule {

            override fun checkProfile(user: User): ProfileFieldRuleResult {

                return ProfileFieldRuleResult(fieldName, true)
            }
        }

        return TestRule()
    }

    fun testFailingRule(fieldName: String): ProfileFieldRule {

        class TestRule: ProfileFieldRule {

            override fun checkProfile(user: User): ProfileFieldRuleResult {

                return ProfileFieldRuleResult(fieldName, false)
            }
        }

        return TestRule()
    }

}