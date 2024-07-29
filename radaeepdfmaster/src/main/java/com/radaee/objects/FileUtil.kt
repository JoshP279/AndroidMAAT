package com.radaee.objects

import android.content.Context
import android.os.Environment
import android.util.Log
import android.widget.Toast
import com.radaee.pdfmaster.R
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * Utility class for file operations.
 */
object FileUtil {
    /**
     * Save the PDF data to a file in the documents directory.
     * @param context the context of the activity or fragment (necessary for displaying toasts)
     * @param pdfData the PDF data to save
     * @param studentNumber the student number to use in the file name
     * @param documentsDir the documents directory to save the file in
     * @return the absolute path of the saved file, or null if the file could not be saved
     */
    fun saveSubmissionPDF(context: Context, pdfData: ByteArray, studentNumber: String, documentsDir: File): String? {
        return try {
            val fileName = "submission_$studentNumber.pdf"
            val file = File(documentsDir, fileName)
            val fos = FileOutputStream(file)
            fos.write(pdfData)
            fos.close()
            Toast.makeText(context, context.getString(R.string.pdf_saved_message, fileName, documentsDir), Toast.LENGTH_SHORT).show()
            file.absolutePath
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(context, R.string.pdf_fail_save, Toast.LENGTH_SHORT).show()
            null
        }
    }

    /**
     * Save the PDF data to a file in the documents directory.
     * @param context the context of the activity or fragment (necessary for displaying toasts)
     * @param pdfData the PDF data to save
     * @param assessmentID the assessment ID to use in the file name
     * @param documentsDir the documents directory to save the file in
     * @return the absolute path of the saved file, or null if the file could not be saved
     */
    fun saveMemoPDF(context: Context,pdfData: ByteArray, assessmentID: Int, documentsDir: File): String? {
        return try {
            val fileName = "memo_$assessmentID.pdf"
            val file = File(documentsDir, fileName)
            val fos = FileOutputStream(file)
            fos.write(pdfData)
            fos.close()
            Toast.makeText(context, "PDF saved as $fileName in $documentsDir", Toast.LENGTH_SHORT).show()
            file.absolutePath
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(context, R.string.pdf_fail_save, Toast.LENGTH_SHORT).show()
            null
        }
    }

    /**
     * Check if a submission file exists in the specified folder.
     * @param folderPath the folder to check for the file in
     * @param studentNumber the student number to check for
     * @return true if the file exists, false otherwise
     */
    fun checkSubmissionExists(folderPath: File, studentNumber: String): Boolean {
        val fileName = "submission_$studentNumber.pdf"
        val file = File(folderPath, fileName)
        return file.exists()
    }

    /**
     * Check if a memo file exists in the specified folder.
     * @param folderPath the folder to check for the file in
     * @param assessmentID the assessment ID to check for
     * @return true if the file exists, false otherwise
     */
    fun checkMemoExists(folderPath: File, assessmentID:Int): Boolean{
        val fileName = "memo_$assessmentID.pdf"
        val file = File(folderPath, fileName)
        return file.exists()
    }

    /**
     * Get the submission file for the specified student number.
     * @param assessmentID the assessment ID to get the file for
     * @param studentNumber the student number to get the file for
     * @return the submission file
     */
    fun getSubmissionFile(assessmentID: Int, studentNumber: String): File {
        val documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        val submissionFile = File(documentsDir, "Assessment_$assessmentID/submission_$studentNumber.pdf")
        return submissionFile
    }
}