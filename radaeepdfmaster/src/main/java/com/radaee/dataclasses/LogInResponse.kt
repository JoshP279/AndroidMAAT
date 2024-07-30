package com.radaee.dataclasses

/**
 * Data class to hold the response of a login request
 * Contains the role the marker has in the app
 * If a role is returned, the user has successfully logged in
 */
data class LogInResponse(
    val MarkerRole: String
)