package com.equalexperts.slack.profile.rules

import com.equalexperts.slack.api.users.model.User
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import org.apache.commons.codec.digest.DigestUtils
import org.slf4j.LoggerFactory


class ProfilePictureRule(private val knownDefaultPictureMd5Hashes: Set<String>) : ProfileFieldRule {

    private val logger = LoggerFactory.getLogger(this::class.java.name)

    override fun checkProfile(user: User): ProfileFieldRuleResult {
        logger.debug("Checking $FIELD_NAME field for ${user.name}")

        val result = user.profile.image_24?.let { checkIfDefaultProfilePicture(user, user.profile.image_24) } ?: false

        logger.debug("Checked $FIELD_NAME field for ${user.name}: $result")
        return ProfileFieldRuleResult(FIELD_NAME, result)
    }

    private fun checkIfDefaultProfilePicture(user: User, image_24: String): Boolean {
        val (_, response, result) = image_24.httpGet().response()

        when (result) {
            is Result.Failure<ByteArray, FuelError> -> {
                val ex = result.getException()
                throw Exception("Unable to retrieve Profile Picture for user ${user.name} - $image_24 {$ex}")
            }
            is Result.Success<ByteArray, FuelError> -> {
                val md5sum = DigestUtils.md5Hex(response.data)

                val nonDefaultProfilePicture = md5sum !in knownDefaultPictureMd5Hashes

                // Slack has changed the way it's obtaining it's default pictures
                // They moved away from a set of randomly generated pictures hosted on gravatar
                // And are now providing a 'd' parameter in the url that redirects it to a default picture if the profile picture isn't set
                // An example of the redirection url's are
                // https://secure.gravatar.com/avatar/49cb64e44ebcdf3443de7966fb18c7bc.jpg?s=24&d=https%3A%2F%2Fa.slack-edge.com%2Fdf10d%2Fimg%2Favatars%2Fava_0014-24.png
                // that redirects to https://i0.wp.com/a.slack-edge.com/df10d/img/avatars/ava_0014-24.png?ssl=1

                val nonRedirectedUrl = image_24 == response.url.toString()

                return nonDefaultProfilePicture && nonRedirectedUrl
            }
        }
    }

    companion object {
        const val FIELD_NAME = "profile picture"
    }

}
