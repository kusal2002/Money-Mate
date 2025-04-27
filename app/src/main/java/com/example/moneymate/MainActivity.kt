package com.example.moneymate

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    private lateinit var navController: NavController

    // Activity Result Launcher for adding a transaction
    private val addTransactionLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            onResume()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        createNotificationChannel()

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainer) as? NavHostFragment
            ?: throw IllegalStateException("NavHostFragment not found with ID fragmentContainer")

        navController = navHostFragment.navController

        val bottomNavigation: BottomNavigationView = findViewById(R.id.bottomNavigation)
        bottomNavigation.setupWithNavController(navController)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "MoneyMate Notifications"
            val descriptionText = "Notifications for MoneyMate app actions"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("MoneyMateChannel", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun startAddTransactionActivity() {
        val intent = Intent(this, AddTransactionActivity::class.java)
        addTransactionLauncher.launch(intent)
    }

    override fun onResume() {
        super.onResume()
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainer)?.childFragmentManager?.fragments?.firstOrNull()
        when (currentFragment) {
            is HomeFragment -> currentFragment.refreshData()
            is TransactionsFragment -> currentFragment.refreshData()
            is StatisticsFragment -> currentFragment.refreshData()
        }
    }
}