package com.example.moneymate

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.*

class AddTransactionActivity : AppCompatActivity() {
    private lateinit var prefsHelper: PrefsHelper
    private lateinit var nameEditText: TextInputEditText // Added for transaction name
    private lateinit var amountEditText: TextInputEditText
    private lateinit var categorySpinner: Spinner
    private lateinit var dateEditText: TextInputEditText
    private lateinit var expenseRadio: RadioButton
    private lateinit var incomeRadio: RadioButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_transaction)

        prefsHelper = PrefsHelper(this)

        nameEditText = findViewById(R.id.nameEditText) // Initialize the name field
        amountEditText = findViewById(R.id.amountEditText)
        categorySpinner = findViewById(R.id.categorySpinner)
        dateEditText = findViewById(R.id.dateEditText)
        expenseRadio = findViewById(R.id.expenseRadio)
        incomeRadio = findViewById(R.id.incomeRadio)
        val saveButton: Button = findViewById(R.id.saveButton)

        // Setup category spinner
        val categories = arrayOf("Food", "Social", "Traffic", "Shopping", "Grocery", "Education", "Medical", "Investment", "Gift", "Rentals", "Other")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = adapter

        // Setup date picker
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        dateEditText.setText(dateFormat.format(calendar.time))

        dateEditText.setOnClickListener {
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                calendar.set(selectedYear, selectedMonth, selectedDay)
                dateEditText.setText(dateFormat.format(calendar.time))
            }, year, month, day)
            datePickerDialog.show()
        }

        // Save button listener
        saveButton.setOnClickListener {
            saveTransaction()
        }
    }

    private fun saveTransaction() {
        val name = nameEditText.text.toString() // Get the transaction name
        val amountText = amountEditText.text.toString()

        if (name.isEmpty()) {
            nameEditText.error = "Please enter a transaction name"
            return
        }

        if (amountText.isEmpty()) {
            amountEditText.error = "Please enter an amount"
            return
        }

        val amount = amountText.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            amountEditText.error = "Please enter a valid amount"
            return
        }

        val category = categorySpinner.selectedItem.toString()
        val date = dateEditText.text.toString()
        val isExpense = expenseRadio.isChecked

        val transaction = Transaction(amount, category, date, isExpense, name) // Include name in the transaction
        val transactions = prefsHelper.getTransactions().toMutableList()
        transactions.add(transaction)
        prefsHelper.saveTransactions(transactions)

        Toast.makeText(this, "Transaction saved", Toast.LENGTH_SHORT).show()
        setResult(RESULT_OK)
        finish()
    }
}