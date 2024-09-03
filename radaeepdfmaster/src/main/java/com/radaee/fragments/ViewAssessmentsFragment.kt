package com.radaee.fragments

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.radaee.activities.SubmissionsActivity
import com.radaee.adapters.AssessmentsAdapter
import com.radaee.dataclasses.AssessmentResponse
import com.radaee.decorators.EqualSpaceItemDecoration
import com.radaee.objects.RetrofitClient
import com.radaee.objects.SharedPref
import com.radaee.pdfmaster.R
import java.io.File

/**
 * ViewAssessmentsFragment is a fragment that displays a list of assessments that the marker is assigned to.
 */
class ViewAssessmentsFragment : Fragment() {
    private lateinit var assessmentsList: RecyclerView
    private lateinit var adapter: AssessmentsAdapter
    private lateinit var assessmentHelper: TextView
    private var assessments = ArrayList<AssessmentResponse>()
    private var filteredAssessments = ArrayList<AssessmentResponse>()
    private lateinit var searchView: SearchView
    lateinit var swipeRefreshLayout: SwipeRefreshLayout
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_view_assessments, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        assessmentsList = view.findViewById(R.id.assessmentsRecyclerView)
        searchView = view.findViewById(R.id.assessmentSearchView)
        assessmentHelper = view.findViewById(R.id.assessmentsHelper)
        swipeRefreshLayout = view.findViewById(R.id.assessmentsSwipeRefresh)

        assessmentHelper.setOnClickListener {
            displayHelperDialog()
        }

        swipeRefreshLayout.setOnRefreshListener {
            RetrofitClient.loadAssessments(requireContext(),assessments,filteredAssessments,assessmentsList)
            swipeRefreshLayout.isRefreshing = false
        }
        setUpRecyclerView()
        setUpSearchView()
    }

    /**
     * Displays a dialog that provides information on how to use the assessments fragment.
     */
    private fun displayHelperDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(R.string.helperHeading)
        builder.setMessage(R.string.assessmentsHelperMessage)
        builder.setPositiveButton(R.string.ok) { dialog, _ -> dialog.dismiss() }
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    /**
     * Sets up the recycler view that displays the list of assessments.
     * The recycler view is populated with the assessments that the marker is assigned to.
     */
    private fun setUpRecyclerView() {
        assessmentsList.layoutManager = GridLayoutManager(requireContext(), 2)
        adapter = AssessmentsAdapter(filteredAssessments, requireContext())
        assessmentsList.adapter = adapter
        if (SharedPref.getBoolean(requireContext(), "OFFLINE_MODE", false)) {
            loadOfflineAssessments()
        }
        else{
            RetrofitClient.loadAssessments(requireContext(),assessments,filteredAssessments,assessmentsList)
        }
        assessmentsList.addItemDecoration(EqualSpaceItemDecoration(10))
        (assessmentsList.adapter as AssessmentsAdapter).setOnItemClickListener(object :
            AssessmentsAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                val assessment = filteredAssessments[position]
                val intent = Intent(requireContext(), SubmissionsActivity::class.java)
                intent.putExtra("assessmentName",assessment.assessmentName)
                intent.putExtra("assessmentID", assessment.assessmentID)
                intent.putExtra("moduleCode", assessment.moduleCode)
                intent.putExtra("totalMarks", assessment.totalMarks)
                startActivity(intent)
            }
        })
    }

    /**
     * Loads the assessments that are stored offline on the device.
     * Only called if the user is in offline mode.
     */
    private fun loadOfflineAssessments(){
        val documentsDirectory = File(Environment.getExternalStorageDirectory(), "Documents")
        if (documentsDirectory.exists() && documentsDirectory.isDirectory) {
            // List all files in the directory
            val files = documentsDirectory.listFiles()
            val assessmentDirectories = files?.filter { it.isDirectory } ?: emptyList()
            assessments.clear()

            filteredAssessments.clear()
            // For each directory, create an assessment object and add it to the list
            // These values are incorrect, but they enable offline functionality
            for (directory in assessmentDirectories) {
                val assessmentName = directory.name
                val elem = assessmentName.split("_")
                val assessment = AssessmentResponse(
                    assessmentName = elem[2],
                    assessmentID = elem[0].toInt(),
                    moduleCode = elem[1],
                    totalSubmissions = 0,
                    numMarked = 0,
                    totalMarks = 0
                )
                assessments.add(assessment)
            }
            filteredAssessments.addAll(assessments)
            adapter.notifyDataSetChanged()
        }
    }
    /**
     * Sets up the search view that allows the user to search for assessments by module code or assessment name.
     */
    private fun setUpSearchView(){
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextChange(newText: String?): Boolean {
                filterAssessments(newText)
                return true
            }
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }
        })
    }

    /**
     * Filters the list of assessments based on the user's search query.
     * Essentially handles all necessary logic for filtering the assessments.
     */
    private fun filterAssessments(query: String?){
        val filteredList = if (!query.isNullOrEmpty()){
            assessments.filter{
                it.moduleCode.contains(query,ignoreCase = true) ||
                it.assessmentName.contains(query, ignoreCase = true)
            }
        }else{
            assessments
        }
        filteredAssessments.clear()
        filteredAssessments.addAll(filteredList)
        adapter.notifyDataSetChanged()
    }
}