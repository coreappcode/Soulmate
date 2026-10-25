package com.example.utils

import java.text.SimpleDateFormat
import java.text.ParsePosition
import java.util.Calendar
import java.util.Locale

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

    fun ageFromDateOfBirth(dateOfBirth: String): Int? {
        val formatter = SimpleDateFormat("MMM d, yyyy", Locale.US).apply {
            isLenient = false
        }
        val parsePosition = ParsePosition(0)
        val birthDate = formatter.parse(dateOfBirth, parsePosition)
            ?.takeIf { parsePosition.index == dateOfBirth.length } ?: return null
        val birth = Calendar.getInstance().apply { time = birthDate }
        val today = Calendar.getInstance()
        var age = today.get(Calendar.YEAR) - birth.get(Calendar.YEAR)
        val birthdayHasPassed = today.get(Calendar.MONTH) > birth.get(Calendar.MONTH) ||
            (today.get(Calendar.MONTH) == birth.get(Calendar.MONTH) &&
                today.get(Calendar.DAY_OF_MONTH) >= birth.get(Calendar.DAY_OF_MONTH))
        if (!birthdayHasPassed) age--
        return age.takeIf { it in 0..120 && !birth.after(today) }
    }

    fun isValidAdultDateOfBirth(dateOfBirth: String): Boolean =
        ageFromDateOfBirth(dateOfBirth) in 18..120
}
