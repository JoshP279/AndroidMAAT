package com.radaee.activities

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SearchView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.radaee.adapters.SubmissionsAdapter
import com.radaee.comm.Global
import com.radaee.dataclasses.PDFResponse
import com.radaee.dataclasses.SubmissionsResponse
import com.radaee.decorators.EqualSpaceItemDecoration
import com.radaee.objects.FileUtil
import com.radaee.objects.RetrofitClient
import com.radaee.pdf.Document
import com.radaee.pdfmaster.R
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class SubmissionsActivity : AppCompatActivity(), SubmissionsAdapter.SubmissionUpdateListener{
    private lateinit var submissionsAssessmentNameTextView: TextView
    private lateinit var submissionsRecyclerView: RecyclerView
    private lateinit var submissionsSearchView: SearchView
    private lateinit var adapter: SubmissionsAdapter
    private var assessmentID: Int = 0
    private  var submissions = ArrayList<SubmissionsResponse>()
    private  var filteredSubmissions = ArrayList<SubmissionsResponse>()
    lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var submissionsHelper: TextView
    private lateinit var filterSpinner: Spinner
    private var submissionPDF: Document? = null
    private var memoPDF: Document? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Global.Init(this)
        setContentView(R.layout.activity_submissions)
        submissionsAssessmentNameTextView = findViewById(R.id.submissionsAssessmentNameTextView)
        val intent = intent
        submissionsAssessmentNameTextView.text = intent?.getStringExtra("assessmentName")
        assessmentID = intent.getIntExtra("assessmentID",1)
        submissionsSearchView = findViewById(R.id.submissionsSearchView)
        submissionsRecyclerView = findViewById(R.id.submissionsRecyclerView)
        swipeRefreshLayout = findViewById(R.id.submissionsSwipeRefresh)
        filterSpinner = findViewById(R.id.filterSpinner)
        swipeRefreshLayout.setOnRefreshListener {
            RetrofitClient.loadSubmissions(this,assessmentID, submissions, filteredSubmissions, submissionsRecyclerView)
            swipeRefreshLayout.isRefreshing = false
        }
        submissionsHelper = findViewById(R.id.submissionsHelper)
        submissionsHelper.setOnClickListener{
            displayHelperDialog()
        }
        setUpRecyclerView()
        setUpSearchView()
        setUpFilterSpinner()
    }

    private fun setUpFilterSpinner() {
        val statuses = arrayOf(
            getString(R.string.all),
            getString(R.string.marked),
            getString(R.string.in_progress),
            getString(R.string.unmarked)
        )
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, statuses)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        filterSpinner.adapter = adapter
        filterSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                filterOptions()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun filterOptions() {
        val selectedFilter = filterSpinner.selectedItem.toString()
        filteredSubmissions.clear()
        when (selectedFilter) {
            getString(R.string.marked) -> {
                filteredSubmissions.addAll(submissions.filter { it.submissionStatus == getString(R.string.marked)})
            }
            getString(R.string.in_progress) -> {
                filteredSubmissions.addAll(submissions.filter { it.submissionStatus == getString(R.string.in_progress)})
            }
            getString(R.string.unmarked) -> {
                filteredSubmissions.addAll(submissions.filter { it.submissionStatus == getString(R.string.unmarked)})
            }
            else -> {
                RetrofitClient.loadSubmissions(this,assessmentID, submissions, filteredSubmissions, submissionsRecyclerView)
            }
        }
        adapter.notifyDataSetChanged()
    }

    private fun displayHelperDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.helperHeading)
        builder.setMessage(R.string.submissionsHelperMessage)
        builder.setPositiveButton(R.string.ok) { dialog, _ -> dialog.dismiss() }
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return false
    }

    private fun setUpRecyclerView() {
        submissionsRecyclerView.layoutManager = LinearLayoutManager(applicationContext)
        RetrofitClient.loadSubmissions(this,assessmentID, submissions, filteredSubmissions, submissionsRecyclerView)
        adapter = SubmissionsAdapter(filteredSubmissions, this)
        adapter.setSubmissionUpdateListener(this)
        submissionsRecyclerView.adapter = adapter
        submissionsRecyclerView.addItemDecoration(EqualSpaceItemDecoration(10))
        (submissionsRecyclerView.adapter as SubmissionsAdapter).setOnItemClickListener(submissionOnClickListener)
    }

    private val submissionOnClickListener = SubmissionsAdapter.OnItemClickListener { position ->
        val submission = filteredSubmissions[position]
//        HomeActivity.recentSubmissions.add(submission)
        val folderName = "Assessment_${assessmentID}"
        val submissionFile = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), folderName)
        val memoFile = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), folderName)
        val submissionName = "submission_${submission.studentNumber}.pdf"
        val memoName = "memo_${assessmentID}.pdf"
        val sFile = File(submissionFile, submissionName)
        val mFile = File(memoFile, memoName)
        if (FileUtil.checkSubmissionExists(submissionFile, submission.studentNumber) && FileUtil.checkMemoExists(memoFile,assessmentID)) {
            initPDFReaderIntent(sFile.path,mFile.path, submission.studentNumber, submission.submissionID)
        }else if (!FileUtil.checkSubmissionExists(submissionFile, submission.studentNumber) && FileUtil.checkMemoExists(memoFile,assessmentID)) {
            RetrofitClient.downloadSubmissionPDF(this@SubmissionsActivity,submission.submissionID, submission.studentNumber, folderName) { path ->
                if (path != null) {
                    initPDFReaderIntent(path, mFile.path, submission.studentNumber, submission.submissionID)
                } else {
                    Toast.makeText(applicationContext, R.string.pdf_fail_download, Toast.LENGTH_SHORT).show()
                }
            }
        }else if (FileUtil.checkSubmissionExists(submissionFile, submission.studentNumber) && !FileUtil.checkMemoExists(memoFile,assessmentID)){
            RetrofitClient.downloadMemoPDF(this@SubmissionsActivity,assessmentID, folderName) {path ->
                if (path != null) {
                    initPDFReaderIntent(sFile.path, path, submission.studentNumber, submission.submissionID)
                } else {
                    Toast.makeText(applicationContext, R.string.pdf_fail_download, Toast.LENGTH_SHORT).show()
                }
            }
        }else{
            RetrofitClient.downloadSubmissionPDF(this@SubmissionsActivity,submission.submissionID, submission.studentNumber, folderName) { sPath ->
                RetrofitClient.downloadMemoPDF(this@SubmissionsActivity,assessmentID, folderName) {mPath ->
                    if (sPath != null && mPath != null) {
                        initPDFReaderIntent(sPath, mPath, submission.studentNumber, submission.submissionID)
                    } else {
                        Toast.makeText(applicationContext, R.string.pdf_fail_download, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun initPDFReaderIntent(sPath:String,mPath: String,studentNumber: String, submissionID: Int){
        submissionPDF = Document()
        memoPDF = Document()
        val err1: Int = submissionPDF!!.Open(sPath, null)
        val err2: Int = memoPDF!!.Open(mPath, null)
        if (err1 == 0 && err2 == 0){
            PDFReaderActivity.submission = submissionPDF
            PDFReaderActivity.memo = memoPDF
            val intent = Intent(applicationContext, PDFReaderActivity::class.java)
            intent.putExtra("studentNum", studentNumber)
            intent.putExtra("submissionID", submissionID)
            intent.putExtra("assessmentID", assessmentID)
            startActivity(intent)
        }else {
            Toast.makeText(applicationContext, R.string.pdf_fail_open, Toast.LENGTH_SHORT).show()
        }
    }


    private fun setUpSearchView(){
        submissionsSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextChange(newText: String?): Boolean {
                searchSubmissions(newText)
                return true
            }

            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }
        })
    }

    private fun searchSubmissions(query: String?){
        val filteredList = if (!query.isNullOrEmpty()){
            submissions.filter{
                it.studentNumber.contains(query,ignoreCase = true) ||
                        it.studentName.contains(query, ignoreCase = true) ||
                        it.studentSurname.contains(query,ignoreCase = true)
            }
        }else{
            submissions
        }
        filteredSubmissions.clear()
        filteredSubmissions.addAll(filteredList)
        adapter.notifyDataSetChanged()
    }

    override fun onDestroy() {
        submissionPDF?.let {
            it.Close()
            Global.RemoveTmp()
        }
        super.onDestroy()
    }

    override fun onSubmissionUpdated() {
        Handler(Looper.getMainLooper()).postDelayed({
            RetrofitClient.loadSubmissions(this, assessmentID, submissions, filteredSubmissions, submissionsRecyclerView)
            swipeRefreshLayout.isRefreshing = false
        }, 1000)
    }
}