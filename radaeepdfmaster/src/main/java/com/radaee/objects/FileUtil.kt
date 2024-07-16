package com.radaee.objects

import android.content.Context
import android.os.Environment
import android.util.Log
import android.widget.Toast
import com.radaee.pdfmaster.R
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object FileUtil {
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

    fun checkSubmissionExists(folderPath: File, studentNumber: String): Boolean {
        val fileName = "submission_$studentNumber.pdf"
        val file = File(folderPath, fileName)
        return file.exists()
    }

    fun checkMemoExists(folderPath: File, assessmentID:Int): Boolean{
        val fileName = "memo_$assessmentID.pdf"
        val file = File(folderPath, fileName)
        return file.exists()
    }

    fun getSubmissionFile(assessmentID: Int, studentNumber: String): File {
        val documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        val submissionFile = File(documentsDir, "Assessment_$assessmentID/submission_$studentNumber.pdf")
        return submissionFile
    }
}