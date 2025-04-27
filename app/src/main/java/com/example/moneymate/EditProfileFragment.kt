package com.example.moneymate

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputEditText

class EditProfileFragment : Fragment() {
    private lateinit var prefsHelper: PrefsHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_edit_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prefsHelper = PrefsHelper(requireContext())

        // Back Button
        val backButton: ImageView = view.findViewById(R.id.backButton)
        backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        val usernameEditText: TextInputEditText = view.findViewById(R.id.usernameEditText)
        val phoneEditText: TextInputEditText = view.findViewById(R.id.phoneEditText)
        val emailEditText: TextInputEditText = view.findViewById(R.id.emailEditText)
        val currentPasswordEditText: TextInputEditText = view.findViewById(R.id.currentPasswordEditText)
        val newPasswordEditText: TextInputEditText = view.findViewById(R.id.newPasswordEditText)
        val confirmPasswordEditText: TextInputEditText = view.findViewById(R.id.confirmPasswordEditText)
        val saveButton: TextView = view.findViewById(R.id.saveButton)

        // Pre-fill fields
        usernameEditText.setText(prefsHelper.getUsername())
        phoneEditText.setText(prefsHelper.getPhone() ?: "")
        emailEditText.setText(prefsHelper.getEmail() ?: "")

        saveButton.setOnClickListener {
            val newUsername = usernameEditText.text.toString().trim()
            val newPhone = phoneEditText.text.toString().trim()
            val newEmail = emailEditText.text.toString().trim()
            val currentPassword = currentPasswordEditText.text.toString()
            val newPassword = newPasswordEditText.text.toString()
            val confirmPassword = confirmPasswordEditText.text.toString()

            // Validate username
            if (newUsername.isEmpty()) {
                usernameEditText.error = "Username cannot be empty"
                Toast.makeText(requireContext(), "Username cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Validate email (if provided)
            if (newEmail.isNotEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
                emailEditText.error = "Invalid email address"
                Toast.makeText(requireContext(), "Invalid email address", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Validate phone (if provided)
            if (newPhone.isNotEmpty() && !android.util.Patterns.PHONE.matcher(newPhone).matches()) {
                phoneEditText.error = "Invalid phone number"
                Toast.makeText(requireContext(), "Invalid phone number", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Password change logic
            val storedPassword = prefsHelper.getPassword()
            var passwordChanged = false
            if (currentPassword.isNotEmpty() || newPassword.isNotEmpty() || confirmPassword.isNotEmpty()) {
                // All password fields must be filled if any one is filled
                if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                    Toast.makeText(requireContext(), "Please fill all password fields to change password", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // Validate current password
                if (currentPassword != storedPassword) {
                    currentPasswordEditText.error = "Current password is incorrect"
                    Toast.makeText(requireContext(), "Current password is incorrect", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // Validate new password length
                if (newPassword.length < 6) {
                    newPasswordEditText.error = "New password must be at least 6 characters"
                    Toast.makeText(requireContext(), "New password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // Validate confirm password
                if (newPassword != confirmPassword) {
                    confirmPasswordEditText.error = "Passwords do not match"
                    Toast.makeText(requireContext(), "Passwords do not match", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // If all validations pass, update the password
                prefsHelper.savePassword(newPassword)
                passwordChanged = true
            }

            // Save other profile details
            prefsHelper.saveUsername(newUsername)
            prefsHelper.savePhone(newPhone)
            prefsHelper.saveEmail(newEmail)

            Toast.makeText(
                requireContext(),
                if (passwordChanged) "Profile and password updated successfully" else "Profile updated successfully",
                Toast.LENGTH_SHORT
            ).show()
            findNavController().popBackStack()
        }
    }
}