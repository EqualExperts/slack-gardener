package com.equalexperts.slack.rest.model

import com.fasterxml.jackson.annotation.JsonProperty

class AuthInfo(@JsonProperty("user_id") user_id: String) {
    val id = UserId(user_id)
}