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

object RetrofitClient {
    private const val BASE_URL = "http://10.0.0.110:3306/"
    val api: API by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(API::class.java)
    }

    fun attemptLogin(context: Context, email:String, password:String) {
        api.login(email, password).enqueue(object : Callback<SingleResponse> {
            override fun onResponse(
                call: Call<SingleResponse>,
                response: Response<SingleResponse>
            ) {
                if (response.isSuccessful) {
                    val resp = response.body()
                    if (resp?.message.equals("Login successful")) {
                        SharedPref.saveString(context,"email", email)
                        SharedPref.saveString(context,"password", password)
                        val intent = Intent(context, MainActivity::class.java)
                        context.startActivity(intent)
                        (context as LogInActivity).finish()
                    }
                } else {
                    Toast.makeText(context, "Invalid username or password", Toast.LENGTH_SHORT)
                        .show()
                }
            }

            override fun onFailure(call: Call<SingleResponse>, t: Throwable) {
                t.printStackTrace()
                Toast.makeText(context, "Failed to connect to server", Toast.LENGTH_SHORT).show()
            }
        })
    }

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

    fun downloadSubmissionPDF(context: Context, submissionID: Int, studentNumber: String, folderName: String, callback: (String?) -> Unit) {
        val progressDialog = ProgressDialog(context)
        progressDialog.setMessage(context.getString(R.string.download_pdf))
        progressDialog.setCancelable(false)
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
                        val path = FileUtil.saveSubmissionPDF(context,byteArray, studentNumber, documentsDir)
                        callback(path)
                    } else {
                        callback(null)
                    }
                } else {
                    callback(null)
                }
            }

            override fun onFailure(call: Call<PDFResponse>, t: Throwable) {
                progressDialog.dismiss()
                t.printStackTrace()
                callback(null)
            }
        })
    }

    fun downloadMemoPDF(context: Context,assessmentID: Int, folderName: String, callback: (String?) -> Unit) {
        val progressDialog = ProgressDialog(context)
        progressDialog.setMessage(context.getString(R.string.download_pdf))
        progressDialog.setCancelable(false)
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
                        callback(path)
                    } else {
                        callback(null)
                    }
                } else {
                    callback(null)
                }
            }
            override fun onFailure(call: Call<PDFResponse>, t: Throwable) {
                progressDialog.dismiss()
                t.printStackTrace()
                callback(null)
            }
        })
    }
}