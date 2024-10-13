package com.radaee.adapters

import android.app.ProgressDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.radaee.dataclasses.AssessmentResponse
import com.radaee.dataclasses.SubmissionsResponse
import com.radaee.objects.RetrofitClient
import com.radaee.objects.SharedPref
import com.radaee.objects.SnackbarUtil
import com.radaee.pdfmaster.R

/**
 * Adapter for the recycler view that displays the assessments
 * @param mList list of assessments
 * @param context context of the activity, which is @ViewAssessmentsFragment
 * This adapter just binds the necessary data to the assessments card view
 */
class AssessmentsAdapter(private val mList: List<AssessmentResponse>, private val context: Context, private val rootView: View) : RecyclerView.Adapter<AssessmentsAdapter.ViewHolder>() {
    private var listener: OnItemClickListener? = null
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.cardview_assessment, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mList[position]
        holder.moduleCodeTextView.text = item.moduleCode
        holder.assessmentNameTextView.text = item.assessmentName
        holder.assessmentTotalSubmissionsTextView.text = String.format(context.getString(R.string.submissions_marked), item.totalSubmissions)
        holder.assessmentNumMarkedTextView.text =  String.format(item.numMarked.toString() + "/")
        holder.overflowMenu.setOnClickListener { showPopupMenu(holder.overflowMenu, position)}

        if (SharedPref.getBoolean(context, "OFFLINE_MODE", false)) {
            holder.assessmentTotalSubmissionsTextView.visibility = View.INVISIBLE
            holder.assessmentNumMarkedTextView.visibility = View.INVISIBLE
            holder.overflowMenu.visibility = View.INVISIBLE
        }else{
            holder.assessmentTotalSubmissionsTextView.visibility = View.VISIBLE
            holder.assessmentNumMarkedTextView.visibility = View.VISIBLE
            holder.overflowMenu.visibility = View.VISIBLE
        }
        holder.bind(item)
    }

    private fun showPopupMenu(view: View, position: Int) {
        val popup = PopupMenu(context, view)
        val inflater: MenuInflater = popup.menuInflater
        inflater.inflate(R.menu.assessments_overflow_menu, popup.menu)
        popup.setOnMenuItemClickListener(MyMenuItemClickListener(position))
        popup.show()
    }

    /**
     * This handles the click events for the pop up menu
     * @param position position of the card view
     */
    inner class MyMenuItemClickListener(position: Int) : PopupMenu.OnMenuItemClickListener {
        private val cur = mList[position]

        override fun onMenuItemClick(item: MenuItem): Boolean {
            return when (item.itemId) {
                R.id.action_download_all -> {
                    val submissions: MutableList<SubmissionsResponse> = mutableListOf()
                    val progressDialog = ProgressDialog(context)
                    progressDialog.setMessage(context.getString(R.string.downloading_submissions))
                    progressDialog.setCancelable(false)
                    progressDialog.show()

                    RetrofitClient.getSubmissions(context,rootView, cur.assessmentID, submissions) {
                        val folderName =
                            cur.assessmentID.toString() + "_" + cur.moduleCode + "_" + cur.assessmentName
                        var remainingDownloads = submissions.size + 1 // +1 for the memo
                        RetrofitClient.downloadMemoPDF(context, rootView, cur.assessmentID, folderName, false) { path ->
                            if (path == null) {
                                SnackbarUtil.showErrorSnackBar(
                                    rootView,
                                    context.getString(R.string.pdf_fail_download),
                                    context
                                )
                            }
                            remainingDownloads--

                            if (remainingDownloads == 0) {
                                progressDialog.dismiss()
                            }
                        }
                        for (submission in submissions) {
                            val submissionFileName =
                                submission.submissionID.toString() + "_" + submission.submissionFolderName
                            RetrofitClient.downloadSubmissionPDF(
                                context,
                                rootView,
                                submission.submissionID,
                                submissionFileName,
                                folderName,
                                false
                            ) { path ->
                                if (path == null) {
                                    SnackbarUtil.showErrorSnackBar(
                                        rootView,
                                        context.getString(R.string.pdf_fail_download),
                                        context
                                    )
                                }
                                remainingDownloads--
                                if (remainingDownloads == 0) {
                                    progressDialog.dismiss()
                                    SnackbarUtil.showSuccessSnackBar(
                                        rootView,
                                        context.getString(R.string.pdf_success_download),
                                        context
                                    )
                                }
                            }
                        }
                        if (remainingDownloads == 0) {
                            progressDialog.dismiss()
                        }
                    }
                    true
                }

                else -> false
            }
        }
    }
        override fun getItemCount() = mList.size

    /**
     * View holder for the recycler view
     * @param itemView view of the card view
     * This class just binds the text views in the card view
     */
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val moduleCodeTextView: TextView = itemView.findViewById(R.id.assessmentModuleCodeTextView)
        val assessmentNameTextView: TextView = itemView.findViewById(R.id.assessmentNameTextView)
        val assessmentNumMarkedTextView: TextView = itemView.findViewById(R.id.assessmentNumMarkedTextView)
        val assessmentTotalSubmissionsTextView: TextView = itemView.findViewById(R.id.assessmentsTotalScriptsTextView)
        val overflowMenu: ImageView = itemView.findViewById(R.id.assessment_overflow_menu)
        fun bind(assessments: AssessmentResponse) {
            itemView.setOnClickListener {
                listener?.onItemClick(adapterPosition)
            }
        }
    }
    /**
     * Interface for the listener that listens for item clicks
     */
    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }
}