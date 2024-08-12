package com.radaee.objects

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Environment
import android.widget.Toast
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
object RetrofitClient {
    /**
     * The base URL of the server.
     * Note that the IP address is hardcoded, so the server must be running on the same IP address as any clients
     * YOU MUST CHANGE THE IP ADDRESS TO THE IP ADDRESS THAT THE SERVER AND THE DEVICE IS CONNECTED TO IN ORDER TO WORK
     * DO NOT CHANGE PORT NUMBER, ONLY THE IP ADDRESS
     */
    private const val BASE_URL = "http://10.0.0.110:3306/"
    val api: API by lazy {
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
            override fun onResponse(
                call: Call<LogInResponse>,
                response: Response<LogInResponse>
            ) {
                if (response.isSuccessful) {
                    val resp = response.body()
                    if (resp?.MarkerRole.equals("Lecturer") || resp?.MarkerRole.equals("Demi")) {
                        SharedPref.saveString(context,"email", email)
                        SharedPref.saveString(context,"password", password)
                        val intent = Intent(context, MainActivity::class.java)
                        SharedPref.saveBoolean(context,"OFFLINE_MODE",false)
                        context.startActivity(intent)
                        (context as LogInActivity).finish()
                    }
                } else {
                    Toast.makeText(context,
                        context.getString(R.string.invalid_username_or_password), Toast.LENGTH_SHORT)
                        .show()
                }
            }
            override fun onFailure(call: Call<LogInResponse>, t: Throwable) {
                t.printStackTrace()
                Toast.makeText(context, context.getString(R.string.server_connect_fail), Toast.LENGTH_SHORT).show()
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
    fun loadAssessments(context: Context,assessments: ArrayList<AssessmentResponse>, filteredAssessments: ArrayList<AssessmentResponse>,assessmentsList: RecyclerView) {
        val savedEmail:String? = SharedPref.getString(context,"email", null)
        if (savedEmail.isNullOrEmpty()) {
            Toast.makeText(context, "No saved email found", Toast.LENGTH_SHORT).show()
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
                        Toast.makeText(
                            context,
                            context.getString(R.string.fail_load_assessments),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                override fun onFailure(call: Call<List<AssessmentResponse>>, t: Throwable) {
                    t.printStackTrace()
                    Toast.makeText(
                        context,
                        context.getString(R.string.server_connect_fail),
                        Toast.LENGTH_SHORT
                    ).show()
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
    fun loadSubmissions(context: Context, assessmentID: Int, submissions: ArrayList<SubmissionsResponse>, filteredSubmissions: ArrayList<SubmissionsResponse>, submissionsRecyclerView: RecyclerView) {
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
                        Toast.makeText(
                            context,
                            context.getString(R.string.fail_load_submissions),
                            Toast.LENGTH_SHORT
                        ).show()
                        (context as SubmissionsActivity).swipeRefreshLayout.isRefreshing = false
                    }
                }
                override fun onFailure(call: Call<List<SubmissionsResponse>>, t: Throwable) {
                    t.printStackTrace()
                    Toast.makeText(
                        context,
                        context.getString(R.string.server_connect_fail),
                        Toast.LENGTH_SHORT
                    ).show()
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
    fun updateSubmission(context: Context, submissionID: Int, assessmentID: Int,studentNumber: String,submissionStatus: String) {
        if (submissionStatus == context.getString(R.string.marked)) {
            uploadSubmissionPDF(context,assessmentID,submissionID, studentNumber)
        }
        api.updateSubmission(UpdateSubmissionRequest(submissionID, submissionStatus))
            .enqueue(object : Callback<SingleResponse> {
                override fun onResponse(call: Call<SingleResponse>, response: Response<SingleResponse>) {
                    if (response.isSuccessful) {
                        Toast.makeText(
                            context,
                            context.getString(R.string.submission_status_success),
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            context,
                            context.getString(R.string.submission_status_fail),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                override fun onFailure(call: Call<SingleResponse>, t: Throwable) {
                    t.printStackTrace()
                    Toast.makeText(
                        context,
                        context.getString(R.string.server_connect_fail),
                        Toast.LENGTH_SHORT
                    ).show()
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
    private fun uploadSubmissionPDF(context: Context, assessmentID: Int, submissionID:Int, studentNumber: String) {
        val pdfFile = FileUtil.getSubmissionFile(assessmentID,studentNumber)
        val progressDialog = ProgressDialog(context)
        progressDialog.setMessage(context.getString(R.string.uploading_pdf))
        progressDialog.setCancelable(false)
        progressDialog.show()
        val mediaType = MediaType.parse("application/pdf")
        val requestFile = RequestBody.create(mediaType, pdfFile)
        val body = MultipartBody.Part.createFormData("pdfFile", pdfFile.name, requestFile)
        api.uploadSubmissionPDF(submissionID, assessmentID, body).enqueue(object : Callback<SingleResponse> {
            override fun onResponse(call: Call<SingleResponse>, response: Response<SingleResponse>) {
                progressDialog.dismiss()
                if (response.isSuccessful) {
                    Toast.makeText(context, R.string.upload_succes, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, R.string.upload_fail, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<SingleResponse>, t: Throwable) {
                progressDialog.dismiss()
                t.printStackTrace()
                Toast.makeText(context, R.string.server_connect_fail, Toast.LENGTH_SHORT).show()
            }
        })
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
    fun downloadSubmissionPDF(context: Context, submissionID: Int, submissionFolderName: String, folderName: String, callback: (String?) -> Unit) {
        val progressDialog = ProgressDialog(context)
        progressDialog.setMessage(context.getString(R.string.download_pdf))
        progressDialog.setCancelable(false) //to ensure that the user cannot cancel the download
        progressDialog.show()
        api.getSubmissionPDF(submissionID).enqueue(object : Callback<PDFResponse> {
            override fun onResponse(call: Call<PDFResponse>, response: Response<PDFResponse>) {
                progressDialog.dismiss()
                if (response.isSuccessful) {
                    val resp = response.body()
                    if (resp != null) {
                        val byteArray = resp.pdfData.data.map { it.toByte() }.toByteArray()
                        val documentsDir = java.io.File(
                            Environment.getExternalStoragePublicDirectory(
                                Environment.DIRECTORY_DOCUMENTS
                            ), folderName
                        )
                        if (!documentsDir.exists()) {
                            documentsDir.mkdirs()
                        }
                        val path = FileUtil.saveSubmissionPDF(context,byteArray, submissionFolderName, documentsDir)
                        callback(path) //if path is not null, the callback is called with the path
                    } else {
                        callback(null) //if the response is null, the callback is called with null
                    }
                } else {
                    callback(null) //if the response is not successful, the callback is called with null
                }
            }

            override fun onFailure(call: Call<PDFResponse>, t: Throwable) {
                progressDialog.dismiss()
                t.printStackTrace()
                callback(null) //if the request fails, the callback is called with null
            }
        })
    }

    /**
     * This function is used to download the memo PDF from the server.
     * @param context The context of the activity or fragment that is calling the function (usually @SubmissionsActivity).
     * @param assessmentID The ID of the assessment that the memo belongs to.
     * @param folderName The name of the folder that the PDF will be saved to.
     * @param callback The function that is called after the PDF is downloaded. The path of the PDF is passed to the function.
     * If the PDF is successfully downloaded, the path of the PDF is passed to the callback function.
     * If the PDF is not successfully downloaded, null is passed to the callback function.
     * Callbacks are used to handle the asynchronous nature of the function.
     * The function is similar to downloadSubmissionPDF, but is used to download the memo PDF instead of the submission PDF.
     */
    fun downloadMemoPDF(context: Context,assessmentID: Int, folderName: String, callback: (String?) -> Unit) {
        val progressDialog = ProgressDialog(context)
        progressDialog.setMessage(context.getString(R.string.download_pdf))
        progressDialog.setCancelable(false) //to ensure that the user cannot cancel the download
        progressDialog.show()
        api.getMemoPDF(assessmentID).enqueue(object : Callback<PDFResponse> {
            override fun onResponse(call: Call<PDFResponse>, response: Response<PDFResponse>) {
                progressDialog.dismiss()
                if (response.isSuccessful) {
                    val resp = response.body()
                    if (resp != null) {
                        val byteArray = resp.pdfData.data.map { it.toByte() }.toByteArray()
                        val documentsDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), folderName)
                        if (!documentsDir.exists()) {
                            documentsDir.mkdirs()
                        }
                        val path = FileUtil.saveMemoPDF(context,byteArray, assessmentID, documentsDir)
                        callback(path) //if path is not null, the callback is called with the path
                    } else {
                        callback(null) //if the response is null, the callback is called with null
                    }
                } else {
                    callback(null) //if the response is not successful, the callback is called with null
                }
            }
            override fun onFailure(call: Call<PDFResponse>, t: Throwable) {
                progressDialog.dismiss()
                t.printStackTrace()
                callback(null) //if the request fails, the callback is called with null
            }
        })
    }
}