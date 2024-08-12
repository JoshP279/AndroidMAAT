package com.radaee.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.radaee.dataclasses.AssessmentResponse
import com.radaee.objects.SharedPref
import com.radaee.pdfmaster.R

/**
 * Adapter for the recycler view that displays the assessments
 * @param mList list of assessments
 * @param context context of the activity, which is @ViewAssessmentsFragment
 * This adapter just binds the necessary data to the assessments card view
 */
class AssessmentsAdapter(private val mList: List<AssessmentResponse>, private val context: Context) : RecyclerView.Adapter<AssessmentsAdapter.ViewHolder>() {
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
        if (SharedPref.getBoolean(context, "OFFLINE_MODE", false)) {
            holder.assessmentTotalSubmissionsTextView.visibility = View.INVISIBLE
            holder.assessmentNumMarkedTextView.visibility = View.INVISIBLE
        }
        holder.bind(item)
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