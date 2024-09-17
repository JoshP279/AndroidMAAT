package com.radaee.objects

object RegexUtils {
    private val EMAIL_PATTERN = Regex(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
    )

    private val MARK_PATTERN = Regex(
        "^(100(\\.0{0,2})?|[0-9]{1,2}(\\.\\d{1,2})?)$"
    )

    fun isValidEmail(email: String): Boolean {
        return EMAIL_PATTERN.matches(email)
    }

    fun isValidMark(mark: String): Boolean {
        return MARK_PATTERN.matches(mark) && mark.toDoubleOrNull() != null && mark.toDouble() in 0.0..100.0
    }

}
