    package com.example.moneymate

    import android.content.Context
    import android.content.SharedPreferences
    import com.google.gson.Gson
    import com.google.gson.reflect.TypeToken

    class PrefsHelper(context: Context) {
        private val prefs: SharedPreferences = context.getSharedPreferences("MoneyMatePrefs", Context.MODE_PRIVATE)
        private val gson = Gson()

        companion object {
            private const val TRANSACTIONS_KEY = "transactions"
            private const val USERNAME_KEY = "username"
            private const val PASSWORD_KEY = "password"
            private const val CURRENCY_KEY = "currency"
            private const val BUDGET_KEY = "budget"
            private const val NOTIFICATION_KEY = "notification"
            private const val PROFILE_PICTURE_KEY = "profile_picture"
            private const val BACKUP_KEY = "backup_data"
            private const val LOGGED_IN_KEY = "logged_in"
            private const val PHONE_KEY = "phone"
            private const val EMAIL_KEY = "email"
        }

        fun getTransactions(): List<Transaction> {
            val json = prefs.getString(TRANSACTIONS_KEY, null) ?: return emptyList()
            val type = object : TypeToken<List<Transaction>>() {}.type
            return gson.fromJson(json, type) ?: emptyList()
        }

        fun saveTransactions(transactions: List<Transaction>) {
            val json = gson.toJson(transactions)
            prefs.edit().putString(TRANSACTIONS_KEY, json).apply()
        }

        fun getUsername(): String {
            return prefs.getString(USERNAME_KEY, "John Smith") ?: "John Smith"
        }

        fun saveUsername(username: String) {
            prefs.edit().putString(USERNAME_KEY, username).apply()
        }

        fun getPassword(): String {
            return prefs.getString(PASSWORD_KEY, "") ?: ""
        }

        fun savePassword(password: String) {
            prefs.edit().putString(PASSWORD_KEY, password).apply()
        }

        fun getEmail(): String? {
            return prefs.getString(EMAIL_KEY, null)
        }

        fun saveEmail(email: String?) {
            prefs.edit().putString(EMAIL_KEY, email).apply()
        }

        fun getCurrency(): String {
            return prefs.getString(CURRENCY_KEY, "USD") ?: "USD"
        }

        fun setCurrency(currency: String) {
            prefs.edit().putString(CURRENCY_KEY, currency).apply()
        }

        fun getMonthlyBudget(): Double? {
            return if (prefs.contains(BUDGET_KEY)) prefs.getFloat(BUDGET_KEY, 0f).toDouble() else null
        }

        fun saveMonthlyBudget(budget: Double) {
            prefs.edit().putFloat(BUDGET_KEY, budget.toFloat()).apply()
        }

        fun isNotificationsEnabled(): Boolean {
            return prefs.getBoolean(NOTIFICATION_KEY, false)
        }

        fun setNotificationsEnabled(enabled: Boolean) {
            prefs.edit().putBoolean(NOTIFICATION_KEY, enabled).apply()
        }

        fun getProfilePictureUri(): String? {
            return prefs.getString(PROFILE_PICTURE_KEY, null)
        }

        fun setProfilePictureUri(uri: String?) {
            prefs.edit().putString(PROFILE_PICTURE_KEY, uri).apply()
        }

        fun isLoggedIn(): Boolean {
            return prefs.getBoolean(LOGGED_IN_KEY, false)
        }

        fun setLoggedIn(loggedIn: Boolean) {
            prefs.edit().putBoolean(LOGGED_IN_KEY, loggedIn).apply()
        }

        // Add clearLoginState method to only reset the logged-in state
        fun clearLoginState() {
            prefs.edit().putBoolean(LOGGED_IN_KEY, false).apply()
        }

        fun backupData() {
            val data = mapOf(
                TRANSACTIONS_KEY to prefs.getString(TRANSACTIONS_KEY, null),
                USERNAME_KEY to getUsername(),
                PASSWORD_KEY to getPassword(),
                CURRENCY_KEY to getCurrency(),
                BUDGET_KEY to getMonthlyBudget(),
                NOTIFICATION_KEY to isNotificationsEnabled(),
                PROFILE_PICTURE_KEY to getProfilePictureUri(),
                LOGGED_IN_KEY to isLoggedIn(),
                EMAIL_KEY to getEmail()
            )
            val json = gson.toJson(data)
            prefs.edit().putString(BACKUP_KEY, json).apply()
        }

        fun getBackupDataAsJson(): String? {
            backupData()
            return prefs.getString(BACKUP_KEY, null)
        }

        fun restoreData(): Boolean {
            val json = prefs.getString(BACKUP_KEY, null) ?: return false
            return restoreDataFromJson(json)
        }

        fun restoreDataFromJson(json: String): Boolean {
            val type = object : TypeToken<Map<String, Any>>() {}.type
            val data: Map<String, Any> = gson.fromJson(json, type) ?: return false

            with(prefs.edit()) {
                data[TRANSACTIONS_KEY]?.let { putString(TRANSACTIONS_KEY, it as String) }
                putString(USERNAME_KEY, data[USERNAME_KEY] as String)
                putString(PASSWORD_KEY, data[PASSWORD_KEY] as String)
                putString(CURRENCY_KEY, data[CURRENCY_KEY] as String)
                putFloat(BUDGET_KEY, (data[BUDGET_KEY] as Double).toFloat())
                putBoolean(NOTIFICATION_KEY, data[NOTIFICATION_KEY] as Boolean)
                putString(PROFILE_PICTURE_KEY, data[PROFILE_PICTURE_KEY] as String?)
                putBoolean(LOGGED_IN_KEY, data[LOGGED_IN_KEY] as Boolean)
                putString(PHONE_KEY, data[PHONE_KEY] as String?)
                putString(EMAIL_KEY, data[EMAIL_KEY] as String?)
                apply()
            }
            return true
        }

        fun clear() {
            prefs.edit().clear().apply()
        }

        fun getBudget(): Double {
            return prefs.getFloat(BUDGET_KEY, 0f).toDouble()
        }

        fun setBudget(budget: Double) {
            prefs.edit().putFloat(BUDGET_KEY, budget.toFloat()).apply()
        }

        fun isDailyExpenseReminderEnabled(): Boolean {
            return prefs.getBoolean("daily_expense_reminder_enabled", false)
        }

        fun setDailyExpenseReminderEnabled(enabled: Boolean) {
            prefs.edit().putBoolean("daily_expense_reminder_enabled", enabled).apply()
        }
    }