package com.equalexperts.slack.rest.model

class User(val name: String, val profile: UserProfile) {

    class UsernameExpander : Expander<User>() {
        override fun expandParameter(value: User) = value.name
    }
}