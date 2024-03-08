package com.equalexperts.slack.channel

import java.time.*
import java.time.format.DateTimeFormatter
import java.util.*

class GardenerRelativeTimeGenerator(
    clock: Clock,
    val longIdlePeriod: Period = Period.ofDays(365),
    val idlePeriod: Period = Period.ofDays(30),
    val warningPeriod: Period = Period.ofDays(5)
) {
    // This can be hard to keep in your head, especially as time is relative _thanks Einstein_
    // (we don't actually care what the dates are here, as we only care about when messages were sent relative to Now.
    // So we've put the points in time we care about on a timeline
    //
    // LongIdleThreshold (A) -> IdleThreshold (B) -> WarningPeriod (C) -> Now (D)
    //
    // LongIdleThreshold is the point in time for channels that we only want to check the activity of rarely
    // IdleThreshold is relevant for 'normal' (i.e. not long idle period) channels
    // WarningPeriod is the time we want to wait after we've warned a channel that we'll archive if they're not active
    // Now is the time we're performing the check

    private var now: ZonedDateTime

    init {
        now = ZonedDateTime.now(clock)
    }

    fun beforeLongIdleThreshold(): ZonedDateTime {
        return now - longIdlePeriod - Period.ofDays( 5) // before A
    }

    fun longIdleThreshold(): ZonedDateTime {
        return now - longIdlePeriod // A
    }

    fun afterLongIdleThreshold(): ZonedDateTime {
        return now - longIdlePeriod  + Period.ofDays( 5) // after A
    }

    fun beforeIdleThreshold(): ZonedDateTime {
        return now - idlePeriod - Period.ofDays( 5) //  before B
    }

    fun idleThreshold(): ZonedDateTime {
        return now - idlePeriod // B
    }

    fun afterIdleThreshold(): ZonedDateTime {
        return now - idlePeriod + Period.ofDays( 5) // after B
    }

    fun beforeWarningThreshold(): ZonedDateTime {
        return now - warningPeriod - Period.ofDays( + 2) // before C
    }

    fun warningThreshold(): ZonedDateTime {
        return now - warningPeriod // C
    }

    fun afterWarningThreshold(): ZonedDateTime {
        return now - warningPeriod + Period.ofDays(2) // after C
    }

    fun beforeNow(): ZonedDateTime {
        return now - Period.ofDays(2) // before D
    }

    fun nowZonedDateTime(): ZonedDateTime {
        return now // D
    }
}
