package com.equalexperts.slack.gardener.rest.model

class User(val name: String, val profile: UserProfile) {

    class UsernameExpander : Expander<User>() {
        override fun expandParameter(value: User) = value.name
    }
}