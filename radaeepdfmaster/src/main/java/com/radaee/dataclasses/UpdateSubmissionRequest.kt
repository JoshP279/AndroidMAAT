package com.radaee.dataclasses

/**
 * Data class for UpdateSubmissionRequest
 * @param submissionID: Int, the ID of the submission
 * @param submissionStatus: String, the status of the submission
 */
data class UpdateSubmissionRequest(
    val submissionID: Int,
    val submissionStatus: String
)