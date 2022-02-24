package com.equalexperts.slack.api.rest.model

import java.time.ZonedDateTime

object SlackTestMessages {
    fun botMessage(userId: String, botId: String, timeSent: ZonedDateTime, message: String) =
        Message(
            text = message,
            type = "message",
            subtype = "bot_message",
            user = userId,
            bot_id = botId,
            ts = timeSent.toEpochSecond().toString()
        )

    fun userMessage(userId: String, botId: String?, timeSent: ZonedDateTime, message: String) =
        Message(
            text = message,
            type = "message",
            subtype = null,
            user = userId,
            bot_id = null,
            ts = timeSent.toEpochSecond().toString()
        )

    fun joinerMessage(userId: String, timeSent: ZonedDateTime) = Message(
        text = null,
        type = "message",
        subtype = "channel_join",
        user = userId,
        bot_id = null,
        ts = timeSent.toEpochSecond().toString()
    )

    fun leaverMessage(userId: String, timeSent: ZonedDateTime) = Message(
        text = null,
        type = "message",
        subtype = "channel_leave",
        user = userId,
        bot_id = null,
        ts = timeSent.toEpochSecond().toString()
    )
}
