package com.radaee.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.radaee.pdfmaster.R

/**
 * AboutFragment contains the information about the app, the developer, etc.
 * It also contains a helper text that explains the purpose of the app.
 */
class AboutFragment : Fragment() {
    private lateinit var aboutHelper: TextView
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_about, container, false)
        aboutHelper = view.findViewById(R.id.aboutHelper)
        aboutHelper.setOnClickListener {
            displayHelperDialog()
        }
        return view
    }

    /**
     * Displays a dialog box with the helper message
     */
    private fun displayHelperDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(R.string.helperHeading)
        builder.setMessage(R.string.aboutHelperMessage)
        builder.setPositiveButton(resources.getString(R.string.ok)) { dialog, _ -> dialog.dismiss() }
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }
}