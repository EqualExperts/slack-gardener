package com.equalexperts.slack.api.users

import com.equalexperts.slack.api.rest.SlackRetrySupport
import com.equalexperts.slack.api.rest.SlackRetrySupport.SlackErrorDecoder
import com.equalexperts.slack.api.rest.feignBuilder
import com.equalexperts.slack.api.users.model.UserId
import com.equalexperts.slack.api.users.model.UserProfileWrapper
import feign.Param
import feign.RequestLine
import java.net.URI

interface ProfilesSlackApi {

    @RequestLine("GET /api/team.profile.get?visibility=all")
    fun teamProfile(): TeamProfile

    @RequestLine("GET /api/users.profile.get?user={user}")
    fun userProfile(@Param("user") userId: UserId): UserProfileWrapper

    companion object {
        fun factory(uri: URI, token: String, sleeper: (Long) -> Unit): ProfilesSlackApi {
            return feignBuilder()
                    .requestInterceptor { it.query("token", token) }
                    .errorDecoder(SlackErrorDecoder())
                    .retryer(SlackRetrySupport(sleeper))
                    .target(ProfilesSlackApi::class.java, uri.toString())
        }
    }

}

data class TeamProfile(val profile: TeamProfileDetails)
data class TeamProfileDetails(val fields: List<TeamProfileFieldMetadata>)
data class TeamProfileFieldMetadata(val id: String,
                                    val ordering: Int,
                                    val label: String,
                                    val hint: String,
                                    val type: String,
                                    val possible_values: List<String>?,
                                    val options: List<String>?,
                                    val is_hidden: Boolean
                                    )

