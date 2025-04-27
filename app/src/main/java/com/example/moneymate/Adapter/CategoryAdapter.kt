package com.example.moneymate

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class CategorySummary(val name: String, val transactionCount: Int, val totalAmount: Double, val iconResId: Int)

class CategoryAdapter(private var categories: List<CategorySummary>) :
    RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val categoryIcon: ImageView = itemView.findViewById(R.id.categoryIcon)
        val categoryName: TextView = itemView.findViewById(R.id.categoryName)
        val categoryTransactions: TextView = itemView.findViewById(R.id.categoryTransactions)
        val categoryAmount: TextView = itemView.findViewById(R.id.categoryAmount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categories[position]
        holder.categoryIcon.setImageResource(category.iconResId)
        holder.categoryName.text = category.name
        holder.categoryTransactions.text = "${category.transactionCount} transactions"
        holder.categoryAmount.text = "-$${String.format("%.2f", category.totalAmount)}"
    }

    override fun getItemCount(): Int = categories.size

    fun updateCategories(newCategories: List<CategorySummary>) {
        categories = newCategories
        notifyDataSetChanged()
    }
}