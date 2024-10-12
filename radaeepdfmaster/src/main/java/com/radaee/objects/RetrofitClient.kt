package com.radaee.objects

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Environment
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.radaee.activities.LogInActivity
import com.radaee.activities.MainActivity
import com.radaee.activities.SubmissionsActivity
import com.radaee.api.API
import com.radaee.dataclasses.AssessmentResponse
import com.radaee.dataclasses.LogInResponse
import com.radaee.dataclasses.PDFResponse
import com.radaee.dataclasses.SingleResponse
import com.radaee.dataclasses.SubmissionsResponse
import com.radaee.dataclasses.UpdateMarkingStyleRequest
import com.radaee.dataclasses.UpdatePasswordRequest
import com.radaee.dataclasses.UpdateSubmissionMarkRequest
import com.radaee.dataclasses.UpdateSubmissionRequest
import com.radaee.pdfmaster.R
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File

/**
 * RetrofitClient is a singleton object that contains the Retrofit API client and functions to interact with the API.
 * The API client is created using Retrofit.Builder() and GsonConverterFactory.create().
 * Essentially, each request to the server is sent as a JSON File.
 * Similarly, the server responds with a JSON File.
 * The dataclasses are used to interpret the JSON Files into Kotlin objects, meaning they are CASE SENSITIVE.
 * So each data class value has to match the JSON key exactly, otherwise it will not be interpreted correctly.
 */
object RetrofitClient{
    /**
     * The base URL of the server.
     * Note that the IP address is hardcoded, so the server must be running on the same IP address as any clients
     * YOU MUST CHANGE THE IP ADDRESS TO THE IP ADDRESS THAT THE SERVER AND THE DEVICE IS CONNECTED TO IN ORDER TO WORK
     * DO NOT CHANGE PORT NUMBER, ONLY THE IP ADDRESS
     */
    private const val BASE_URL = "http://10.0.0.107:8080"
    val api: API by lazy {//
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(API::class.java)
    }

    /**
     * This function is used to attempt a login with the given email and password.
     * @param context The context of the activity that is calling the function (usually @LogInActivity).
     * @param email The email entered by the user.
     * @param password The password entered by the user.
     * If the login is successful, the email and password are saved to the Shared Preferences.
     * The user is then redirected to the MainActivity.
     */
    fun attemptLogin(context: Context, email:String, password:String) {
        api.login(email, password).enqueue(object : Callback<LogInResponse> {
            val rootView = (context as LogInActivity).findViewById<View>(android.R.id.content)
            override fun onResponse(
                call: Call<LogInResponse>,
                response: Response<LogInResponse>
            ) {
                if (response.isSuccessful) {
                    val resp = response.body()
                    if (resp?.MarkerRole.equals("Lecturer") || resp?.MarkerRole.equals("Demi")) {
                        SharedPref.saveString(context, "email", email)
                        SharedPref.saveString(context, "password", password)
                        if (resp?.MarkingStyle != null) SharedPref.saveString(
                            context,
                            "marking_style",
                            resp.MarkingStyle
                        )
                        else SharedPref.saveString(
                            context,
                            "marking_style",
                            context.getString(R.string.marking_style1)
                        ) //default marking style, most people are right handed
                        val intent = Intent(context, MainActivity::class.java)
                        SharedPref.saveBoolean(context, "OFFLINE_MODE", false)
                        context.startActivity(intent)
                        (context as LogInActivity).finish()
                    }
                }
                else {
                    SnackbarUtil.showErrorSnackBar(rootView, context.getString(R.string.invalid_username_or_password), context)
                }
            }
            override fun onFailure(call: Call<LogInResponse>, t: Throwable) {
                t.printStackTrace()
                SnackbarUtil.showErrorSnackBar(rootView, context.getString(R.string.server_connect_fail), context)
            }
        })
    }

