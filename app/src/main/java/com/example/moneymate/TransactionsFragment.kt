package com.example.moneymate

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class TransactionsFragment : Fragment() {
    private lateinit var prefsHelper: PrefsHelper
    private lateinit var transactions: MutableList<Transaction>
    private lateinit var adapter: TransactionAdapter

    // Activity Result Launcher for editing a transaction
    private val editTransactionLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            result.data?.let { intent ->
                val updatedTransaction = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(EditTransactionActivity.EXTRA_TRANSACTION, Transaction::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra(EditTransactionActivity.EXTRA_TRANSACTION)
                }
                val position = intent.getIntExtra(EditTransactionActivity.EXTRA_POSITION, -1)
                if (updatedTransaction != null && position != -1) {
                    transactions[position] = updatedTransaction
                    prefsHelper.saveTransactions(transactions)
                    adapter.updateTransactions(transactions)
                    // Show Toast for successful edit
                    Toast.makeText(requireContext(), "Transaction edited successfully", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_transactions, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prefsHelper = PrefsHelper(requireContext())
        transactions = prefsHelper.getTransactions().toMutableList()

        val transactionRecycler: RecyclerView = view.findViewById(R.id.transactionRecycler)
        adapter = TransactionAdapter(
            transactions,
            prefsHelper.getCurrency(),
            onDeleteClick = { position ->
                transactions.removeAt(position)
                prefsHelper.saveTransactions(transactions)
                adapter.notifyItemRemoved(position)
                // Show Toast for successful deletion
                Toast.makeText(requireContext(), "Transaction deleted successfully", Toast.LENGTH_SHORT).show()
            },
            onEditClick = { transaction, position ->
                val intent = Intent(requireContext(), EditTransactionActivity::class.java)
                intent.putExtra(EditTransactionActivity.EXTRA_TRANSACTION, transaction)
                intent.putExtra(EditTransactionActivity.EXTRA_POSITION, position)
                editTransactionLauncher.launch(intent)
            }
        )
        transactionRecycler.layoutManager = LinearLayoutManager(requireContext())
        transactionRecycler.adapter = adapter
    }

    fun refreshData() {
        transactions.clear()
        transactions.addAll(prefsHelper.getTransactions())
        adapter.updateTransactions(transactions)
    }
}