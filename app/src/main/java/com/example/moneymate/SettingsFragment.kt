package com.example.moneymate

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
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

        // General Settings button (now a CardView)
        val generalSettingsButton: CardView = view.findViewById(R.id.generalSettingsButton)
        generalSettingsButton.setOnClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_generalSettingFragment)
        }

        // Edit Profile button (now a CardView)
        val editProfileButton: CardView = view.findViewById(R.id.editProfileButton)
        editProfileButton.setOnClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_editProfileFragment)
        }

        // Logout button (now a CardView)
        val logoutButton: CardView = view.findViewById(R.id.logoutButton)
        logoutButton.setOnClickListener {
            // Clear the logged-in state
            prefsHelper.clearLoginState()
            // Navigate to LoginActivity and clear the activity stack
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish()
        }
    }
}