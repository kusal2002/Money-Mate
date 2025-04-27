package com.example.moneymate

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TransactionAdapter(
    private var transactions: List<Transaction>,
    private val currency: String,
    private val onDeleteClick: (Int) -> Unit,
    private val onEditClick: (Transaction, Int) -> Unit
) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameText: TextView = itemView.findViewById(R.id.transactionName)
        val categoryText: TextView = itemView.findViewById(R.id.transactionCategory)
        val amountText: TextView = itemView.findViewById(R.id.transactionAmount)
        val dateText: TextView = itemView.findViewById(R.id.transactionDate)
        val editButton: ImageView = itemView.findViewById(R.id.editTransactionButton)
        val deleteButton: ImageView = itemView.findViewById(R.id.deleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = transactions[position]
        holder.nameText.text = transaction.name
        holder.categoryText.text = transaction.category
        holder.amountText.text = "${if (transaction.isExpense) "-" else "+"}${transaction.amount.toInt()} $currency"
        holder.dateText.text = transaction.date

        holder.editButton.setOnClickListener {
            onEditClick(transaction, position)
        }

        holder.deleteButton.setOnClickListener {
            onDeleteClick(position)
        }
    }

    override fun getItemCount(): Int = transactions.size

    fun updateTransactions(newTransactions: List<Transaction>) {
        transactions = newTransactions
        notifyDataSetChanged()
    }
}