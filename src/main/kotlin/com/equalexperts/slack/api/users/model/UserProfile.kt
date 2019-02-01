package com.equalexperts.slack.api.users.model

data class UserProfile(val bot_id: String?,
                       val real_name: String?,
                       val real_name_normalized: String?,
                       val display_name: String?,
                       val display_name_normalized: String?,
                       val title: String?,
                       val fields: Map<String, UserProfileField>?
)