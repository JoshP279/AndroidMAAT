package com.radaee.dataclasses

/**
 * Data class to hold the response of a submission
 * @param submissionID: Int, the ID of the submission
 * @param assessmentID: Int, the ID of the assessment
 * @param studentNumber: String, the student number of the student who submitted the assessment
 * @param studentName: String, the name of the student who submitted the assessment
 * @param studentSurname: String, the surname of the student who submitted the assessment
 * @param submissionStatus: String, the status of the submission
 */
data class SubmissionsResponse(
    val submissionID: Int,
    val assessmentID: Int,
    val studentNumber: String,
    val studentName: String,
    val studentSurname:String,
    val submissionStatus: String,
    val submissionFolderName: String
)
