package com.equalexperts.slack.api.rest.model

import java.time.ZonedDateTime

object MessagesForTesting {
    fun botMessage(userId: String, botId: String, timeSent: ZonedDateTime) = Message("message", "bot_message", userId, botId, timeSent.toEpochSecond().toString())
}