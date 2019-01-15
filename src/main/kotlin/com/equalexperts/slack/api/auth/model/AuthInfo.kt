package com.equalexperts.slack.api.auth.model

import com.equalexperts.slack.api.users.model.UserId
import com.fasterxml.jackson.annotation.JsonProperty

class AuthInfo(@JsonProperty("user_id") user_id: String) {
    val id = UserId(user_id)
}