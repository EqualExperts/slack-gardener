package com.equalexperts.slack.profile.rules

import com.equalexperts.slack.api.profile.model.TeamProfile
import com.equalexperts.slack.api.profile.model.TeamProfileDetails
import com.equalexperts.slack.api.profile.model.TeamProfileFieldMetadata
import com.equalexperts.slack.api.profile.model.UserProfileField
import com.equalexperts.slack.api.users.UsersForTesting
import com.equalexperts.slack.profile.UserProfilesForTesting
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class ProfileFieldHomeBaseRuleTest {

    @Test
    fun `should return true for home base custom field presence`() {
        val userProfile = UserProfilesForTesting.testBotProfile().copy(fields = hashMapOf("TEST_FIELD_ID" to UserProfileField("TEST_VALUE", "TEST_ALT")))
        val testUser = UsersForTesting.testBot(userProfile)

        val teamProfile = TeamProfile(TeamProfileDetails(listOf(TeamProfileFieldMetadata("TEST_FIELD_ID", 0, ProfileFieldHomeBaseRule.FIELD_NAME, "TEST_HINT", "TEST_TYPE", null, null, false))))
        val rule = ProfileFieldHomeBaseRule(teamProfile)

        val result = rule.checkProfile(testUser)
        assertTrue(result.result)
    }

    @Test
    fun `should return false for home base custom field absence`() {
        val userProfile = UserProfilesForTesting.testBotProfile().copy(fields = null)
        val testUser = UsersForTesting.testBot(userProfile)

        val teamProfile = TeamProfile(TeamProfileDetails(listOf(TeamProfileFieldMetadata("TEST_FIELD_ID", 0, ProfileFieldHomeBaseRule.FIELD_NAME, "TEST_HINT", "TEST_TYPE", null, null, false))))
        val rule = ProfileFieldHomeBaseRule(teamProfile)

        val result = rule.checkProfile(testUser)
        assertFalse(result.result)
    }
}