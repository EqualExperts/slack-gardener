package com.equalexperts.slack.api.users.model

import com.equalexperts.slack.api.rest.model.ResponseMetadata

object UserListsForTesting {

    fun withEmptyCursorToken(user: User) = UserList(listOf(user), ResponseMetadata(""))
    fun withEmptyCursorToken(users: List<User>) = UserList(users, ResponseMetadata(""))
    fun withCursorToken(user: User, cursor_token: String) = UserList(listOf(user), ResponseMetadata(cursor_token))
    fun withCursorToken(users: List<User>, cursor_token: String) = UserList(users, ResponseMetadata(cursor_token))

}