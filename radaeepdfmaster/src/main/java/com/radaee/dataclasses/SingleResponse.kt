package com.radaee.dataclasses

/**
 * Data class to hold the response of a single message
 * Meant to be a reusable type for responses that only contain a message
 */
data class SingleResponse(
    val message: String
)