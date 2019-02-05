package com.equalexperts.slack.api.users.model

import com.equalexperts.slack.api.rest.model.ResponseMetadata

data class UserList(val members: List<User>,
                    val response_metadata: ResponseMetadata) {
    companion object {
        fun withEmptyCursorToken(user: User) = UserList(listOf(user), ResponseMetadata(""))
        fun withCursorToken(user: User, cursor_token: String) = UserList(listOf(user), ResponseMetadata(cursor_token))
    }
}

