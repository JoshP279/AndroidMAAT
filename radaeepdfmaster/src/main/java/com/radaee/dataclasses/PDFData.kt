package com.radaee.dataclasses

/**
 * Data class to hold the data of the PDF file
 * @param type: String, the type of the data of the PDF file (usually buffer)
 * @param data: List<Int>, the data of the PDF file
 */
data class PDFData(
    val type: String,
    val data: List<Int>
)
