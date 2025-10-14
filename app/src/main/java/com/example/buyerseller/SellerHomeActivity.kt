package com.example.buyerseller

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.buyerseller.databinding.ActivitySellerHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class SellerHomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySellerHomeBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    private var tapCount = 0
    private val REQUIRED_TAPS = 7

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySellerHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        // Load data and set up UI components
        loadUserProfile()
        setupClickListeners()
        setupRecyclerView()
    }

    private fun loadUserProfile() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            goToLogin()
            return
        }

        val uid = currentUser.uid
        database.child("users").child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val name = snapshot.child("name").getValue(String::class.java)
                binding.tvWelcomeUser.text = if (!name.isNullOrEmpty()) {
                    "Welcome back, $name!"
                } else {
                    "Welcome, Seller!" // Fallback
                }
            }

            override fun onCancelled(error: DatabaseError) {
                binding.tvWelcomeUser.text = "Welcome, Seller!"
                Toast.makeText(this@SellerHomeActivity, "Failed to load user data.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupClickListeners() {
        // Navigate to Profile
        binding.ivProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        // Easter Egg Logic
        binding.welcomeCard.setOnClickListener {
            tapCount++
            if (tapCount >= REQUIRED_TAPS) {
                binding.tvEasterEggSeller.visibility = View.VISIBLE
                Toast.makeText(this, "Secret unlocked!", Toast.LENGTH_SHORT).show()
                tapCount = 0
            }
        }

        // Add a new product
        binding.fabAddProduct.setOnClickListener {
            // TODO: Create an AddProductActivity and start it here
            // startActivity(Intent(this, AddProductActivity::class.java))
            Toast.makeText(this, "Add new product clicked!", Toast.LENGTH_SHORT).show()
        }

        // Logout
        binding.btnLogout.setOnClickListener {
            auth.signOut()
            goToLogin()
        }
    }

    private fun setupRecyclerView() {
        // TODO: Implement your RecyclerView adapter to show the seller's products
        // You would fetch products from Firebase where the seller's UID matches
    }

    private fun goToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}