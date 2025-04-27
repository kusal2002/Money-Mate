package com.example.moneymate

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class SettingsFragment : Fragment() {
    private lateinit var prefsHelper: PrefsHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prefsHelper = PrefsHelper(requireContext())

        val editProfileButton: TextView = view.findViewById(R.id.editProfileButton)
        val generalSettingButton: TextView = view.findViewById(R.id.budgetSettingButton)
        val logoutButton: TextView = view.findViewById(R.id.logoutButton)
        val backButton: ImageView = view.findViewById(R.id.backButton)

        backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        editProfileButton.setOnClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_editProfileFragment)
        }

        generalSettingButton.setOnClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_generalSettingFragment)
        }

        logoutButton.setOnClickListener {
            prefsHelper.clearLoginState() // Only clear the login state, preserve user data
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show()
        }
    }

    fun refreshData() {
        // No data to refresh in this fragment
    }
}