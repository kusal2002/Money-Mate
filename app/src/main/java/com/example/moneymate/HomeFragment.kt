package com.example.moneymate

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.Random

class HomeFragment : Fragment() {
    private lateinit var prefsHelper: PrefsHelper
    private lateinit var notificationHelper: NotificationHelper
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
                    val totalBalanceText = view?.findViewById<TextView>(R.id.totalBalanceText)
                    val incomeText = view?.findViewById<TextView>(R.id.incomeText)
                    val expenditureText = view?.findViewById<TextView>(R.id.expenditureText)
                    if (totalBalanceText != null && incomeText != null && expenditureText != null) {
                        updateBalanceAndTransactions(totalBalanceText, incomeText, expenditureText)
                    }
                    checkBudgetStatus()
                    // Show Toast for successful edit
                    Toast.makeText(requireContext(), "Transaction edited successfully", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prefsHelper = PrefsHelper(requireContext())
        notificationHelper = NotificationHelper(requireContext())
        transactions = prefsHelper.getTransactions().toMutableList()

        setupUI(view)
        checkBudgetStatus()
    }

    private fun setupUI(view: View) {
        val welcomeText: TextView = view.findViewById(R.id.welcomeText)
        val usernameText: TextView = view.findViewById(R.id.usernameText)
        val totalBalanceText: TextView = view.findViewById(R.id.totalBalanceText)
        val incomeText: TextView = view.findViewById(R.id.incomeText)
        val expenditureText: TextView = view.findViewById(R.id.expenditureText)
        val transactionRecycler: RecyclerView = view.findViewById(R.id.transactionRecycler)
        val addTransactionBtn: FloatingActionButton = view.findViewById(R.id.addTransactionFab)
        val setBudgetButton: Button = view.findViewById(R.id.setBudgetButton)

        // Set username and account number
        val username = prefsHelper.getUsername()
        welcomeText.text = "$username Welcome Back!"
        usernameText.text = username

        // Setup RecyclerView and adapter first
        adapter = TransactionAdapter(
            transactions,
            prefsHelper.getCurrency(),
            onDeleteClick = { position ->
                if (position in transactions.indices) {
                    transactions.removeAt(position)
                    prefsHelper.saveTransactions(transactions)
                    adapter.notifyItemRemoved(position)
                    checkBudgetStatus()
                    updateBalanceAndTransactions(totalBalanceText, incomeText, expenditureText)
                    // Show Toast for successful deletion
                    Toast.makeText(requireContext(), "Transaction deleted successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Error deleting transaction", Toast.LENGTH_SHORT).show()
                }
            },
            onEditClick = { transaction, position ->
                startEditTransactionActivity(transaction, position)
            }
        )
        transactionRecycler.layoutManager = LinearLayoutManager(requireContext())
        transactionRecycler.adapter = adapter

        // Now update balance, income, and expenditure
        updateBalanceAndTransactions(totalBalanceText, incomeText, expenditureText)

        // FAB listener
        addTransactionBtn.setOnClickListener {
            (requireActivity() as MainActivity).startAddTransactionActivity()
        }

        // Set Budget button listener
        setBudgetButton.setOnClickListener {
            showBudgetDialog(totalBalanceText, incomeText, expenditureText)
        }
    }

    private fun showBudgetDialog(
        totalBalanceText: TextView,
        incomeText: TextView,
        expenditureText: TextView
    ) {
        // Create an EditText for the dialog
        val editText = EditText(requireContext()).apply {
            hint = "Enter your budget (e.g., 1000)"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            setText(prefsHelper.getMonthlyBudget()?.toString() ?: "")
        }

        // Create the dialog
        AlertDialog.Builder(requireContext())
            .setTitle("Set Monthly Budget")
            .setMessage("Enter your monthly budget in ${prefsHelper.getCurrency()}")
            .setView(editText)
            .setPositiveButton("Save") { _, _ ->
                val budgetText = editText.text.toString().trim()
                val budget = budgetText.toDoubleOrNull()

                if (budget == null || budget < 0) {
                    Toast.makeText(requireContext(), "Please enter a valid budget", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                // Save the budget using PrefsHelper
                prefsHelper.saveMonthlyBudget(budget)
                // Update the UI
                updateBalanceAndTransactions(totalBalanceText, incomeText, expenditureText)
                checkBudgetStatus()
                Toast.makeText(requireContext(), "Budget set to ${prefsHelper.getCurrency()} $budget", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateBalanceAndTransactions(
        totalBalanceText: TextView,
        incomeText: TextView,
        expenditureText: TextView
    ) {
        val budget = prefsHelper.getMonthlyBudget() ?: 0.0
        val expenses = transactions.filter { it.isExpense }.sumOf { it.amount }
        val income = transactions.filter { !it.isExpense }.sumOf { it.amount }
        val balance = budget + income - expenses

        totalBalanceText.text = "${String.format("%,.0f", balance)} ${prefsHelper.getCurrency()}"
        incomeText.text = "${income.toInt()} ${prefsHelper.getCurrency()}"
        expenditureText.text = "${expenses.toInt()} ${prefsHelper.getCurrency()}"

        // Update the current budget text
        val currentBudgetText = view?.findViewById<TextView>(R.id.currentBudgetText)
        currentBudgetText?.text = if (budget > 0) {
            "Current Budget: ${prefsHelper.getCurrency()} ${String.format("%,.0f", budget)}"
        } else {
            "Current Budget: Not Set"
        }

        adapter.updateTransactions(transactions)
    }

    private fun checkBudgetStatus() {
        val budget = prefsHelper.getMonthlyBudget() ?: 0.0
        val spent = transactions.filter { it.isExpense }.sumOf { it.amount }
        if (budget > 0) {
            if (spent >= budget) {
                notificationHelper.sendBudgetAlert("You have exceeded your budget of ${prefsHelper.getCurrency()} $budget!")
            } else if (spent >= budget * 0.9) {
                notificationHelper.sendBudgetAlert("You are nearing your budget limit of ${prefsHelper.getCurrency()} $budget!")
            }
        }
    }

    private fun generateAccountNumber(): String {
        val random = Random()
        return "${random.nextInt(10000)}${random.nextInt(10000)}"
    }

    private fun startEditTransactionActivity(transaction: Transaction, position: Int) {
        val intent = Intent(requireContext(), EditTransactionActivity::class.java)
        intent.putExtra(EditTransactionActivity.EXTRA_TRANSACTION, transaction)
        intent.putExtra(EditTransactionActivity.EXTRA_POSITION, position)
        editTransactionLauncher.launch(intent)
    }

    fun refreshData() {
        transactions.clear()
        transactions.addAll(prefsHelper.getTransactions())
        val totalBalanceText = view?.findViewById<TextView>(R.id.totalBalanceText)
        val incomeText = view?.findViewById<TextView>(R.id.incomeText)
        val expenditureText = view?.findViewById<TextView>(R.id.expenditureText)
        if (totalBalanceText != null && incomeText != null && expenditureText != null) {
            updateBalanceAndTransactions(totalBalanceText, incomeText, expenditureText)
        }
        checkBudgetStatus()
    }
}