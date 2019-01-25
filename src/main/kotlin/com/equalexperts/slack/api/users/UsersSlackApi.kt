package com.equalexperts.slack.api.users

import com.equalexperts.slack.api.rest.SlackRetrySupport
import com.equalexperts.slack.api.rest.SlackRetrySupport.SlackErrorDecoder
import com.equalexperts.slack.api.rest.feignBuilder
import com.equalexperts.slack.api.users.model.UserId
import com.equalexperts.slack.api.users.model.UserInfo
import feign.Param
import feign.RequestLine
import java.net.URI

interface UsersSlackApi {


    @RequestLine("GET /api/users.info?user={user}")
    fun getUserInfo(@Param("user") userId: UserId) : UserInfo

    @RequestLine("GET /api/users.list")
    fun list(@Param("user") userId: UserId) : UserInfo

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