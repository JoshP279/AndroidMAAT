package com.radaee.adapters

import android.content.Context
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.radaee.activities.SubmissionsActivity
import com.radaee.dataclasses.SubmissionsResponse
import com.radaee.objects.RetrofitClient
import com.radaee.pdfmaster.R

class SubmissionsAdapter(private val mList: List<SubmissionsResponse>, private val context: Context) : RecyclerView.Adapter<SubmissionsAdapter.ViewHolder>()  {
    private var listener: OnItemClickListener? = null
    private var submissionUpdateListener: SubmissionUpdateListener? = null
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.cardview_submission, parent, false)
        return ViewHolder(view)
    }
    interface SubmissionUpdateListener {
        fun onSubmissionUpdated()
    }
    fun setSubmissionUpdateListener(listener: SubmissionUpdateListener) {
        this.submissionUpdateListener = listener
    }
    override fun getItemCount() = mList.size
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mList[position]
        holder.submissionsSurnameTextView.text = item.studentSurname + ", "
        holder.submissionsNameTextView.text = item.studentName
        holder.submissionsStudentNumberTextView.text = item.studentNumber
        holder.statusTextView.text = item.submissionStatus
        updateStatusImageAndText(holder.statusImageView,holder.statusTextView,item.submissionStatus)
        holder.overflowMenu.setOnClickListener { showPopupMenu(holder.overflowMenu, position)}
        holder.bind(item)
    }
    private fun showPopupMenu(view: View, position: Int) {
        val popup = PopupMenu(context, view)
        val inflater: MenuInflater = popup.menuInflater
        inflater.inflate(R.menu.overflow_menu, popup.menu)
        setMenuIcons(popup)
        popup.setOnMenuItemClickListener(MyMenuItemClickListener(position))
        popup.show()
    }
    private fun setMenuIcons(popup: PopupMenu) {
        popup.menu.findItem(R.id.action_setMarked).icon = ContextCompat.getDrawable(context, R.drawable.tick)
        popup.menu.findItem(R.id.action_setUnmarked).icon = ContextCompat.getDrawable(context, R.drawable.unmarked)
        popup.menu.findItem(R.id.action_setInProgress).icon = ContextCompat.getDrawable(context, R.drawable.inprogress)
    }
    private fun updateSubmission(submissionID: Int, assessmentID:Int, studentNumber: String, submissionStatus: String) {
        if (context is SubmissionsActivity) {
            Log.e("check", assessmentID.toString())
            RetrofitClient.updateSubmission(context,submissionID,assessmentID,studentNumber,submissionStatus)
            submissionUpdateListener?.onSubmissionUpdated()
        }
    }
    inner class MyMenuItemClickListener(position: Int) : PopupMenu.OnMenuItemClickListener {
        private val cur = mList[position]
        override fun onMenuItemClick(item: MenuItem): Boolean {
            return when (item.itemId) {
                R.id.action_setMarked -> {
                    updateSubmission(cur.submissionID, cur.assessmentID,cur.studentNumber,context.getString(R.string.marked))
                    true
                }
                R.id.action_setUnmarked-> {
                    updateSubmission(cur.submissionID,cur.assessmentID,cur.studentNumber, context.getString(R.string.unmarked))
                    true
                }
                R.id.action_setInProgress-> {
                    updateSubmission(cur.submissionID, cur.assessmentID,cur.studentNumber,context.getString(R.string.in_progress))
                    true
                }
                else -> false
            }
        }
    }
    inner class ViewHolder(ItemView: View): RecyclerView.ViewHolder(ItemView){
        val submissionsNameTextView: TextView = itemView.findViewById(R.id.submissionNameTextView)
        val submissionsSurnameTextView: TextView = itemView.findViewById(R.id.submissionsSurnameTextView)
        val submissionsStudentNumberTextView: TextView = itemView.findViewById(R.id.submissionStudentNumberTextView)
        val overflowMenu: ImageView = itemView.findViewById(R.id.overflowMenu)
        val statusTextView: TextView = itemView.findViewById(R.id.submissionsStatusTextView)
        val statusImageView: ImageView = itemView.findViewById(R.id.statusImageView)
        fun bind(submissionsResponse: SubmissionsResponse){
            itemView.setOnClickListener{
                listener?.onItemClick(adapterPosition)
            }
        }
    }
    fun interface OnItemClickListener{
        fun onItemClick(position: Int)
    }
    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }
    private fun updateStatusImageAndText(imageView: ImageView, textView: TextView, status: String){
        when(status){
            context.getString(R.string.marked) -> {
                imageView.setImageResource(R.drawable.tick)
                textView.setTextColor(ContextCompat.getColor(context, R.color.green))
            }
            context.getString(R.string.unmarked) -> {
                imageView.setImageResource(R.drawable.unmarked)
                textView.setTextColor(ContextCompat.getColor(context, R.color.red))
            }
            context.getString(R.string.in_progress) -> {
                imageView.setImageResource(R.drawable.inprogress)
                val typedValue = TypedValue()
                context.theme.resolveAttribute(android.R.attr.textColorPrimary, typedValue, true)
                val colorResId = if (typedValue.resourceId != 0) {
                    typedValue.resourceId
                } else {
                    typedValue.data
                }
                textView.setTextColor(ContextCompat.getColor(context, colorResId))
            }
        }
    }
}