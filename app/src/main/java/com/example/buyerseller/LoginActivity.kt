package com.example.buyerseller

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.buyerseller.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase instances
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        // Set up button and text view click listeners
        binding.btnLogin.setOnClickListener {
            handleLogin()
        }

        binding.tvGoToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    // The onStart() method has been removed to disable auto-login.

    private fun handleLogin() {
        if (!validateInput()) {
            return // Stop the login process if validation fails
        }

        val email = binding.etLoginEmail.text.toString().trim()
        val password = binding.etLoginPassword.text.toString().trim()

        showLoading(true) // Show progress bar
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                val uid = auth.currentUser?.uid
                if (uid != null) {
                    checkUserRole(uid)
                } else {
                    showLoading(false)
                    Toast.makeText(this, "Login failed. Please try again.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                showLoading(false)
                Toast.makeText(this, "Login failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun validateInput(): Boolean {
        // Clear previous errors
        binding.tilEmail.error = null
        binding.tilPassword.error = null

        val email = binding.etLoginEmail.text.toString().trim()
        val password = binding.etLoginPassword.text.toString().trim()

        if (email.isEmpty()) {
            binding.tilEmail.error = "Email is required"
            return false
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = "Please enter a valid email address"
            return false
        }

        if (password.isEmpty()) {
            binding.tilPassword.error = "Password is required"
            return false
        }

        return true
    }

    private fun checkUserRole(uid: String) {
        database.child("users").child(uid).child("role")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val role = snapshot.getValue(String::class.java)

                    // Determine which activity to start based on the user's role
                    val intent = when (role) {
                        "buyer" -> Intent(this@LoginActivity, BuyerHomeActivity::class.java)
                        "seller" -> Intent(this@LoginActivity, SellerHomeActivity::class.java)
                        else -> {
                            // Handle cases where role is null or not defined
                            Toast.makeText(this@LoginActivity, "User role not found. Please contact support.", Toast.LENGTH_LONG).show()
                            null
                        }
                    }

                    if (intent != null) {
                        // Clear the back stack so the user can't navigate back to the login screen
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish() // Finish LoginActivity
                    } else {
                        showLoading(false)
                        auth.signOut() // Sign out user if their role is invalid
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    showLoading(false)
                    Toast.makeText(this@LoginActivity, "Database error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    // A helper function to manage the loading state of the UI
    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnLogin.isEnabled = !isLoading
        binding.etLoginEmail.isEnabled = !isLoading
        binding.etLoginPassword.isEnabled = !isLoading
    }
}