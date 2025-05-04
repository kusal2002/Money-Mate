package com.example.moneymate

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.text.SimpleDateFormat
import java.util.*

class StatisticsFragment : Fragment() {
    private lateinit var prefsHelper: PrefsHelper
    private lateinit var transactions: List<Transaction>
    private val categories = listOf(
        "Food", "Social", "Traffic", "Shopping", "Grocery",
        "Education", "Medical", "Investment", "Gift", "Rentals", "Other"
    )
    private val categoryIcons = mapOf(
        "Shopping" to android.R.drawable.ic_menu_add,
        "Food" to android.R.drawable.ic_menu_share,
        "Entertainment" to android.R.drawable.ic_menu_slideshow,
        "Social" to android.R.drawable.ic_menu_myplaces,
        "Traffic" to android.R.drawable.ic_menu_directions,
        "Grocery" to android.R.drawable.ic_menu_preferences,
        "Education" to android.R.drawable.ic_menu_edit,
        "Medical" to android.R.drawable.ic_menu_help,
        "Investment" to android.R.drawable.ic_menu_info_details,
        "Gift" to android.R.drawable.ic_menu_send,
        "Rentals" to android.R.drawable.ic_menu_mapmode,
        "Other" to android.R.drawable.ic_menu_search
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_statistics, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prefsHelper = PrefsHelper(requireContext())
        transactions = prefsHelper.getTransactions()

        val barChart: BarChart = view.findViewById(R.id.barChart)
        val incomeText: TextView = view.findViewById(R.id.incomeText)
        val expenseText: TextView = view.findViewById(R.id.expenseText)
        val categoryRecycler: RecyclerView = view.findViewById(R.id.categoryRecycler)

        // Setup RecyclerView for categories
        categoryRecycler.layoutManager = LinearLayoutManager(context)
        val categoryAdapter = CategoryAdapter(emptyList())
        categoryRecycler.adapter = categoryAdapter

        // Update data
        updateStatistics(barChart, incomeText, expenseText, categoryAdapter)
    }

    private fun updateStatistics(
        barChart: BarChart,
        incomeText: TextView,
        expenseText: TextView,
        categoryAdapter: CategoryAdapter
    ) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()

        // Get the past 7 days, including today
        val dailyIncome = FloatArray(7) { 0f }
        val dailyExpenses = FloatArray(7) { 0f }
        val days = arrayOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        val dayLabels = mutableListOf<String>()

        // Start from today and go back 6 days
        for (i in 0 until 7) {
            val dateStr = dateFormat.format(calendar.time)
            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
            dayLabels.add(days[dayOfWeek - 1]) // Calendar.DAY_OF_WEEK: 1 (Sunday) to 7 (Saturday)

            transactions.filter { it.date == dateStr }.forEach { transaction ->
                if (transaction.isExpense) {
                    dailyExpenses[i] += transaction.amount.toFloat()
                } else {
                    dailyIncome[i] += transaction.amount.toFloat()
                }
            }
            calendar.add(Calendar.DAY_OF_YEAR, -1)
        }

        // Total income and expenses
        val totalIncome = transactions.filter { !it.isExpense }.sumOf { it.amount }
        val totalExpenses = transactions.filter { it.isExpense }.sumOf { it.amount }

        // Use the selected currency from PrefsHelper
        val currency = prefsHelper.getCurrency()
        incomeText.text = "${String.format("%.2f", totalIncome)} $currency"
        expenseText.text = "${String.format("%.2f", totalExpenses)} $currency"

        // Setup bar chart
        val incomeEntries = dailyIncome.mapIndexed { index, value -> BarEntry(index.toFloat(), value) }
        val expenseEntries = dailyExpenses.mapIndexed { index, value -> BarEntry(index.toFloat(), value) }

        val incomeDataSet = BarDataSet(incomeEntries, "Income").apply {
            color = 0xFF00C4B4.toInt() // Teal color for income
        }
        val expenseDataSet = BarDataSet(expenseEntries, "Expenses").apply {
            color = 0xFF1976D2.toInt() // Blue color for expenses
        }

        val barData = BarData(incomeDataSet, expenseDataSet).apply {
            barWidth = 0.3f
        }
        barChart.data = barData

        // Customize X-axis
        val xAxis = barChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(dayLabels.reversed().toTypedArray())
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.labelCount = 7

        // Customize Y-axis
        barChart.axisLeft.setDrawGridLines(false)
        barChart.axisRight.isEnabled = false
        barChart.description.isEnabled = false
        barChart.legend.isEnabled = true // Enable legend to distinguish income vs expenses
        barChart.setFitBars(true)
        barChart.invalidate()

        // Update categories
        val categorySummaries = categories.map { category ->
            val categoryTransactions = transactions.filter { it.category == category && it.isExpense }
            val totalAmount = categoryTransactions.sumOf { it.amount }
            val iconResId = categoryIcons[category] ?: android.R.drawable.ic_menu_info_details
            CategorySummary(category, categoryTransactions.size, totalAmount, iconResId)
        }.sortedByDescending { it.totalAmount }

        categoryAdapter.updateCategories(categorySummaries)
    }

    fun refreshData() {
        transactions = prefsHelper.getTransactions()
        val barChart = view?.findViewById<BarChart>(R.id.barChart)
        val incomeText = view?.findViewById<TextView>(R.id.incomeText)
        val expenseText = view?.findViewById<TextView>(R.id.expenseText)
        val categoryRecycler = view?.findViewById<RecyclerView>(R.id.categoryRecycler)

        if (barChart != null && incomeText != null && expenseText != null && categoryRecycler != null) {
            val categoryAdapter = categoryRecycler.adapter as CategoryAdapter
            updateStatistics(barChart, incomeText, expenseText, categoryAdapter)
        }
    }
}