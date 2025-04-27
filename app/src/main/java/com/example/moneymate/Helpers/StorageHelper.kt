package com.example.moneymate

import android.content.Context
import android.net.Uri
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.InputStreamReader
import java.io.OutputStreamWriter

class StorageHelper(private val context: Context) {
    private val prefsHelper = PrefsHelper(context)
    private val gson = Gson()

    // Data class to hold all app data for backup/restore
    private data class AppData(
        val budget: Double,
        val currency: String,
        val transactions: List<Transaction>,
        val isLoggedIn: Boolean,
        val username: String
    )

    fun backupData(uri: Uri) {
        try {
            // Gather all data to back up
            val appData = AppData(
                budget = prefsHelper.getBudget(),
                currency = prefsHelper.getCurrency(),
                transactions = prefsHelper.getTransactions(),
                isLoggedIn = prefsHelper.isLoggedIn(),
                username = prefsHelper.getUsername()
            )

            // Convert to JSON
            val json = gson.toJson(appData)

            // Write to the file specified by the Uri
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                OutputStreamWriter(outputStream).use { writer ->
                    writer.write(json)
                    writer.flush()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw RuntimeException("Failed to backup data: ${e.message}")
        }
    }

    fun restoreData(uri: Uri) {
        try {
            // Read JSON from the file specified by the Uri
            val json = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                InputStreamReader(inputStream).use { reader ->
                    reader.readText()
                }
            } ?: throw RuntimeException("Failed to read backup file")

            // Parse JSON into AppData
            val appData = gson.fromJson(json, AppData::class.java)

            // Restore data to SharedPreferences
            prefsHelper.setBudget(appData.budget)
            prefsHelper.setCurrency(appData.currency)
            prefsHelper.saveTransactions(appData.transactions)
            prefsHelper.setLoggedIn(appData.isLoggedIn)
            prefsHelper.saveUsername(appData.username)
        } catch (e: Exception) {
            e.printStackTrace()
            throw RuntimeException("Failed to restore data: ${e.message}")
        }
    }
}