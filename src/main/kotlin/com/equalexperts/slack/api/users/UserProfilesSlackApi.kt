package com.equalexperts.slack.api.users

import com.equalexperts.slack.api.rest.SlackRetrySupport
import com.equalexperts.slack.api.rest.SlackRetrySupport.SlackErrorDecoder
import com.equalexperts.slack.api.rest.feignBuilder
import com.equalexperts.slack.api.users.model.User
import com.equalexperts.slack.api.users.model.UserId
import com.equalexperts.slack.api.users.model.UserInfo
import com.equalexperts.slack.api.users.model.UserProfile
import feign.Param
import feign.RequestLine
import java.net.URI

interface UserProfilesSlackApi {


    @RequestLine("GET /api/users.profile.get?user={user}")
    fun profile(@Param("user") userId: UserId): UserProfile

    companion object {
        fun factory(uri: URI, token: String, sleeper: (Long) -> Unit): UserProfilesSlackApi {
            return feignBuilder()
                    .requestInterceptor { it.query("token", token) }
                    .errorDecoder(SlackErrorDecoder())
                    .retryer(SlackRetrySupport(sleeper))
                    .target(UserProfilesSlackApi::class.java, uri.toString())
        }
    }


}

