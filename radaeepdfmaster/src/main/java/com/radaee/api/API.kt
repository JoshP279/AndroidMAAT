package com.radaee.api

import com.radaee.dataclasses.AssessmentResponse
import com.radaee.dataclasses.LogInResponse
import com.radaee.dataclasses.PDFResponse
import com.radaee.dataclasses.SingleResponse
import com.radaee.dataclasses.SubmissionsResponse
import com.radaee.dataclasses.UpdateMarkingStyleRequest
import com.radaee.dataclasses.UpdatePasswordRequest
import com.radaee.dataclasses.UpdateSubmissionMarkRequest
import com.radaee.dataclasses.UpdateSubmissionRequest
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Query

/**
 * API interface for Retrofit
 * Contains all the API calls
 * All implementations can be seen in @RetrofitClient
 */
interface API {
    /**
     * Login API
     * @param markerEmail: Email of the marker
     * @param password: Password of the marker
     * @return SingleResponse containing the markerRole of the login request
     */
    @GET("/login")
    fun login(
        @Query("MarkerEmail") markerEmail: String,
        @Query("Password") password: String
    ): Call<LogInResponse>

    /**
     * Get all assessments for a marker
     * @param markerEmail: Email of the marker
     * @return List<AssessmentResponse> containing assessments for the marker
     */
    @GET("/assessments")
    fun getAssessments(
        @Query("MarkerEmail") markerEmail: String
    ): Call<List<AssessmentResponse>>

    /**
     * Get all submissions for an assessment
     * @param assessmentID: ID of the assessment
     * @return List<SubmissionsResponse> containing submissions for the assessment
     */
    @GET("/submissions")
    fun getSubmissions(
        @Query("AssessmentID") assessmentID: Int
    ): Call<List<SubmissionsResponse>>

    /**
     * Update the status of a submission
     * @param request: UpdateSubmissionRequest
     * @return SingleResponse containing the result of the update request
     */
    @PUT("/updateSubmissionStatus")
    fun updateSubmission(
        @Body request: UpdateSubmissionRequest
    ): Call<SingleResponse>

    /**
     * Get the PDF of a submission
     * @param submissionID: ID of the submission
     * @return PDFResponse containing the submission PDF
     */
    @GET("/submissionPDF")
    fun getSubmissionPDF(
        @Query("SubmissionID") submissionID: Int
    ): Call<PDFResponse>

    /**
     * Get the PDF of a memo
     * @param assessmentID: ID of the assessment
     * @return PDFResponse containing the memo PDF
     */
    @GET("/memoPDF")
    fun getMemoPDF(
        @Query("AssessmentID") assessmentID: Int
    ): Call<PDFResponse>

    /**
     * Upload the marked submission PDF
     * @Multipart must be used here, as the submissionID, assessmentID and pdfFile are sent as form-data in the request (this is necessary for the pdfFile to be handled correctly by server)
     * @param submissionID: ID of the submission
     * @param totalMarks: Total marks of the submission
     * @param markingStyle: Marking style of the submission
     * @param pdfFile: MultipartBody.Part
     * @return SingleResponse containing the result of the upload request
     */
    @Multipart
    @PUT("/uploadMarkedSubmission")
    fun uploadSubmissionPDF(
        @Part("submissionID") submissionID: Int,
        @Part("totalMarks") totalMarks: Int,
        @Part("markingStyle") markingStyle: String,
        @Part pdfFile: MultipartBody.Part
    ): Call<SingleResponse>

    @PUT("/updateMarkingStyle")
    fun updateMarkingStyle(
        @Body request: UpdateMarkingStyleRequest
    ): Call<SingleResponse>

    @PUT("/updateSubmissionMark")
    fun updateSubmissionMark(
        @Body request: UpdateSubmissionMarkRequest
    ): Call<SingleResponse>

    @PUT("/updatePassword")
    fun updatePassword(
        @Body request: UpdatePasswordRequest
    ): Call<SingleResponse>

}