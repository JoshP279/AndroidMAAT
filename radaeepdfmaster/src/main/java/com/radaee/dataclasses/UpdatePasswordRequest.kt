package com.radaee.dataclasses

data class UpdatePasswordRequest(
    val markerEmail: String,
    val password: String
)
