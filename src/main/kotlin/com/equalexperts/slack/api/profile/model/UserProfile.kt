package com.equalexperts.slack.api.profile.model


data class UserProfileWrapper(
        val profile: UserProfile
)

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
                       val fields: Map<String, UserProfileField>?,
                       val email: String
)

data class UserProfileField(val value: String, val alt: String)
