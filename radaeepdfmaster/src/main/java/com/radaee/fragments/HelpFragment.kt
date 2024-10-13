package com.radaee.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.radaee.interfaces.HelpHandler
import com.radaee.pdfmaster.R

/**
 * HelpFragment contains some information intended to help users understand the app
 * Each activity/fragment is mentioned here, along with a brief description of its purpose
 */
class HelpFragment : Fragment(), HelpHandler {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_help, container, false)
        return view
    }

    /**
     * Displays a dialog box with the helper message
     */
    override fun displayHelperDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(R.string.helperHeading)
        builder.setMessage(R.string.helpHelperMessage)
        builder.setPositiveButton(resources.getString(R.string.ok)) { dialog, _ -> dialog.dismiss() }
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }
}