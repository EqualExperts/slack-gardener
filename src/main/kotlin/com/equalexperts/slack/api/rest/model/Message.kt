package com.equalexperts.slack.api.rest.model

class Message(val text: String?, val type: String, val subtype: String?, val user: String?, val bot_id: String?, ts: String) {
    val timestamp = Timestamp(ts)
}