    /**
     * This function is used to load assessments from the server.
     * @param context The context of the activity or fragment that is calling the function (usually @ViewAssessmentsFragment).
     * @param assessments The list of assessments that will be displayed in the RecyclerView. This list is updated with the fetched assessments.
     * @param filteredAssessments The list of assessments that will be displayed in the RecyclerView after filtering. This list is updated with the fetched assessments.
     * This list is used to filter the assessments based on the search query in ViewAssessmentsFragment.
     * @param assessmentsList The RecyclerView that displays the assessments.
     */
    fun loadAssessments(context: Context,rootView: View, assessments: ArrayList<AssessmentResponse>, filteredAssessments: ArrayList<AssessmentResponse>,assessmentsList: RecyclerView) {
        val savedEmail:String? = SharedPref.getString(context,"email", null)
        if (savedEmail.isNullOrEmpty()) {
            SnackbarUtil.showErrorSnackBar(rootView, context.getString(R.string.no_saved_email), context)
            return
        }
        api.getAssessments(savedEmail)
            .enqueue(object : Callback<List<AssessmentResponse>> {
                override fun onResponse(
                    call: Call<List<AssessmentResponse>>,
                    response: Response<List<AssessmentResponse>>
                ) {
                    if (response.isSuccessful) {
                        val fetchedAssessments = response.body()
                        if (fetchedAssessments != null) {
                            assessments.clear()
                            assessments.addAll(fetchedAssessments)
                            filteredAssessments.clear()
                            filteredAssessments.addAll(fetchedAssessments)
                            assessmentsList.adapter?.notifyDataSetChanged()
                            SharedPref.saveBoolean(context,"OFFLINE_MODE",false)
                        }
                    } else {
                        SnackbarUtil.showErrorSnackBar(rootView, context.getString(R.string.fail_load_assessments), context)
                    }
                }
                override fun onFailure(call: Call<List<AssessmentResponse>>, t: Throwable) {
                    t.printStackTrace()
                    SnackbarUtil.showErrorSnackBar(rootView, context.getString(R.string.server_connect_fail), context)
                }
            })
    }

    /**
     * This function is used to load assessments from the server.
     * @param context The context of the activity or fragment that is calling the function (usually @ViewAssessmentsFragment).
     * @param assessmentID The ID of the assessment that the submissions belong to.
     * @param submissions The list of submissions that will be downloaded.
     */
    fun getSubmissions(context: Context, rootView: View, assessmentID: Int, submissions: MutableList<SubmissionsResponse>, callback: () -> Unit) {
        api.getSubmissions(assessmentID)
            .enqueue(object : Callback<List<SubmissionsResponse>> {
                override fun onResponse(
                    call: Call<List<SubmissionsResponse>>,
                    response: Response<List<SubmissionsResponse>>
                ) {
                    if (response.isSuccessful) {
                        val fetchedSubmissions = response.body()
                        if (fetchedSubmissions != null) {
                            submissions.clear()
                            submissions.addAll(fetchedSubmissions)
                            callback() // Notify that data is ready
                        } else {
                            SnackbarUtil.showErrorSnackBar(rootView, context.getString(R.string.fail_load_submissions), context)
                        }
                    } else {
                        SnackbarUtil.showErrorSnackBar(rootView, context.getString(R.string.fail_load_submissions), context)
                    }
                }

                override fun onFailure(call: Call<List<SubmissionsResponse>>, t: Throwable) {
                    t.printStackTrace()
                    SnackbarUtil.showErrorSnackBar(rootView, context.getString(R.string.server_connect_fail), context)
                }
            })
    }

