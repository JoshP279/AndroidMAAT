package com.radaee.dataclasses

data class SubmissionsResponse(
    val submissionID: Int,
    val assessmentID: Int,
    val studentNumber: String,
    val studentName: String,
    val studentSurname:String,
    val submissionStatus: String
)
