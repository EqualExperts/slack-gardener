package com.equalexperts.slack.api.rest.model

import java.time.ZonedDateTime

object MessagesForTesting {
    fun botMessage(userId: String, botId: String, timeSent: ZonedDateTime, message: String) = Message(message, "message", "bot_message", userId, botId, timeSent.toEpochSecond().toString())
    fun userMessage(userId: String, botId: String?, timeSent: ZonedDateTime, message: String) = Message(message, "message", null, userId, botId, timeSent.toEpochSecond().toString())
}