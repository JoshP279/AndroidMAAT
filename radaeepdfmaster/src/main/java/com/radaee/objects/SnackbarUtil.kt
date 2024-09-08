package com.radaee.objects

import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.radaee.pdfmaster.R as AppR

object SnackbarUtil {

    fun showInfoSnackBar(view: View?, message: String, context: Context?) {
        if (view == null || context == null) {
            return
        }
        if (!view.isAttachedToWindow) {
            return
        }
        try {
            val snackbar = Snackbar.make(view, message, Snackbar.LENGTH_LONG)
            val snackbarView = snackbar.view
            snackbarView.setBackgroundColor(ContextCompat.getColor(context, AppR.color.colorPrimary))

            val snackbarText = snackbarView.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
            snackbarText?.let {
                it.setTextColor(ContextCompat.getColor(context, AppR.color.white))
            }
            snackbar.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun showErrorSnackBar(view: View?, message: String, context: Context?) {
        if (view == null || context == null) {
            return
        }
        if (!view.isAttachedToWindow) {
            return
        }
        try {
            val snackbar = Snackbar.make(view, message, Snackbar.LENGTH_LONG)
            val snackbarView = snackbar.view
            snackbarView.setBackgroundColor(ContextCompat.getColor(context, AppR.color.red))

            val snackbarText = snackbarView.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
            snackbarText?.let {
                it.setTextColor(ContextCompat.getColor(context, AppR.color.white))
            }

            snackbar.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun showSuccessSnackBar(view: View?, message: String, context: Context?) {
        if (view == null || context == null) {
            return
        }
        if (!view.isAttachedToWindow) {
            return
        }
        try {
            val snackbar = Snackbar.make(view, message, Snackbar.LENGTH_LONG)
            val snackbarView = snackbar.view
            snackbarView.setBackgroundColor(ContextCompat.getColor(context, AppR.color.green))

            val snackbarText = snackbarView.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
            snackbarText?.let {
                it.setTextColor(ContextCompat.getColor(context, AppR.color.white))
            }

            snackbar.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
