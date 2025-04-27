package com.example.moneymate

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputEditText
import java.util.Calendar

class GeneralSettingFragment : Fragment() {
    private lateinit var prefsHelper: PrefsHelper
    private lateinit var currencySpinner: Spinner
    private lateinit var budgetEditText: TextInputEditText
    private lateinit var notificationSwitch: Switch
    private lateinit var expenseReminderSwitch: Switch

    // Activity Result Launcher for SAF to create a file (export)
    private val exportDataLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        uri?.let { documentUri ->
            try {
                val jsonData = prefsHelper.getBackupDataAsJson()
                if (jsonData != null) {
                    requireContext().contentResolver.openOutputStream(documentUri)?.use { outputStream ->
                        outputStream.write(jsonData.toByteArray())
                        outputStream.flush()
                    }
                    Toast.makeText(requireContext(), "Data exported successfully", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(requireContext(), "No data to export", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Failed to export data: ${e.message}", Toast.LENGTH_LONG).show()
            }
        } ?: run {
            Toast.makeText(requireContext(), "Export cancelled", Toast.LENGTH_SHORT).show()
        }
    }

    // Activity Result Launcher for SAF to open a file (import)
    private val importDataLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { documentUri ->
            try {
                val jsonData = requireContext().contentResolver.openInputStream(documentUri)?.use { inputStream ->
                    inputStream.bufferedReader().use { it.readText() }
                }
                if (jsonData != null && prefsHelper.restoreDataFromJson(jsonData)) {
                    // Refresh UI after import
                    budgetEditText.setText(prefsHelper.getMonthlyBudget()?.toString() ?: "")
                    val currencies = arrayOf("USD", "EUR", "GBP", "INR", "JPY", "LKR")
                    currencySpinner.setSelection(currencies.indexOf(prefsHelper.getCurrency()))
                    notificationSwitch.isChecked = prefsHelper.isNotificationsEnabled()
                    expenseReminderSwitch.isChecked = prefsHelper.isDailyExpenseReminderEnabled()
                    Toast.makeText(requireContext(), "Data imported successfully", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(requireContext(), "Failed to import data: Invalid file", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Failed to import data: ${e.message}", Toast.LENGTH_LONG).show()
            }
        } ?: run {
            Toast.makeText(requireContext(), "Import cancelled", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_general_setting, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prefsHelper = PrefsHelper(requireContext())

        // Back Button
        val backButton: ImageView = view.findViewById(R.id.backButton)
        backButton.setOnClickListener {
            findNavController().navigate(R.id.action_generalSettingFragment_to_settingsFragment)
        }

        // Currency Spinner
        currencySpinner = view.findViewById(R.id.currencySpinner)
        val currencies = arrayOf("USD", "EUR", "GBP", "INR", "JPY", "LKR")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, currencies)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        currencySpinner.adapter = adapter
        val savedCurrency = prefsHelper.getCurrency() ?: "USD"
        currencySpinner.setSelection(currencies.indexOf(savedCurrency))

        // Monthly Budget
        budgetEditText = view.findViewById(R.id.budgetEditText)
        budgetEditText.setText(prefsHelper.getMonthlyBudget()?.toString() ?: "")

        // Save Budget Button
        val saveBudgetButton: TextView = view.findViewById(R.id.saveBudgetButton)
        saveBudgetButton.setOnClickListener {
            val budgetText = budgetEditText.text.toString()
            if (budgetText.isNotEmpty()) {
                try {
                    val budget = budgetText.toDouble()
                    if (budget < 0) {
                        Toast.makeText(requireContext(), "Budget cannot be negative", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                    prefsHelper.saveMonthlyBudget(budget)
                    Toast.makeText(requireContext(), "Budget saved successfully", Toast.LENGTH_SHORT).show()
                } catch (e: NumberFormatException) {
                    Toast.makeText(requireContext(), "Please enter a valid number", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "Please enter a budget", Toast.LENGTH_SHORT).show()
            }
        }

        // Notifications Switch
        notificationSwitch = view.findViewById(R.id.notificationSwitch)
        notificationSwitch.isChecked = prefsHelper.isNotificationsEnabled()
        notificationSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefsHelper.setNotificationsEnabled(isChecked)
            Toast.makeText(requireContext(), "Notifications ${if (isChecked) "enabled" else "disabled"}", Toast.LENGTH_SHORT).show()
        }

        // Daily Expense Reminder Switch
        expenseReminderSwitch = view.findViewById(R.id.expenseReminderSwitch)
        expenseReminderSwitch.isChecked = prefsHelper.isDailyExpenseReminderEnabled()
        expenseReminderSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefsHelper.setDailyExpenseReminderEnabled(isChecked)
            if (isChecked) {
                scheduleDailyReminder()
                Toast.makeText(requireContext(), "Daily expense reminder enabled", Toast.LENGTH_SHORT).show()
            } else {
                cancelDailyReminder()
                Toast.makeText(requireContext(), "Daily expense reminder disabled", Toast.LENGTH_SHORT).show()
            }
        }

        // Export Data
        val exportButton: TextView = view.findViewById(R.id.exportButton)
        exportButton.setOnClickListener {
            exportData()
        }

        // Import Data
        val importButton: TextView = view.findViewById(R.id.importButton)
        importButton.setOnClickListener {
            importData()
        }

        // Currency Spinner Listener
        currencySpinner.setOnItemSelectedListener(object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>, view: android.view.View?, position: Int, id: Long) {
                val selectedCurrency = currencies[position]
                prefsHelper.setCurrency(selectedCurrency)
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>) {}
        })
    }

    private fun exportData() {
        exportDataLauncher.launch("moneymate_backup.json")
    }

    private fun importData() {
        importDataLauncher.launch(arrayOf("application/json"))
    }

    private fun scheduleDailyReminder() {
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(requireContext(), ExpenseReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Set the reminder to trigger at 8:00 PM daily
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 20) // 8:00 PM
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            if (timeInMillis < System.currentTimeMillis()) {
                // If the time is in the past for today, schedule for tomorrow
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }

        // Schedule the alarm to repeat daily
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    private fun cancelDailyReminder() {
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(requireContext(), ExpenseReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}