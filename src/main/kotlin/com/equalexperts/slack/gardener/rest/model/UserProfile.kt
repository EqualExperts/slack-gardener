package com.equalexperts.slack.gardener.rest.model

import com.fasterxml.jackson.annotation.JsonProperty

class UserProfile(@JsonProperty("bot_id") bot_id: String?) {
    val botId = bot_id?.let { BotId(it) }
}