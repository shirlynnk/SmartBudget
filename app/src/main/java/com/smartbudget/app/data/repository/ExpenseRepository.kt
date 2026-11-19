package com.smartbudget.app.data.repository

import com.smartbudget.app.data.local.dao.ExpenseDao
import com.smartbudget.app.data.local.entity.Expense

class ExpenseRepository(private val expenseDao: ExpenseDao) {

    suspend fun getAllExpenses(): List<Expense> {
        return expenseDao.getAllExpenses()
    }

    suspend fun getExpenseById(id: Int): Expense? {
        return expenseDao.getExpenseById(id)
    }

    suspend fun insert(expense: Expense) {
        expenseDao.insertExpense(expense)
    }

    suspend fun update(expense: Expense) {
        expenseDao.updateExpense(expense)
    }

    suspend fun delete(expense: Expense) {
        expenseDao.deleteExpense(expense)
    }
}