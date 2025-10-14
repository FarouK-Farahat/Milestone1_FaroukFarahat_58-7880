package com.example.buyerseller

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.buyerseller.databinding.ActivityBuyerHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class BuyerHomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBuyerHomeBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    private var tapCount = 0
    private val REQUIRED_TAPS = 7

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBuyerHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        // Load user info and set up UI
        loadUserProfile()
        setupClickListeners()
        setupRecyclerView()
    }

    private fun loadUserProfile() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            // If no user is logged in, redirect to LoginActivity
            goToLogin()
            return
        }

        val uid = currentUser.uid
        database.child("users").child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Get user's name from the database
                val name = snapshot.child("name").getValue(String::class.java)
                binding.tvWelcomeUser.text = if (!name.isNullOrEmpty()) {
                    "Welcome, $name!"
                } else {
                    "Welcome, Buyer!" // Fallback text
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle potential database errors
                binding.tvWelcomeUser.text = "Welcome, Buyer!"
                Toast.makeText(this@BuyerHomeActivity, "Failed to load user data.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupClickListeners() {
        // Profile Icon Navigation
        binding.ivProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        // Easter Egg on Welcome Card
        binding.welcomeCard.setOnClickListener {
            tapCount++
            if (tapCount >= REQUIRED_TAPS) {
                binding.tvEasterEgg.visibility = View.VISIBLE
                Toast.makeText(this, "You found a secret!", Toast.LENGTH_SHORT).show()
                tapCount = 0
            }
        }

        // Logout FAB
        binding.fabLogout.setOnClickListener {
            auth.signOut() // Sign out from Firebase
            goToLogin()
        }
    }

    private fun setupRecyclerView() {
        // TODO: Here you will set up your RecyclerView Adapter with product data
        // For example:
        // val productAdapter = ProductAdapter(productList)
        // binding.rvProducts.adapter = productAdapter
        // binding.rvProducts.layoutManager = LinearLayoutManager(this)
    }

    private fun goToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}