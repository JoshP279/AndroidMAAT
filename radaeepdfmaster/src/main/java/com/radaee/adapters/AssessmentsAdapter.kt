package com.radaee.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.radaee.dataclasses.AssessmentResponse
import com.radaee.pdfmaster.R

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
        holder.bind(item)
    }
    override fun getItemCount() = mList.size
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
    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }
}