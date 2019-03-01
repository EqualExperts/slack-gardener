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

                return md5sum !in knownDefaultPictureMd5Hashes
            }
        }
    }

    companion object {
        const val FIELD_NAME = "profile picture"
    }

}
