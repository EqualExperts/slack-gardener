package com.equalexperts.slack.gardener

import java.time.ZonedDateTime

sealed class ChannelState {
    /*
        A channel with at least one message from a human during the idle period
     */
    object Active: ChannelState()
    /*
        A channel without any messages from humans during the idle period
     */
    object Stale: ChannelState()
    /*
        A channel without no messages from humans and a warning (message) from this bot during the idle period
     */
    class StaleAndWarned(val oldestWarning: ZonedDateTime) : ChannelState()
}