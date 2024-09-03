package com.radaee.dataclasses

/**
 * Data class for UpdateMarkingStyle
 * @param markingStyle: String, the marking style of the marker
 * @param markerEmail: String, the email of the marker
 */
data class UpdateMarkingStyleRequest(
    val markingStyle: String,
    val markerEmail: String
)
