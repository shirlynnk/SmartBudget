package com.smartbudget.app
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.smartbudget.app.data.local.entity.Category
import com.smartbudget.app.data.repository.CategoryRepository
import com.smartbudget.app.util.ValidationResult
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
class CategoryViewModel(private val repository: CategoryRepository) : ViewModel() {

        // null = still loading, empty list = loaded but no categories
        val categories: StateFlow<List<Category>?> = repository.categories
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

        fun addCategory(name: String, onResult: (ValidationResult) -> Unit) {
            viewModelScope.launch { onResult(repository.addCategory(name)) }
        }

        fun renameCategory(category: Category, newName: String, onResult: (ValidationResult) -> Unit) {
            viewModelScope.launch { onResult(repository.renameCategory(category, newName)) }
        }

        fun deleteCategory(category: Category) {
            viewModelScope.launch { repository.deleteCategory(category) }
        }
    }

    class CategoryViewModelFactory(
        private val repository: CategoryRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CategoryViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return CategoryViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
