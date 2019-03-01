package com.equalexperts.slack.api.users

import com.equalexperts.slack.api.rest.SlackRetrySupport
import com.equalexperts.slack.api.rest.SlackRetrySupport.SlackErrorDecoder
import com.equalexperts.slack.api.rest.feignBuilder
import com.equalexperts.slack.api.users.model.User
import com.equalexperts.slack.api.users.model.UserId
import com.equalexperts.slack.api.users.model.UserInfo
import com.equalexperts.slack.api.users.model.UserList
import feign.Param
import feign.RequestLine
import org.slf4j.LoggerFactory
import java.net.URI

interface UsersSlackApi {

    @RequestLine("GET /api/users.info?user={user}")
    fun getUserInfo(@Param("user") userId: UserId): UserInfo

    @RequestLine("GET /api/users.list?cursor={cursorValue}")
    fun list(@Param("cursorValue") cursorValue: String = ""): UserList

    companion object {

        fun factory(uri: URI, token: String, sleeper: (Long) -> Unit): UsersSlackApi {
            return feignBuilder()
                    .requestInterceptor { it.query("token", token) }
                    .errorDecoder(SlackErrorDecoder())
                    .retryer(SlackRetrySupport(sleeper))
                    .target(UsersSlackApi::class.java, uri.toString())
        }
    }
}

fun UsersSlackApi.listAll(): Set<User> {
    val logger = LoggerFactory.getLogger(this::class.java.name)

    val users = mutableSetOf<User>()
    var moreChannelsToList: Boolean
    var cursorValue = ""
    do {
        val listResults = list(cursorValue)
        val nextCursor = listResults.response_metadata.next_cursor

        logger.debug("Users found, adding to list ${listResults.members}")
        users += listResults.members

        if (!nextCursor.isBlank()) {
            logger.debug("Found new cursor token to retrieve more users from, using cursor token $nextCursor")
            moreChannelsToList = true
            cursorValue = nextCursor

        } else {
            logger.debug("No new cursor token, all users found")
            moreChannelsToList = false
        }

    } while (moreChannelsToList)
    logger.info("${users.size} users found")
    return users
}
