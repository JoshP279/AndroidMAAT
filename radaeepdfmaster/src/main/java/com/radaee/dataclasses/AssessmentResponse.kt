package com.radaee.dataclasses

data class AssessmentResponse(
    val assessmentID: Int,
    val moduleCode : String,
    val assessmentName : String,
    val numMarked: Int,
    val totalSubmissions: Int
)
