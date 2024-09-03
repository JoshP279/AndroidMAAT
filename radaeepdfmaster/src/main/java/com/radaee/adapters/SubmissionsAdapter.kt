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

/**
 * Adapter for the recycler view that displays the submissions
 * @param mList list of submissions
 * @param context context of the activity, which is @SubmissionsActivity
 * This adapter just binds the necessary data to the submissions card view
 */
class SubmissionsAdapter(private val mList: List<SubmissionsResponse>, private val context: Context, private val totalMarks: Int, private val markingStyle: String) : RecyclerView.Adapter<SubmissionsAdapter.ViewHolder>()  {
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
    /**
     * Interface for the listener that listens for submission updates
     */
    interface SubmissionUpdateListener {
        fun onSubmissionUpdated()
    }

    /**
     * Sets the listener for submission updates
     * @param listener listener for submission updates
     */
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

    /**
     * Custom pop up menu that allows for a submission status to be updated
     * @param view view of the card view
     * @param position position of the card view
     * This function shows a pop up menu when the overflow menu is clicked
     */
    private fun showPopupMenu(view: View, position: Int) {
        val popup = PopupMenu(context, view)
        val inflater: MenuInflater = popup.menuInflater
        inflater.inflate(R.menu.overflow_menu, popup.menu)
        setMenuIcons(popup)
        popup.setOnMenuItemClickListener(MyMenuItemClickListener(position))
        popup.show()
    }

    /**
     * Set the icon for the respective menu item
     */
    private fun setMenuIcons(popup: PopupMenu) {
        popup.menu.findItem(R.id.action_setMarked).icon = ContextCompat.getDrawable(context, R.drawable.tick)
        popup.menu.findItem(R.id.action_setUnmarked).icon = ContextCompat.getDrawable(context, R.drawable.unmarked)
        popup.menu.findItem(R.id.action_setInProgress).icon = ContextCompat.getDrawable(context, R.drawable.inprogress)
    }

    /**
     * Update the submission status
     * All params are necessary to update the submission status
     * @param submissionID submission's unique ID
     * @param assessmentID assessment's unique ID
     * @param studentNumber student number
     * @param submissionStatus submission status to update (marked, unmarked, in progress)
     */
    private fun updateSubmission(submissionID: Int, assessmentID:Int, studentNumber: String, submissionStatus: String, submissionFolderName: String) {
        if (context is SubmissionsActivity) {
            RetrofitClient.updateSubmission(context,submissionID,assessmentID,totalMarks,submissionStatus, submissionFolderName, markingStyle)
            submissionUpdateListener?.onSubmissionUpdated()
        }
    }
    /**
     * This handles the click events for the pop up menu
     * @param position position of the card view
     */
    inner class MyMenuItemClickListener(position: Int) : PopupMenu.OnMenuItemClickListener {
        private val cur = mList[position]
        override fun onMenuItemClick(item: MenuItem): Boolean {
            return when (item.itemId) {
                R.id.action_setMarked -> {
                    updateSubmission(cur.submissionID, cur.assessmentID,cur.studentNumber,context.getString(R.string.marked), cur.submissionFolderName)
                    true
                }
                R.id.action_setUnmarked-> {
                    updateSubmission(cur.submissionID,cur.assessmentID,cur.studentNumber, context.getString(R.string.unmarked), cur.submissionFolderName)
                    true
                }
                R.id.action_setInProgress-> {
                    updateSubmission(cur.submissionID, cur.assessmentID,cur.studentNumber,context.getString(R.string.in_progress), cur.submissionFolderName)
                    true
                }
                else -> false
            }
        }
    }

    /**
     * View holder for the recycler view
     * @param ItemView view of the card view
     * This class just binds the text views in the card view
     */

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
    /**
     * Interface for the listener that listens for item clicks
     */
    fun interface OnItemClickListener{
        fun onItemClick(position: Int)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    /**
     * Update the status image and text
     * @param imageView image view of the status
     * @param textView text view of the status
     * @param status status of the submission
     * This function updates the status image and text based on the submission status, not the database submission status itself, that is done in the @updateSubmission function
     */
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