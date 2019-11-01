package com.equalexperts.slack.api.profile.model

data class TeamProfile(val profile: TeamProfileDetails)

fun TeamProfile.getFieldMetadata(fieldName: String): TeamProfileFieldMetadata {
    return profile.fields.single { it.label.toLowerCase() == fieldName.toLowerCase() }
}

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
