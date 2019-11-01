package com.equalexperts.slack.api.users.model

import com.equalexperts.slack.api.profile.model.UserProfile
import com.equalexperts.slack.api.rest.model.Expander
import com.equalexperts.slack.api.rest.model.ResponseMetadata


data class UserInfo(val user: User)

data class UserId(val id: String) {
    override fun toString() = id
}

data class User(val name: String,
                val profile: UserProfile,
                val id: String,
                val team_id: String,
                val deleted: Boolean,
                val is_admin: Boolean,
                val is_owner: Boolean,
                val is_primary_owner: Boolean,
                val is_restricted: Boolean,
                val is_ultra_restricted: Boolean,
                val is_bot: Boolean,
                val is_app_user: Boolean
) {

    class UsernameExpander : Expander<User>() {
        override fun expandParameter(value: User) = value.name
    }

    class UserIdExpander : Expander<User>() {
        override fun expandParameter(value: User) = value.id
    }

}

data class UserList(val members: List<User>,
                    val response_metadata: ResponseMetadata)
