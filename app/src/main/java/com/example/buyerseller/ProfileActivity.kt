package com.example.buyerseller

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.buyerseller.databinding.ActivityProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        val uid = auth.currentUser?.uid

        // Ensure user is logged in
        if (uid == null) {
            Toast.makeText(this, "User not found, please log in again.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        database = FirebaseDatabase.getInstance().getReference("users").child(uid)

        loadUserProfile()
        setupClickListeners()
    }

    private fun loadUserProfile() {
        showLoading(true)
        // Set non-editable email field from FirebaseAuth
        binding.etProfileEmail.setText(auth.currentUser?.email ?: "No email found")

        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val name = snapshot.child("name").getValue(String::class.java) ?: ""
                val phone = snapshot.child("phone").getValue(String::class.java) ?: ""
                val role = snapshot.child("role").getValue(String::class.java)?.replaceFirstChar { it.uppercase() } ?: "N/A"

                binding.etProfileName.setText(name)
                binding.etProfilePhone.setText(phone)
                binding.tvProfileRole.text = "Role: $role"
                showLoading(false)
            }

            override fun onCancelled(error: DatabaseError) {
                showLoading(false)
                Toast.makeText(this@ProfileActivity, "Error loading profile: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupClickListeners() {
        // Back button functionality
        binding.ivBack.setOnClickListener {
            finish() // Closes the current activity
        }

        // Save button functionality
        binding.btnSaveProfile.setOnClickListener {
            if (validateInput()) {
                updateUserProfile()
            }
        }
    }

    private fun validateInput(): Boolean {
        binding.tilProfileName.error = null
        binding.tilProfilePhone.error = null

        val name = binding.etProfileName.text.toString().trim()
        val phone = binding.etProfilePhone.text.toString().trim()

        if (name.isEmpty()) {
            binding.tilProfileName.error = "Name cannot be empty"
            return false
        }

        if (phone.isEmpty()) {
            binding.tilProfilePhone.error = "Phone number cannot be empty"
            return false
        }

        return true
    }

    private fun updateUserProfile() {
        showLoading(true)
        val updates = mapOf<String, Any>(
            "name" to binding.etProfileName.text.toString().trim(),
            "phone" to binding.etProfilePhone.text.toString().trim()
        )

        database.updateChildren(updates)
            .addOnSuccessListener {
                showLoading(false)
                Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                showLoading(false)
                Toast.makeText(this, "Update failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnSaveProfile.isEnabled = !isLoading
        binding.etProfileName.isEnabled = !isLoading
        binding.etProfilePhone.isEnabled = !isLoading
    }
}