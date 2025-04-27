package com.example.moneymate

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.RadioButton
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class EditTransactionActivity : AppCompatActivity() {
    companion object {
        const val EXTRA_TRANSACTION = "extra_transaction"
        const val EXTRA_POSITION = "extra_position"
    }

    private lateinit var nameEditText: TextInputEditText
    private lateinit var amountEditText: TextInputEditText
    private lateinit var categorySpinner: Spinner
    private lateinit var dateEditText: TextInputEditText
    private lateinit var expenseRadio: RadioButton
    private lateinit var incomeRadio: RadioButton
    private lateinit var saveButton: Button
    private lateinit var calendar: Calendar
    private lateinit var dateFormat: SimpleDateFormat // Moved to class level
    private var transaction: Transaction? = null
    private var position: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_transaction)

        nameEditText = findViewById(R.id.nameEditText)
        amountEditText = findViewById(R.id.amountEditText)
        categorySpinner = findViewById(R.id.categorySpinner)
        dateEditText = findViewById(R.id.dateEditText)
        expenseRadio = findViewById(R.id.expenseRadio)
        incomeRadio = findViewById(R.id.incomeRadio)
        saveButton = findViewById(R.id.saveButton)

        // Initialize dateFormat
        dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        // Retrieve the transaction and position from the intent
        transaction = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(EXTRA_TRANSACTION, Transaction::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(EXTRA_TRANSACTION)
        }
        position = intent.getIntExtra(EXTRA_POSITION, -1)

        if (transaction == null || position == -1) {
            Toast.makeText(this, "Error: Transaction not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Setup category spinner
        val categories = arrayOf("Food", "Social", "Traffic", "Shopping", "Grocery", "Education", "Medical", "Investment", "Gift", "Rentals", "Other")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = adapter

        // Pre-fill the form with transaction details
        nameEditText.setText(transaction!!.name)
        amountEditText.setText(transaction!!.amount.toString())
        categorySpinner.setSelection(categories.indexOf(transaction!!.category))
        dateEditText.setText(transaction!!.date)
        if (transaction!!.isExpense) {
            expenseRadio.isChecked = true
        } else {
            incomeRadio.isChecked = true
        }

        // Parse the date to initialize the calendar
        calendar = Calendar.getInstance()
        try {
            val date = dateFormat.parse(transaction!!.date)
            if (date != null) {
                calendar.time = date
            }
        } catch (e: Exception) {
            calendar = Calendar.getInstance()
        }

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

        saveButton.setOnClickListener {
            saveTransaction()
        }
    }

    private fun saveTransaction() {
        val name = nameEditText.text.toString()
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

        val updatedTransaction = Transaction(amount, category, date, isExpense, name)

        val resultIntent = Intent()
        resultIntent.putExtra(EXTRA_TRANSACTION, updatedTransaction)
        resultIntent.putExtra(EXTRA_POSITION, position)
        setResult(RESULT_OK, resultIntent)
        finish()
    }
}