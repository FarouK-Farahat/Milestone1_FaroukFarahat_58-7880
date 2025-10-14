package com.example.buyerseller

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.buyerseller.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

data class UserProfile(
    val uid: String = "",
    val name: String = "",
    val phone: String = "",
    val role: String = ""
)

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.tvGoToLogin.setOnClickListener {
            // Navigate to LoginActivity without adding it to the back stack
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        binding.btnRegister.setOnClickListener {
            handleRegistration()
        }
    }

    private fun handleRegistration() {
        if (!validateInput()) {
            return // Stop if validation fails
        }

        showLoading(true) // Show loading indicator

        val name = binding.etName.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val role = if (binding.rbBuyer.isChecked) "buyer" else "seller"

        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid
                if (uid == null) {
                    showLoading(false)
                    Toast.makeText(this, "Registration failed. Please try again.", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                val userProfile = UserProfile(uid, name, phone, role)
                saveUserProfile(userProfile)
            }
            .addOnFailureListener { e ->
                showLoading(false)
                Toast.makeText(this, "Registration failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveUserProfile(profile: UserProfile) {
        FirebaseDatabase.getInstance().getReference("users")
            .child(profile.uid)
            .setValue(profile)
            .addOnSuccessListener {
                Toast.makeText(this, "Registered successfully!", Toast.LENGTH_SHORT).show()

                val intent = if (profile.role == "buyer") {
                    Intent(this, BuyerHomeActivity::class.java)
                } else {
                    Intent(this, SellerHomeActivity::class.java)
                }
                // Clear back stack to prevent user from returning to registration screen
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { e ->
                showLoading(false)
                Toast.makeText(this, "Failed to save user profile: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun validateInput(): Boolean {
        // Clear previous errors
        binding.tilName.error = null
        binding.tilPhone.error = null
        binding.tilEmail.error = null
        binding.tilPassword.error = null

        val name = binding.etName.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        var isValid = true

        if (name.isEmpty()) {
            binding.tilName.error = "Name is required"
            isValid = false
        }

        if (phone.isEmpty()) {
            binding.tilPhone.error = "Phone number is required"
            isValid = false
        }

        if (email.isEmpty()) {
            binding.tilEmail.error = "Email is required"
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = "Please enter a valid email"
            isValid = false
        }

        if (password.isEmpty()) {
            binding.tilPassword.error = "Password is required"
            isValid = false
        } else if (password.length < 6) {
            binding.tilPassword.error = "Password must be at least 6 characters"
            isValid = false
        }

        if (!binding.rbBuyer.isChecked && !binding.rbSeller.isChecked) {
            Toast.makeText(this, "Please select a role (Buyer or Seller)", Toast.LENGTH_SHORT).show()
            isValid = false
        }

        return isValid
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnRegister.isEnabled = !isLoading
        binding.etName.isEnabled = !isLoading
        binding.etPhone.isEnabled = !isLoading
        binding.etEmail.isEnabled = !isLoading
        binding.etPassword.isEnabled = !isLoading
        binding.rbBuyer.isEnabled = !isLoading
        binding.rbSeller.isEnabled = !isLoading
    }
}