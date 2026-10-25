package com.example.utils

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidatorsTest {
    @Test
    fun adultBirthDateMustBeStrictlyParsedAndWithinAgeRange() {
        assertTrue(Validators.isValidAdultDateOfBirth("Jan 1, 2000"))
        assertFalse(Validators.isValidAdultDateOfBirth("Jan 1, 2010"))
        assertFalse(Validators.isValidAdultDateOfBirth("Jan 1, 2099"))
        assertFalse(Validators.isValidAdultDateOfBirth("Jan 1, 2000 trailing"))
        assertFalse(Validators.isValidAdultDateOfBirth("Jan 1, 1800"))
    }
}
