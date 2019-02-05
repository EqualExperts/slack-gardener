package com.equalexperts.slack.profile

import com.equalexperts.slack.api.users.TeamProfileFieldMetadata
import com.equalexperts.slack.api.users.model.User
import com.equalexperts.slack.api.users.model.UserProfile
import com.equalexperts.slack.api.users.model.UserProfileField
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class ProfileRequiredFieldsCheckerTest {

    @Test
    fun `should return passing results for present fields`() {
        val userProfile = UserProfile.testBot().copy(fields = hashMapOf("TEST_FIELD_ID" to UserProfileField("TEST_VALUE", "TEST_ALT")))
        val testUser = User(name = "TEST_BOT_USER",
                profile = userProfile,
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

        val profileRequiredFieldsChecker = ProfileRequiredFieldsChecker()

        val results = profileRequiredFieldsChecker.checkProfile(
                testUser,
                listOf("real_name"),
                listOf(TeamProfileFieldMetadata("TEST_FIELD_ID", 0, "TEST_LABEL", "TEST_HINT", "TEST_TYPE", null, null, false))
        )
        assertEquals(results, ProfileCheckerResults(mapOf("real_name" to true, "TEST_LABEL" to true)))
    }

    @Test
    fun `should return failing results for missing fields`() {
        val testUser = User(name = "TEST_BOT_USER",
                profile = UserProfile.testBot().copy(real_name = null, fields = emptyMap()),
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

        val profileRequiredFieldsChecker = ProfileRequiredFieldsChecker()

        val results = profileRequiredFieldsChecker.checkProfile(
                testUser,
                listOf("real_name"),
                listOf(TeamProfileFieldMetadata("TEST_FIELD_ID", 0, "TEST_LABEL", "TEST_HINT", "TEST_TYPE", null, null, false)))
        assertEquals(results, ProfileCheckerResults(mapOf("real_name" to false, "TEST_LABEL" to false)))
    }

}