package com.equalexperts.slack.channel

import com.equalexperts.slack.api.conversations.ConversationsSlackApi
import com.equalexperts.slack.api.users.UsersSlackApi
import com.equalexperts.slack.api.users.model.UserId
import org.slf4j.LoggerFactory
import kotlin.system.measureNanoTime

class ChannelMemberExportCsvFormat(private val conversationApi: ConversationsSlackApi, private val usersSlackApi: UsersSlackApi, private val channelname: String) {
    private val logger = LoggerFactory.getLogger(this::class.java.name)

    fun process() {
        val nanoTime = measureNanoTime {
            val eeAlumniChannel = ConversationsSlackApi.listAll(conversationApi).first { it.name == channelname }
            val members = conversationApi.members(eeAlumniChannel.id)

            val users = members.members
                    .map { it -> UserId(it) }
                    .map { it -> usersSlackApi.getUserInfo(it) }
                    .map { it -> "${it.user.name}, ${it.user.is_restricted}, ${it.user.is_ultra_restricted} \n" }


            println("name, single, multi\n")
            for (user in users) {
                println(user)
            }
        }

        logger.info("done in ${nanoTime / 1_000_000} ms")
    }

}
