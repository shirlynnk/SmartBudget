package com.smartbudget.app.util

data class ValidationResult(
    val isValid: Boolean,
    val errorMessage: String? = null
)

object Validators {
    private const val MAX_NAME_LENGTH =30
    fun validateRequired(value:String?, fieldName:String):ValidationResult{
        return if(value.isNullOrBlank()){
            ValidationResult(false,"$fieldName is required")
        }else{
            ValidationResult(true)
        }
    }
    fun validateAmount(input:String?):ValidationResult{
        val text=input?.trim()
        if(text.isNullOrEmpty()){
            return ValidationResult(false,"Amount is required")
        }
        val amount=text.toDoubleOrNull()
        return when{
            amount==null|| amount.isNaN() || amount.isInfinite()->
                ValidationResult(false,"Enter a valid number")
            amount<0.0->
                ValidationResult(false,"Amount cannot be negative")
            amount==0.0->ValidationResult(false, "Amount must be greater than zero")
            else->
                ValidationResult(true)
        }
        }
    fun validateCategoryName(
        name:String?,
        existingNames:List<String>
    ): ValidationResult{
        val text=name?.trim()
        return when{
            text.isNullOrEmpty()-> ValidationResult(false,"Category name is required")
            text.length>MAX_NAME_LENGTH->
                ValidationResult(false,"Name must be $MAX_NAME_LENGTH characters or fewer")
            existingNames.any{it.trim().equals(text, ignoreCase=true)}->
                ValidationResult(false,"Category name already exists")
            else->
                ValidationResult(true)
        }
    }
}