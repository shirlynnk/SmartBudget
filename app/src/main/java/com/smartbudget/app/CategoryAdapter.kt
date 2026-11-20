package com.smartbudget.app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.smartbudget.app.data.local.entity.Category
import java.util.Locale

class CategoryAdapter(
    private val onRename: (Category) -> Unit,
    private val onDelete: (Category) -> Unit
) : ListAdapter<Category, CategoryAdapter.CategoryViewHolder>(CategoryDiffCallback()) {

    private var totals: Map<String, Double> = emptyMap()
    private var maxSpending: Double = 0.0

    fun submitSpendingTotals(values: Map<String, Double>) {
        totals = values
        maxSpending = values.values.maxOrNull() ?: 0.0
        notifyDataSetChanged()
    }

    class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tvCategoryName)
        private val tvIcon: TextView = itemView.findViewById(R.id.tvCategoryIcon)
        private val tvSpending: TextView = itemView.findViewById(R.id.tvCategorySpending)
        private val progress: LinearProgressIndicator =
            itemView.findViewById(R.id.progressCategorySpending)
        private val btnRename: View = itemView.findViewById(R.id.btnRename)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)

        fun bind(
            category: Category,
            spending: Double?,
            maxSpending: Double,
            onRename: (Category) -> Unit,
            onDelete: (Category) -> Unit
        ) {
            tvName.text = category.categoryName
            tvIcon.text = category.categoryName.firstOrNull()?.uppercaseChar()?.toString() ?: "•"
            tvSpending.visibility = if (spending == null) View.GONE else View.VISIBLE
            tvSpending.text = spending?.let {
                String.format(Locale.getDefault(), "Spent KSh %,.2f", it)
            }.orEmpty()
            progress.visibility = if (spending == null) View.GONE else View.VISIBLE
            if (spending != null) {
                val percent = if (maxSpending > 0.0) {
                    ((spending / maxSpending) * 100).toInt().coerceIn(0, 100)
                } else 0
                progress.setProgressCompat(percent, false)
            }
            btnRename.setOnClickListener { onRename(category) }
            btnDelete.setOnClickListener { onDelete(category) }
            itemView.contentDescription = buildString {
                append(category.categoryName)
                spending?.let { append(", spent KSh ").append(String.format(Locale.getDefault(), "%,.2f", it)) }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder =
        CategoryViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_category, parent, false)
        )

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = getItem(position)
        holder.bind(category, totals[category.categoryName], maxSpending, onRename, onDelete)
    }

    class CategoryDiffCallback : DiffUtil.ItemCallback<Category>() {
        override fun areItemsTheSame(oldItem: Category, newItem: Category) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Category, newItem: Category) = oldItem == newItem
    }
}