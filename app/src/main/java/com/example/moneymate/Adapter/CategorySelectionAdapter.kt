package com.example.moneymate

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class CategorySelectionAdapter(
    private var categories: List<String>,
    private val onCategorySelected: (String) -> Unit
) : RecyclerView.Adapter<CategorySelectionAdapter.CategoryViewHolder>() {

    private var selectedPosition: Int = -1

    class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val categoryIcon: ImageView = itemView.findViewById(R.id.categoryIcon)
        val categoryName: TextView = itemView.findViewById(R.id.categoryName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category_selection, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categories[position]
        holder.categoryName.text = category

        // Set category icon and background color based on category
        val (iconRes, bgColorRes) = when (category.lowercase()) {
            "food" -> Pair(R.drawable.ic_food, R.color.category_food)
            "social" -> Pair(R.drawable.ic_social, R.color.category_social)
            "traffic" -> Pair(R.drawable.ic_traffic, R.color.category_traffic)
            "shopping" -> Pair(R.drawable.ic_shopping, R.color.category_shopping)
            "grocery" -> Pair(R.drawable.ic_grocery, R.color.category_grocery)
            "education" -> Pair(R.drawable.ic_education, R.color.category_education)
            "bills" -> Pair(R.drawable.ic_bills, R.color.category_bills)
            "rental" -> Pair(R.drawable.ic_rental, R.color.category_rental)
            "medical" -> Pair(R.drawable.ic_medical, R.color.category_medical)
            "investment" -> Pair(R.drawable.ic_investment, R.color.category_investment)
            "gift" -> Pair(R.drawable.ic_gift, R.color.category_gift)
            "salary" -> Pair(R.drawable.ic_salary, R.color.category_salary)
            "freelance" -> Pair(R.drawable.ic_freelance, R.color.category_freelance)
            else -> Pair(R.drawable.ic_other, R.color.category_other)
        }

        holder.categoryIcon.setImageResource(iconRes)
        holder.categoryIcon.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, bgColorRes))

        // Highlight selected category
        holder.itemView.isSelected = position == selectedPosition
        holder.itemView.setOnClickListener {
            val previousPosition = selectedPosition
            selectedPosition = position
            notifyItemChanged(previousPosition)
            notifyItemChanged(selectedPosition)
            onCategorySelected(category)
        }
    }

    override fun getItemCount(): Int = categories.size

    fun updateCategories(newCategories: List<String>) {
        categories = newCategories
        selectedPosition = -1
        notifyDataSetChanged()
    }
}