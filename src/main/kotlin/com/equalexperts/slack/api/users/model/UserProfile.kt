package com.equalexperts.slack.api.users.model

import com.equalexperts.slack.api.rest.model.BotId
import com.fasterxml.jackson.annotation.JsonProperty

class UserProfile(@JsonProperty("bot_id") bot_id: String?) {
    val botId = bot_id?.let { BotId(it) }
}