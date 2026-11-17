package com.smartbudget.app.data.repository

import com.smartbudget.app.data.local.dao.ExpenseDao
import com.smartbudget.app.data.local.entity.Expense

class ExpenseRepository(private val expenseDao: ExpenseDao) {

    suspend fun getAllExpenses(): List<Expense> {
        return expenseDao.getAllExpenses()
    }

    suspend fun insert(expense: Expense) {
        expenseDao.insertExpense(expense)
    }
}