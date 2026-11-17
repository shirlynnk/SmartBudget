package com.smartbudget.tempbudget

import com.smartbudget.tempbudget.database.Expense
import com.smartbudget.tempbudget.database.ExpenseDao

class ExpenseRepository(private val expenseDao: ExpenseDao) {

    suspend fun getAllExpenses(): List<Expense> {
        return expenseDao.getAllExpenses()
    }

    suspend fun insert(expense: Expense) {
        expenseDao.insertExpense(expense)
    }
}