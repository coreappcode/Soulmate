package com.example.utils

object Validators {
    fun isValidPhoneNumber(phone: String): Boolean {
        val digits = phone.filter { it.isDigit() }
        return digits.length in 10..15
    }

    fun isValidUsernameLength(username: String): Boolean {
        return username.length in 5..45
    }

    fun isValidUsernameChars(username: String): Boolean {
        return username.matches(Regex("^[a-z0-9_]+$"))
    }

    fun isValidUsername(username: String): Boolean {
        return isValidUsernameLength(username) && isValidUsernameChars(username)
    }

    fun isValidName(name: String): Boolean {
        return name.trim().length in 2..50
    }

    fun isValidEmail(email: String): Boolean {
        return email.isNotBlank() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}
