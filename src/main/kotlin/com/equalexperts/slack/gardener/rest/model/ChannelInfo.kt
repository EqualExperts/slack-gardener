package com.equalexperts.slack.gardener.rest.model

import java.time.Instant
import java.time.ZoneOffset.UTC
import java.time.ZonedDateTime

class ChannelInfo(val id: String, val name: String, created: Long, num_members: Int) {
//    val created = Instant.ofEpochSecond(created)
//    val created: Instant by lazy {  Instant.ofEpochSecond(created) }

    val created = ZonedDateTime.ofInstant(Instant.ofEpochSecond(created), UTC)
//    val created: ZonedDateTime by lazy { ZonedDateTime.ofInstant(Instant.ofEpochSecond(created), UTC) }

    val members = num_members

    class ChannelIdExpander : Expander<ChannelInfo>() {
        override fun expandParameter(value: ChannelInfo) = value.id
    }
}