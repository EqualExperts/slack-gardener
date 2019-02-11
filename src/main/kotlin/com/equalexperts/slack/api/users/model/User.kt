package com.equalexperts.slack.api.users.model

import com.equalexperts.slack.api.rest.model.Expander
import com.equalexperts.slack.api.rest.model.ResponseMetadata
import com.equalexperts.slack.api.profile.model.UserProfile


data class UserInfo(val user: User)

data class UserId(val id: String) {
    override fun toString() = id
}

data class User(val name: String,
                val profile: UserProfile,
                val id: String,
                val team_id: String,
                val is_deleted: Boolean,
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

}

data class UserList(val members: List<User>,
                    val response_metadata: ResponseMetadata) {
    companion object {
        fun withEmptyCursorToken(user: User) = UserList(listOf(user), ResponseMetadata(""))
        fun withCursorToken(user: User, cursor_token: String) = UserList(listOf(user), ResponseMetadata(cursor_token))
    }
}