    /**
     * This function is used to load submissions from the server.
     * @param context The context of the activity or fragment that is calling the function (usually @SubmissionsActivity).
     * @param assessmentID The ID of the assessment that the submissions belong to.
     * @param submissions The list of submissions that will be displayed in the RecyclerView. This list is updated with the fetched submissions.
     * @param filteredSubmissions The list of submissions that will be displayed in the RecyclerView after filtering. This list is updated with the fetched submissions.
     * This list is used to filter the submissions based on the search query in SubmissionsActivity.
     * @param submissionsRecyclerView The RecyclerView that displays the submissions.
     */
    fun loadSubmissions(context: Context, rootView: View, assessmentID: Int, submissions: ArrayList<SubmissionsResponse>, filteredSubmissions: ArrayList<SubmissionsResponse>, submissionsRecyclerView: RecyclerView) {
        api.getSubmissions(assessmentID)
            .enqueue(object : Callback<List<SubmissionsResponse>> {
                override fun onResponse(
                    call: Call<List<SubmissionsResponse>>,
                    response: Response<List<SubmissionsResponse>>
                ) {
                    if (response.isSuccessful) {
                        val fetchedSubmissions = response.body()
                        if (fetchedSubmissions != null) {
                            submissions.clear()
                            submissions.addAll(fetchedSubmissions)
                            filteredSubmissions.clear()
                            filteredSubmissions.addAll(fetchedSubmissions)
                            submissionsRecyclerView.adapter?.notifyDataSetChanged()
                            SharedPref.saveBoolean(context,"OFFLINE_MODE",false)
                        }
                    } else {
                        SnackbarUtil.showErrorSnackBar(rootView, context.getString(R.string.fail_load_submissions), context)
                        (context as SubmissionsActivity).swipeRefreshLayout.isRefreshing = false
                    }
                }
                override fun onFailure(call: Call<List<SubmissionsResponse>>, t: Throwable) {
                    t.printStackTrace()
                    SnackbarUtil.showErrorSnackBar(rootView, context.getString(R.string.server_connect_fail), context)
                }
            })
    }

    /**
     * This function is used to update the submission status of a submission.
     * @param context The context of the activity or fragment that is calling the function (usually @SubmissionsActivity).
     * @param submissionID The ID of the submission that will be updated.
     * @param assessmentID The ID of the assessment that the submission belongs to.
     * @param studentNumber The student number of the student that submitted the submission.
     * @param submissionStatus The new status of the submission.
     * If the submission status is "Marked", the submission PDF is uploaded to the server.
     */
    fun updateSubmission(context: Context, rootView: View, submissionID: Int, assessmentID: Int, totalMarks:Int,submissionStatus: String, submissionFolderName: String, markingStyle: String) {
        api.updateSubmission(UpdateSubmissionRequest(submissionID, submissionStatus))
            .enqueue(object : Callback<SingleResponse> {
                override fun onResponse(call: Call<SingleResponse>, response: Response<SingleResponse>) {
                    if (response.isSuccessful) {
                        SnackbarUtil.showSuccessSnackBar(rootView, context.getString(R.string.submission_status_success), context)
                        if (submissionStatus == context.getString(R.string.marked)) {
                            uploadSubmissionPDF(context,rootView, assessmentID,submissionID, totalMarks, submissionFolderName, markingStyle)
                        }
                    } else {
                        SnackbarUtil.showErrorSnackBar(rootView, context.getString(R.string.submission_status_fail), context)
                    }
                }
                override fun onFailure(call: Call<SingleResponse>, t: Throwable) {
                    t.printStackTrace()
                    SnackbarUtil.showErrorSnackBar(rootView, context.getString(R.string.server_connect_fail), context)
                }
            })
    }

    /**
     * This function is used to upload the submission PDF to the server.
     * @param context The context of the activity or fragment that is calling the function (usually @SubmissionsActivity).
     * @param assessmentID The ID of the assessment that the submission belongs to.
     * @param submissionID The ID of the submission that will be updated.
     * @param studentNumber The student number of the student that submitted the submission.
     */
    private fun uploadSubmissionPDF(context: Context, rootView: View, assessmentID: Int, submissionID:Int, totalMarks: Int, submissionFolderName: String, markingStyle: String) {
        val pdfFile = FileUtil.getSubmissionFile(assessmentID, submissionID, submissionFolderName)
        if (pdfFile?.exists() == true) {
            val progressDialog = ProgressDialog(context)
            progressDialog.setMessage(context.getString(R.string.uploading_pdf))
            progressDialog.setCancelable(false)
            progressDialog.show()
            val mediaType = MediaType.parse("application/pdf")
            val requestFile = RequestBody.create(mediaType, pdfFile)
            val body = MultipartBody.Part.createFormData("pdfFile", pdfFile.name, requestFile)
            api.uploadSubmissionPDF(submissionID, totalMarks, markingStyle, body)
                .enqueue(object : Callback<SingleResponse> {
                    override fun onResponse(
                        call: Call<SingleResponse>,
                        response: Response<SingleResponse>
                    ) {
                        progressDialog.dismiss()
                        if (response.isSuccessful) {
                            SnackbarUtil.showSuccessSnackBar(rootView, context.getString(R.string.upload_succes), context)
                        } else {
                            SnackbarUtil.showErrorSnackBar(rootView, context.getString(R.string.upload_fail), context)
                            updateSubmission(context, rootView, submissionID, assessmentID, totalMarks, context.getString(R.string.in_progress), submissionFolderName, markingStyle) //if the upload fails, the submission status is set to in progress
                        }
                    }

                    override fun onFailure(call: Call<SingleResponse>, t: Throwable) {
                        progressDialog.dismiss()
                        t.printStackTrace()
                        SnackbarUtil.showErrorSnackBar(rootView, context.getString(R.string.server_connect_fail), context)
                    }
                })
        }else{
            SnackbarUtil.showErrorSnackBar(rootView, context.getString(R.string.pdf_not_found), context)
        }
    }

