package com.radaee.adapters

import android.app.AlertDialog
import android.content.Context
import android.content.res.Configuration
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.TypedArrayUtils.getResourceId
import androidx.recyclerview.widget.RecyclerView
import com.radaee.activities.SubmissionsActivity
import com.radaee.dataclasses.SubmissionsResponse
import com.radaee.objects.RegexUtils
import com.radaee.objects.RetrofitClient
import com.radaee.objects.SharedPref
import com.radaee.pdfmaster.R

/**
 * Adapter for the recycler view that displays the submissions
 * @param mList list of submissions
 * @param context context of the activity, which is @SubmissionsActivity
 * This adapter just binds the necessary data to the submissions card view
 */
class SubmissionsAdapter(private val mList: List<SubmissionsResponse>, private val context: Context, private val rootView: View, private val totalMarks: Int, private val markingStyle: String) : RecyclerView.Adapter<SubmissionsAdapter.ViewHolder>()  {
    private var listener: OnItemClickListener? = null
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.cardview_submission, parent, false)
        return ViewHolder(view)
    }
    override fun getItemCount() = mList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mList[position]
        holder.submissionsSurnameTextView.text = item.studentSurname + ", "
        holder.submissionsNameTextView.text = item.studentName
        holder.submissionsSurnameTextView.apply {
            isVerticalScrollBarEnabled = true
            movementMethod = ScrollingMovementMethod()
        }
        holder.submissionsStudentNumberTextView.text = item.studentNumber
        holder.statusTextView.text = item.submissionStatus
        updateStatusImageAndText(holder.statusImageView,holder.statusTextView,item.submissionStatus, holder.submissionMark, item.submissionMark)
        holder.overflowMenu.setOnClickListener { showPopupMenu(holder.overflowMenu, position)}
        holder.bind(item)

        if (SharedPref.getBoolean(context, "OFFLINE_MODE", false)) {
            if (context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                holder.submissionsSurnameTextView.maxWidth = 1000
            }else{
                holder.submissionsSurnameTextView.maxWidth = 600
            }
        }
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
    private fun updateSubmission(submissionID: Int, assessmentID:Int, submissionStatus: String, submissionFolderName: String) {
        if (context is SubmissionsActivity) {
            RetrofitClient.updateSubmission(context, rootView, submissionID,assessmentID,totalMarks,submissionStatus, submissionFolderName, markingStyle)
        }
    }
    /**
     * This handles the click events for the pop up menu
     * @param position position of the card view
     */
    inner class MyMenuItemClickListener(position: Int) : PopupMenu.OnMenuItemClickListener {
        private var cur = mList[position]
        override fun onMenuItemClick(item: MenuItem): Boolean {
            return when (item.itemId) {
                R.id.action_setMarked -> {
                    updateSubmission(cur.submissionID, cur.assessmentID,context.getString(R.string.marked), cur.submissionFolderName)
                    true
                }
                R.id.action_setUnmarked-> {
                    updateSubmission(cur.submissionID,cur.assessmentID, context.getString(R.string.unmarked), cur.submissionFolderName)
                    true
                }
                R.id.action_setInProgress-> {
                    updateSubmission(cur.submissionID, cur.assessmentID,context.getString(R.string.in_progress), cur.submissionFolderName)
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
        val submissionMark: TextView = itemView.findViewById(R.id.submissionMarkEditText)
        init {
            submissionMark.setOnClickListener {
                showMarkInputDialog(adapterPosition)
            }
        }
        fun bind(submissionsResponse: SubmissionsResponse){
            itemView.setOnClickListener{
                listener?.onItemClick(adapterPosition)
            }
        }
        private fun showMarkInputDialog(position: Int) {
            val builder = AlertDialog.Builder(context)
            builder.setTitle(R.string.edit_mark)

            val layout = LinearLayout(context)
            layout.orientation = LinearLayout.VERTICAL
            layout.setPadding(50, 40, 50, 10)

            val input = EditText(context)
            input.hint = "e.g. 85"
            input.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            input.setText(submissionMark.text.toString().replace("%", ""))

            layout.addView(input)

            builder.setView(layout)

            builder.setPositiveButton(R.string.confirm) { dialog, _ ->
                val newMark = input.text.toString().trim()

                println("User input mark: $newMark")

                val mark = newMark.toDoubleOrNull()

                if (!(newMark.isEmpty() || mark == null || mark < 0.0 || mark > 100.0)) {
                    submissionMark.setText("$newMark%")
                    updateSubmissionMarkInBackend(mark, position)
                }
            }

            builder.setNegativeButton(R.string.text_cancel_label) { dialog, _ ->
                dialog.cancel()
            }

            val dialog = builder.create()
            dialog.show()

            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.isEnabled = false

            input.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    val newMark = s.toString()

                    if (RegexUtils.isValidMark(newMark)) {
                        positiveButton.isEnabled = true
                        input.error = null
                    } else {
                        positiveButton.isEnabled = false
                        input.error = context.getString(R.string.invalid_mark)
                    }
                }

                override fun afterTextChanged(s: Editable?) {}
            })

            positiveButton.setOnClickListener {
                val newMark = input.text.toString()
                if (RegexUtils.isValidMark(newMark)) {
                    submissionMark.setText("$newMark%")
                    updateSubmissionMarkInBackend(newMark.toDouble(), position)
                    dialog.dismiss()
                }
            }

            input.requestFocus()
            input.postDelayed({
                val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT)
            }, 200)
        }


        private fun updateSubmissionMarkInBackend(newMark: Double, position: Int) {
            val submission = mList[position]
            submission.submissionMark = newMark
            Log.d("SubmissionAdapter", "Updating mark for submission ${submission.submissionID} to $newMark")
            RetrofitClient.updateSubmissionMark(context, rootView, submission.submissionID, newMark)
            notifyItemChanged(position)
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
    private fun updateStatusImageAndText(imageView: ImageView, statusTextView: TextView, status: String, markTextView: TextView, submissionMark: Number){
        when(status){
            context.getString(R.string.marked) -> {
                imageView.setImageResource(R.drawable.tick)
                statusTextView.setTextColor(ContextCompat.getColor(context, R.color.green))
                markTextView.visibility = View.VISIBLE
                markTextView.setText(submissionMark.toString() + "%")
            }
            context.getString(R.string.unmarked) -> {
                imageView.setImageResource(R.drawable.unmarked)
                statusTextView.setTextColor(ContextCompat.getColor(context, R.color.red))
                markTextView.visibility = View.INVISIBLE
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
                statusTextView.setTextColor(ContextCompat.getColor(context, colorResId))
                markTextView.visibility = View.INVISIBLE
            }
        }
    }
}