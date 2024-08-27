package com.radaee.objects

object RegexUtils {
    // Regular expression for validating email
    private val EMAIL_PATTERN = Regex(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
    )

    // Function to validate email
    fun isValidEmail(email: String): Boolean {
        return EMAIL_PATTERN.matches(email)
    }
}