    /**
     * This function is used to download the submission PDF from the server.
     * @param context The context of the activity or fragment that is calling the function (usually @SubmissionsActivity).
     * @param submissionID The ID of the submission that will be downloaded.
     * @param submissionFolderName The folder name (generated by moodle or test drive) of the student that submitted the submission.
     * @param folderName The name of the folder that the PDF will be saved to.
     * @param callback The function that is called after the PDF is downloaded. The path of the PDF is passed to the function.
     * If the PDF is successfully downloaded, the path of the PDF is passed to the callback function.
     * If the PDF is not successfully downloaded, null is passed to the callback function.
     * Callbacks are used to handle the asynchronous nature of the function.
     */
    fun downloadSubmissionPDF(context: Context,rootView: View, submissionID: Int, submissionFolderName: String, folderName: String, showProgressDialog: Boolean,  callback: (String?) -> Unit) {
        val progressDialog = ProgressDialog(context)
        if (showProgressDialog){
            progressDialog.setMessage(context.getString(R.string.download_pdf))
            progressDialog.setCancelable(false) //to ensure that the user cannot cancel the download
            progressDialog.show()
        }
        api.getSubmissionPDF(submissionID).enqueue(object : Callback<PDFResponse> {
            override fun onResponse(call: Call<PDFResponse>, response: Response<PDFResponse>) {
                if (showProgressDialog) progressDialog.dismiss()
                if (response.isSuccessful) {
                    val resp = response.body()
                    if (resp != null) {
                        val byteArray = resp.pdfData.data.map { it.toByte() }.toByteArray()
                        val documentsDir = File(
                            Environment.getExternalStoragePublicDirectory(
                                Environment.DIRECTORY_DOCUMENTS
                            ), folderName
                        )
                        if (!documentsDir.exists()) {
                            documentsDir.mkdirs()
                        }
                        val path = FileUtil.saveSubmissionPDF(context,byteArray, submissionFolderName, documentsDir)
                        if (showProgressDialog) SnackbarUtil.showSuccessSnackBar(rootView, context.getString(R.string.pdf_downloaded), context)
                        callback(path)
                    } else {
                        callback(null)
                    }
                } else {
                    callback(null)
                }
            }

            override fun onFailure(call: Call<PDFResponse>, t: Throwable) {
                if (showProgressDialog) progressDialog.dismiss()
                t.printStackTrace()
                callback(null)
            }
        })
    }

