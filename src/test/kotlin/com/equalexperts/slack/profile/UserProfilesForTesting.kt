package com.equalexperts.slack.profile

import com.equalexperts.slack.api.profile.model.UserProfile

object UserProfilesForTesting {
    fun testBot(): UserProfile = UserProfile("TEST_BOT_ID",
            "TEST_REAL_NAME",
            "TEST_REAL_NAME_NORMALISED",
            "TEST_DISPLAY_NAME",
            "TEST_DISPLAY_NAME_NORMALISED",
            "TEST_FIRST_NAME",
            "TEST_LAST_NAME",
            "TEST_TITLE",
            "TEST_IMAGE_ORIGINAL_URL",
            "TEST_IMAGE_24_URL",
            "TEST_IMAGE_32_URL",
            "TEST_IMAGE_48_URL",
            "TEST_IMAGE_72_URL",
            "TEST_IMAGE_192_URL",
            "TEST_IMAGE_512_URL",
            "TEST_IMAGE_1024_URL",
            mapOf())
}