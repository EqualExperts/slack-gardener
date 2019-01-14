package com.equalexperts.slack.rest.model

class Message(val type: String, val subtype: String?, val user: String?, bot_id: String?, ts: String) {
    val timestamp  = Timestamp(ts)
    val botId = bot_id?.let { BotId(it) }
}