    /**
     * This function is used to `download` the memo PDF from the server.
     * @param context The context of the activity or fragment that is calling the function (usually @SubmissionsActivity).
     * @param assessmentID The ID of the assessment that the memo belongs to.
     * @param folderName The name of the folder that the PDF will be saved to.
     * @param callback The function that is called after the PDF is downloaded. The path of the PDF is passed to the function.
     * If the PDF is successfully downloaded, the path of the PDF is passed to the callback function.
     * If the PDF is not successfully downloaded, null is passed to the callback function.
     * Callbacks are used to handle the asynchronous nature of the function.
     * The function is similar to downloadSubmissionPDF, but is used to download the memo PDF instead of the submission PDF.
     */
    fun downloadMemoPDF(context: Context,rootView: View, assessmentID: Int, folderName: String, showProgressDialog: Boolean,  callback: (String?) -> Unit) {
        val progressDialog = ProgressDialog(context)
        if (showProgressDialog)
        {
            progressDialog.setMessage(context.getString(R.string.download_memo_pdf))
            progressDialog.setCancelable(false) //to ensure that the user cannot cancel the download
            progressDialog.show()
        }
        api.getMemoPDF(assessmentID).enqueue(object : Callback<PDFResponse> {
            override fun onResponse(call: Call<PDFResponse>, response: Response<PDFResponse>) {
                if (showProgressDialog) progressDialog.dismiss()
                if (response.isSuccessful) {
                    val resp = response.body()
                    if (resp != null) {
                        val byteArray = resp.pdfData.data.map { it.toByte() }.toByteArray()
                        val documentsDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), folderName)
                        if (!documentsDir.exists()) {
                            documentsDir.mkdirs()
                        }
                        val path = FileUtil.saveMemoPDF(context,byteArray, assessmentID, documentsDir)
                        if (showProgressDialog) SnackbarUtil.showSuccessSnackBar(rootView, context.getString(R.string.memo_downloaded), context)
                        callback(path) //if path is not null, the callback is called with the path
                    } else {
                        callback(null) //if the response is null, the callback is called with null
                    }
                } else {
                    callback(null) //if the response is not successful, the callback is called with null
                }
            }
            override fun onFailure(call: Call<PDFResponse>, t: Throwable) {
                if (showProgressDialog) progressDialog.dismiss()
                t.printStackTrace()
                callback(null) //if the request fails, the callback is called with null
            }
        })
    }

    fun updateMarkingStyle(context: Context, rootView: View, markerEmail: String, markingStyle: String) {
        api.updateMarkingStyle(UpdateMarkingStyleRequest(markingStyle,markerEmail)).enqueue(object : Callback<SingleResponse> {
            override fun onResponse(call: Call<SingleResponse>, response: Response<SingleResponse>) {
                if (response.isSuccessful) {
                    SnackbarUtil.showSuccessSnackBar(rootView, context.getString(R.string.marking_style_updated), context)
                } else {
                    SnackbarUtil.showErrorSnackBar(rootView, context.getString(R.string.marking_style_update_fail), context)
                }
            }
            override fun onFailure(call: Call<SingleResponse>, t: Throwable) {
                t.printStackTrace()
                SnackbarUtil.showErrorSnackBar(rootView, context.getString(R.string.server_connect_fail), context)
            }
        })
    }

    fun updateSubmissionMark(context: Context, rootView: View, submissionID: Int, totalMarks: Number) {
        api.updateSubmissionMark(UpdateSubmissionMarkRequest(submissionID, totalMarks)).enqueue(object : Callback<SingleResponse> {
            override fun onResponse(call: Call<SingleResponse>, response: Response<SingleResponse>) {
                if (response.isSuccessful) {
                    SnackbarUtil.showSuccessSnackBar(rootView, context.getString(R.string.mark_updated), context)
                } else {
                    SnackbarUtil.showErrorSnackBar(rootView, context.getString(R.string.mark_update_fail), context)
                }
            }
            override fun onFailure(call: Call<SingleResponse>, t: Throwable) {
                t.printStackTrace()
                SnackbarUtil.showErrorSnackBar(rootView, context.getString(R.string.server_connect_fail), context)
            }
        })
    }
    fun updatePassword(context: Context, rootView: View, markerEmail: String, password: String, dialog: androidx.appcompat.app.AlertDialog){
        api.updatePassword(UpdatePasswordRequest(markerEmail, password)).enqueue(object : Callback<SingleResponse>{
            override fun onResponse(call: Call<SingleResponse>, response: Response<SingleResponse>) {
                if (response.isSuccessful) {
                    SharedPref.saveString(context,"password", password)
                    SnackbarUtil.showSuccessSnackBar(rootView, context.getString(R.string.mark_updated), context)
                } else {
                    SnackbarUtil.showErrorSnackBar(rootView, context.getString(R.string.mark_update_fail), context)
                }
                dialog.dismiss()
            }
            override fun onFailure(call: Call<SingleResponse>, t: Throwable) {
                t.printStackTrace()
                dialog.dismiss()
                SnackbarUtil.showErrorSnackBar(rootView, context.getString(R.string.server_connect_fail), context)
            }
        })
    }

}