package com.smartbudget.app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.smartbudget.app.data.local.entity.Expense
import java.util.Locale

class ExpenseAdapter(
    private val onItemClick: (Expense) -> Unit,
    private val onDeleteClick: (Expense) -> Unit
) : ListAdapter<Expense, ExpenseAdapter.ExpenseViewHolder>(ExpenseDiffCallback()) {

    class ExpenseViewHolder(
        itemView: View,
        private val onItemClick: (Expense) -> Unit,
        private val onDeleteClick: (Expense) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val tvItemName: TextView = itemView.findViewById(R.id.tvItemName)
        private val tvCategoryName: TextView = itemView.findViewById(R.id.tvCategoryName)
        private val tvAmount: TextView = itemView.findViewById(R.id.tvAmount)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btnDeleteExpense)

        fun bind(expense: Expense) {
            tvItemName.text = expense.itemName
            tvCategoryName.text = expense.categoryName.ifEmpty { "Uncategorized" }
            tvAmount.text = String.format(Locale.getDefault(), "KSh %,.2f", expense.amount)
            itemView.findViewById<TextView>(R.id.tvExpenseIcon).text =
                expense.categoryName.firstOrNull()?.uppercaseChar()?.toString() ?: "•"

            itemView.setOnClickListener { onItemClick(expense) }
            btnDelete.setOnClickListener { onDeleteClick(expense) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_expense, parent, false)
        return ExpenseViewHolder(view, onItemClick, onDeleteClick)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ExpenseDiffCallback : DiffUtil.ItemCallback<Expense>() {
        override fun areItemsTheSame(oldItem: Expense, newItem: Expense): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Expense, newItem: Expense): Boolean {
            return oldItem == newItem
        }
    }
}