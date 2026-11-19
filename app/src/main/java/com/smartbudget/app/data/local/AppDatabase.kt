package com.smartbudget.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.smartbudget.app.data.local.dao.BudgetDao
import com.smartbudget.app.data.local.dao.CategoryDao
import com.smartbudget.app.data.local.dao.ExpenseDao
import com.smartbudget.app.data.local.entity.Budget
import com.smartbudget.app.data.local.entity.Category
import com.smartbudget.app.data.local.entity.Expense

@Database(entities = [Expense::class, Budget::class, Category::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun expenseDao(): ExpenseDao
    abstract fun budgetDao(): BudgetDao
    abstract fun categoryDao(): CategoryDao


    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "smartbudget_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}