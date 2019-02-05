package com.equalexperts.slack.api.users.model


data class UserProfile(val bot_id: String?,
                       val real_name: String?,
                       val real_name_normalized: String?,
                       val display_name: String?,
                       val display_name_normalized: String?,
                       val first_name: String?,
                       val last_name: String?,
                       val title: String?,
                       val image_original: String?,
                       val image_24: String?,
                       val image_32: String?,
                       val image_48: String?,
                       val image_72: String?,
                       val image_192: String?,
                       val image_512: String?,
                       val image_1024: String?,
                       var fields: Map<String, UserProfileField>?
) {
    
    companion object {
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

}