package com.radaee.activities

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SearchView
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.radaee.adapters.SubmissionsAdapter
import com.radaee.comm.Global
import com.radaee.dataclasses.SubmissionsResponse
import com.radaee.decorators.EqualSpaceItemDecoration
import com.radaee.interfaces.HelpHandler
import com.radaee.objects.FileUtil
import com.radaee.objects.RetrofitClient
import com.radaee.objects.SharedPref
import com.radaee.objects.SnackbarUtil
import com.radaee.pdf.Document
import com.radaee.pdfmaster.R
import java.io.File

/**
 * This activity displays the submissions for a particular assessment.
 * When a submission is clicked, the user is taken to the PDFReaderActivity where they can view the submission and memo PDFs.
 */
class SubmissionsActivity : AppCompatActivity(), HelpHandler{
    /**
     * The companion object contains the filtered submissions and the current position of the submission clicked.
     * This is used to ensure that the correct submission is opened in the PDFReaderActivity.
     */
    private lateinit var submissionsAssessmentNameTextView: TextView
    private lateinit var submissionsRecyclerView: RecyclerView
    private lateinit var submissionsSearchView: SearchView
    private lateinit var adapter: SubmissionsAdapter
    private var assessmentID: Int = 0
    private var totalMarks: Int = 0
    private var assessmentName = ""
    private var moduleCode = ""
    private var markingStyle = ""
    private  var submissions = ArrayList<SubmissionsResponse>()
    private var filteredSubmissions = ArrayList<SubmissionsResponse>()
    lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var filterSpinner: Spinner
    private var submissionPDF: Document? = null
    private var memoPDF: Document? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /// Initialize the Global class for the RadaeePDFSDK. This is required to use the SDK.
        Global.Init(this)
        setContentView(R.layout.activity_submissions)
        submissionsSearchView = findViewById(R.id.submissionsSearchView)
        submissionsRecyclerView = findViewById(R.id.submissionsRecyclerView)
        swipeRefreshLayout = findViewById(R.id.submissionsSwipeRefresh)
        filterSpinner = findViewById(R.id.filterSpinner)
        submissionsAssessmentNameTextView = findViewById(R.id.submissionsAssessmentNameTextView)
        //Obtaining information from previous activity
        val intent = intent
        assessmentName = intent.getStringExtra("assessmentName").toString()
        submissionsAssessmentNameTextView.text = assessmentName
        assessmentID = intent.getIntExtra("assessmentID",1)
        totalMarks = intent.getIntExtra("totalMarks", 1)
        moduleCode = intent.getStringExtra("moduleCode").toString()
        markingStyle = SharedPref.getString(this@SubmissionsActivity, "marking_style", getString(R.string.marking_style1)).toString()
        swipeRefreshLayout.setOnRefreshListener {
            RetrofitClient.loadSubmissions(this,findViewById(android.R.id.content), assessmentID, submissions, filteredSubmissions, submissionsRecyclerView)
            swipeRefreshLayout.isRefreshing = false
        }
        setUpRecyclerView()
        setUpSearchView()
        setUpFilterSpinner()
    }

    /**
     * This function sets up the filter spinner for the submissions.
     * The spinner has four options: All, Marked, In Progress, Unmarked. Each of these will filter the submissions accordingly.
     */
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

    /**
     * This function filters the submissions based on the selected option in the filter spinner. Essentially the logic behind the options is as follows:
     */
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
                if (SharedPref.getBoolean(this, "OFFLINE_MODE", false)) {
                    loadOfflineSubmissions()
                }
                else{
                    RetrofitClient.loadSubmissions(this,findViewById(android.R.id.content),assessmentID, submissions, filteredSubmissions, submissionsRecyclerView)
                }
            }
        }
        adapter.notifyDataSetChanged()
    }

    /**
     * This function displays a dialog box with a message to help the user understand the purpose of the submissions activity.
     */
    override fun displayHelperDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.helperHeading)
        builder.setMessage(R.string.submissionsHelperMessage)
        builder.setPositiveButton(R.string.ok) { dialog, _ -> dialog.dismiss() }
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_help -> {
                displayHelperDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_help, menu)
        return true
    }

    /**
     * This function sets up the recycler view for the submissions.
     * It uses the SubmissionsAdapter to display the submissions in a list.
     * Note that the RetrofitClient is used to load the submissions from the server.
     */
    private fun setUpRecyclerView() {
        submissionsRecyclerView.layoutManager = LinearLayoutManager(applicationContext)
        adapter = SubmissionsAdapter(filteredSubmissions, this, findViewById(android.R.id.content), totalMarks, markingStyle)
        submissionsRecyclerView.adapter = adapter
        if (SharedPref.getBoolean(this, "OFFLINE_MODE", true)) {
            loadOfflineSubmissions()
        }

        else{
            RetrofitClient.loadSubmissions(this,findViewById(android.R.id.content), assessmentID, submissions, filteredSubmissions, submissionsRecyclerView)
        }

        submissionsRecyclerView.addItemDecoration(EqualSpaceItemDecoration(10))
        (submissionsRecyclerView.adapter as SubmissionsAdapter).setOnItemClickListener(submissionOnClickListener)
    }

    private fun loadOfflineSubmissions() {
        val folderName = "${assessmentID}_${moduleCode}_${assessmentName}"
        val submissionsDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), folderName)

        if (submissionsDir.exists() && submissionsDir.isDirectory) {
            val submissionFiles = submissionsDir.listFiles()
            submissions.clear()
            filteredSubmissions.clear()

            submissionFiles?.forEach { file ->
                if (!file.name.startsWith("memo_")) {
                    val fileName = file.name
                    val studentInfo = fileName.split("_")
                    val studentNumber = studentInfo[1].substringBefore("-").removePrefix("s")
                    val names = studentInfo[1].substringAfter("-").split(" ")
                    val studentName = names[0]
                    val studentSurname = names.drop(1).joinToString(" ")

                    val submission = SubmissionsResponse(
                        submissionID = studentInfo[0].toInt(),
                        assessmentID = assessmentID,
                        studentNumber = studentNumber,
                        submissionMark = 0,
                        studentName = studentName,
                        studentSurname = studentSurname,
                        submissionStatus = getString(R.string.unmarked),
                        submissionFolderName = fileName.substringAfter("_").removeSuffix(".pdf"),
                    )
                    submissions.add(submission)
                }
            }
            filteredSubmissions.addAll(submissions)
            adapter.notifyDataSetChanged()

        } else {
            SnackbarUtil.showErrorSnackBar(findViewById(android.R.id.content), getString(R.string.no_offline_submissions), this)
        }
    }

    /**
     * This is the click listener for the submissions. There are 4 possibilities when clicking on a submission:
     * 1. The submission PDF exists and the memo PDF exists. No downloads are necessary, and proceed to the PDFReaderActivity.
     * 2. The submission PDF does not exist but the memo PDF exists. Download the submission PDF and proceed to the PDFReaderActivity.
     * 3. The submission PDF exists but the memo PDF does not exist. Download the memo PDF and proceed to the PDFReaderActivity.
     * 4. Neither the submission PDF nor the memo PDF exists. Download both PDFs and proceed to the PDFReaderActivity.
     */
    private val submissionOnClickListener = SubmissionsAdapter.OnItemClickListener { position ->
        val submission = filteredSubmissions[position]
        val folderName = assessmentID.toString() + "_" + moduleCode + "_" + assessmentName
        val submissionFile = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), folderName)
        var submissionFileName = submission.submissionID.toString() + "_" + submission.submissionFolderName
        val memoFile = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), folderName)
        val memoName = "memo_${assessmentID}.pdf"
        if (!submissionFile.name.contains(".pdf")) {
            submissionFileName = "$submissionFileName.pdf"
            Log.e("SubmissionsActivity", submissionFileName)
        }
        val sFile: File = File(submissionFile, submissionFileName)
        val mFile = File(memoFile, memoName)
        if (FileUtil.checkSubmissionExists(submissionFile, submissionFileName, submission.studentNumber) && FileUtil.checkMemoExists(memoFile,assessmentID)) {
            Log.e("SubmissionsActivity0", sFile.path)
            initPDFReaderIntent(sFile.path,mFile.path, submission.studentNumber, submission.submissionID, position)
        }else if (!FileUtil.checkSubmissionExists(submissionFile, submissionFileName,submission.studentNumber) && FileUtil.checkMemoExists(memoFile,assessmentID)) {
            RetrofitClient.downloadSubmissionPDF(this@SubmissionsActivity,findViewById(android.R.id.content), submission.submissionID, submissionFileName, folderName, true) { path ->
                if (path != null) {
                    Log.e("SubmissionsActivity1", path)
                    initPDFReaderIntent(path, mFile.path, submission.studentNumber, submission.submissionID, position)
                } else {
                    SnackbarUtil.showErrorSnackBar(findViewById(android.R.id.content), getString(R.string.pdf_fail_download), this)
                }
            }
        }else if (FileUtil.checkSubmissionExists(submissionFile, submissionFileName,  submission.studentNumber) && !FileUtil.checkMemoExists(memoFile,assessmentID)){
            RetrofitClient.downloadMemoPDF(this@SubmissionsActivity,findViewById(android.R.id.content), assessmentID, folderName, true) {path ->
                if (path != null) {
                    Log.e("SubmissionsActivity2", path)
                    initPDFReaderIntent(sFile.path, path, submission.studentNumber, submission.submissionID, position)
                } else {
                    SnackbarUtil.showErrorSnackBar(findViewById(android.R.id.content), getString(R.string.pdf_fail_download), this)
                }
            }
        }else{
            RetrofitClient.downloadSubmissionPDF(this@SubmissionsActivity,findViewById(android.R.id.content), submission.submissionID, submissionFileName, folderName, true) { sPath ->
                RetrofitClient.downloadMemoPDF(this@SubmissionsActivity,findViewById(android.R.id.content), assessmentID, folderName, true) {mPath ->
                    if (sPath != null && mPath != null){
                        Log.e("SubmissionsActivity3", sPath)
                        initPDFReaderIntent(sPath, mPath, submission.studentNumber, submission.submissionID, position)
                    } else {
                        SnackbarUtil.showErrorSnackBar(findViewById(android.R.id.content), getString(R.string.pdf_fail_download), this)
                    }
                }
            }
        }
    }

    /**
     * This function initializes the PDFReaderActivity with the submission and memo PDFs. Attaches both PDFs to the intent, as well studentNum, submissionID, and assessmentID.
     */
    private fun initPDFReaderIntent(sPath:String,mPath: String,studentNumber: String, submissionID: Int, position: Int){
        //Note that these Document objects are custom objects, used to open PDFs with the RadaeePDFSDK.
        submissionPDF = Document()
        memoPDF = Document()
        val err1: Int = submissionPDF!!.Open(sPath, null)
        val err2: Int = memoPDF!!.Open(mPath, null)
        if (err1 == 0 && err2 == 0){
            PDFReaderActivity.submission = submissionPDF
            PDFReaderActivity.memo = memoPDF
            PDFReaderActivity.filteredSubmissions = filteredSubmissions
            PDFReaderActivity.currentPos = position
            val intent = Intent(applicationContext, PDFReaderActivity::class.java)
            intent.putExtra("studentNum", studentNumber)
            intent.putExtra("submissionID", submissionID)
            intent.putExtra("assessmentID", assessmentID)
            intent.putExtra("assessmentName", assessmentName)
            intent.putExtra("moduleCode", moduleCode)
            intent.putExtra("submissionFolderName", filteredSubmissions[position].submissionFolderName)
            intent.putExtra("totalMarks", totalMarks)
            startActivity(intent)
        }else {
            SnackbarUtil.showErrorSnackBar(findViewById(android.R.id.content), getString(R.string.pdf_fail_open), this)
        }
    }

    /**
     * This function sets up the search view for the submissions.
     */
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

    /**
     * This function searches the submissions based on the query entered by the user. Essentially, the search is case-insensitive and searches the student number, name, and surname.
     */
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

    /**
     * This function is called when the activity is destroyed.
     * It closes the submission PDF and removes the temporary files.
     */
    override fun onDestroy() {
        submissionPDF?.let { it.Close() }
        memoPDF?.let{ it.Close() }
        Global.RemoveTmp()
        super.onDestroy()
    }
}