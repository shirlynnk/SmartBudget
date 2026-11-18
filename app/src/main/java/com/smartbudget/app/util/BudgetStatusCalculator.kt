package com.smartbudget.app.util

object BudgetStatusCalculator {
    private const val WARNING_THRESHOLD =0.80
    fun calculate(budget: Double, spent: Double): BudgetStatus{
        if(budget<=0.0){
            return if (spent>0.0) BudgetStatus.OVER_BUDGET else BudgetStatus.WITHIN_BUDGET
        }
        val ratio=spent/budget
        return when {
            ratio>1.0-> BudgetStatus.OVER_BUDGET
            ratio>=WARNING_THRESHOLD-> BudgetStatus.CLOSE_TO_EXCEEDING
            else-> BudgetStatus.WITHIN_BUDGET
        }
    }
}