package com.radaee.api

import com.radaee.dataclasses.AssessmentResponse
import com.radaee.dataclasses.PDFResponse
import com.radaee.dataclasses.SingleResponse
import com.radaee.dataclasses.SubmissionsResponse
import com.radaee.dataclasses.UpdateSubmissionRequest
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Query

interface API {
    @GET("/androidLogin")
    fun login(
        @Query("MarkerEmail") markerEmail: String,
        @Query("Password") password: String
    ):Call<SingleResponse>

    @GET("/assessments")
    fun getAssessments(
        @Query("MarkerEmail") markerEmail: String
    ): Call<List<AssessmentResponse>>

    @GET("/submissions")
    fun getSubmissions(
        @Query("AssessmentID") assessmentID: Int
    ): Call<List<SubmissionsResponse>>

    @PUT("/updateSubmission")
    fun updateSubmission(
        @Body request: UpdateSubmissionRequest
    ): Call<SingleResponse>

    @GET("/submissionPDF")
    fun getSubmissionPDF(
        @Query("SubmissionID") submissionID: Int
    ): Call<PDFResponse>

    @GET("/memoPDF")
    fun getMemoPDF(
        @Query("AssessmentID") assessmentID: Int
    ): Call<PDFResponse>

    @Multipart
    @PUT("/uploadSubmission")
    fun uploadSubmissionPDF(
        @Part("submissionID") submissionID: Int,
        @Part("assessmentID") assessmentID: Int,
        @Part pdfFile: MultipartBody.Part
    ): Call<SingleResponse>
}