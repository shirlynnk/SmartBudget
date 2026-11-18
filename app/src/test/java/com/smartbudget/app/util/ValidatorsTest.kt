package com.smartbudget.app.util
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Test

class ValidatorsTest {
    @Test
    fun requiredField_blank_isInvalid(){
        assertFalse(Validators.validateRequired("", "Name").isValid)
        assertFalse(Validators.validateRequired(null, "Name").isValid)
    }
    @Test
    fun requiredField_withText_isValid(){
        assertTrue(Validators.validateRequired("Food","Name").isValid)
    }
    @Test
    fun amount_notANumber_isInvalid(){
        assertFalse(Validators.validateAmount("abc").isValid)
        assertFalse(Validators.validateAmount("NaN").isValid)
    }
    @Test
    fun amount_negativeNumber_isInvalid(){
        assertFalse(Validators.validateAmount("-50").isValid)
    }
    @Test
    fun amount_zero_isInvalid(){
        assertFalse(Validators.validateAmount("0").isValid)
    }
    @Test
    fun amount_positive_isValid() {
        val first = Validators.validateAmount("150.50")
        assertTrue("150.50 failed: ${first.errorMessage}", first.isValid)
        val second = Validators.validateAmount(" 200 ")
        assertTrue("' 200 ' failed: ${second.errorMessage}", second.isValid)
    }

    @Test
    fun categoryName_duplicate_ignoringCae_isInvalid() {
        val result = Validators.validateCategoryName("food", listOf("Food", "Transport"))
        assertFalse(result.isValid)
    }

    @Test
    fun categoryName_tooLong_isInvalid() {
        assertFalse(Validators.validateCategoryName("A".repeat(31), emptyList()).isValid)
    }

    @Test
    fun categoryName_new_isValid() {
        assertTrue(Validators.validateCategoryName("Rent", listOf("Food")).isValid)
    }
}