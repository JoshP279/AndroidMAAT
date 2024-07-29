package com.radaee.dataclasses

/**
 * Data class for the response of the assessment
 * @param assessmentID the ID of the assessment
 * @param moduleCode the module code of the assessment
 * @param assessmentName the name of the assessment
 * @param numMarked the number of submissions that have been marked
 * @param totalSubmissions the total number of submissions
 */
data class AssessmentResponse(
    val assessmentID: Int,
    val moduleCode : String,
    val assessmentName : String,
    val numMarked: Int,
    val totalSubmissions: Int
)
