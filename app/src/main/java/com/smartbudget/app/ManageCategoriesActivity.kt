package com.smartbudget.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

/**
 * Keeps the expense form's existing Manage categories entry point, while using the
 * same category screen and CRUD implementation as the navigation drawer.
 */
class ManageCategoriesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_categories)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.manageCategoriesContainer, CategoryFragment())
                .commit()
        }
    }
}