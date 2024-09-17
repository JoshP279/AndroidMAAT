package com.radaee.objects

import android.content.Context
import android.os.Environment
import android.util.Log
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
     * @param submissionID the submission ID to use in the file name
     * @param documentsDir the documents directory to save the file in
     * @return the absolute path of the saved file, or null if the file could not be saved
     */
    fun saveSubmissionPDF(context: Context, pdfData: ByteArray,submissionFolderName: String, documentsDir: File): String? {
        return try {
            val file = File(documentsDir, "$submissionFolderName.pdf")
            val fos = FileOutputStream(file)
            fos.write(pdfData)
            fos.close()
            file.absolutePath
        } catch (e: IOException) {
            e.printStackTrace()
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
            file.absolutePath
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Check if a submission file exists in the specified folder.
     * @param folderPath the folder to check for the file in
     * @param studentNumber the student number to check for
     * @return true if the file exists, false otherwise
     */
    fun checkSubmissionExists(folderPath: File, fileName: String, studentNumber: String): Boolean {
        val file: File = if (!fileName.contains(".pdf")) File(folderPath, "$fileName.pdf")
        else File(folderPath, fileName)
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
     * @param submissionID the student number to get the file for
     * @param submissionFolderName the submission folder name to get the file for
     * @return the submission file
     */
    fun getSubmissionFile(assessmentID: Int, submissionID: Int, submissionFolderName: String): File? {
        val documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        val assessmentFolder = documentsDir.listFiles()?.find {
            it.isDirectory && it.name.startsWith("${assessmentID}_")
        }

        return if (assessmentFolder != null) {
            // The file should be named as submissionID_submissionFolderName
            val expectedFileName = "${submissionID}_$submissionFolderName.pdf"
            Log.e("FileUtil", expectedFileName)
            val submissionFile = assessmentFolder.listFiles()?.find {
                it.name == expectedFileName
            }
            submissionFile
        } else {
            null
        }
    }

}