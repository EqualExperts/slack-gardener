package com.equalexperts.slack.api.rest.model

import java.time.Instant
import java.time.ZoneOffset.UTC
import java.time.ZonedDateTime

data class Timestamp constructor(private val opaque: String) {
    constructor(timeBased: ZonedDateTime) : this(timeBased.toEpochSecond().toString())

    override fun toString(): String = opaque
    fun toZonedDateTime(): ZonedDateTime {
        return Instant.ofEpochSecond(opaque.split(".")[0].toLong()).atZone(UTC)
    }
}