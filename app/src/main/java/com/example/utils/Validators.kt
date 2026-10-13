package com.example.utils

object Validators {
    fun isValidPhoneNumber(phone: String): Boolean {
        val digits = phone.filter { it.isDigit() }
        return digits.length >= 10
    }

    fun isValidUsernameLength(username: String): Boolean {
        return username.length >= 5
    }

    fun isValidUsernameChars(username: String): Boolean {
        return username.matches(Regex("^[a-z0-9_]+$"))
    }
}
