package com.equalexperts.slack.api.users.model

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class UserProfileTest {

    @Test
    fun `should return true when all image url is set`() {
        val profile = UserProfile.testBot()

        assertTrue(profile.hasProfilePicture())
    }

    @Test
    fun `should return true when image original url and original 1024 is missing and gravatar in URL`() {
        val profile = UserProfile.testBot().copy(
                image_24 = "gravatar",
                image_32 = "gravatar",
                image_48 = "gravatar",
                image_72 = "gravatar",
                image_192 = "gravatar",
                image_512 = "gravatar")

        assertTrue(profile.hasProfilePicture())
    }

    @Test
    fun `should return false when all image urls are missing`() {
        val profile = UserProfile.testBot().copy(image_original = null,
                image_24 = null,
                image_32 = null,
                image_48 = null,
                image_72 = null,
                image_192 = null,
                image_512 = null,
                image_1024 = null)
        assertFalse(profile.hasProfilePicture())
    }
}