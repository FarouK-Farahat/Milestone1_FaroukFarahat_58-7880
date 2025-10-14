package com.example.buyerseller

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class MainActivity : AppCompatActivity() {

    override fun onStart() {
        super.onStart()

        val user = FirebaseAuth.getInstance().currentUser

        if (user == null) {
            // User not logged in, go to login screen
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        } else {
            // User is logged in, check their role in Firebase
            val uid = user.uid
            val userRef = FirebaseDatabase.getInstance().getReference("users").child(uid)

            userRef.get()
                .addOnSuccessListener { dataSnapshot ->
                    val role = dataSnapshot.child("role").value?.toString()
                    Log.d("MainActivity", "User role: $role")

                    when (role) {
                        "buyer" -> {
                            startActivity(Intent(this, BuyerHomeActivity::class.java))
                            finish()
                        }
                        "seller" -> {
                            startActivity(Intent(this, SellerHomeActivity::class.java))
                            finish()
                        }
                        else -> {
                            Log.e("MainActivity", "Unknown or missing role.")
                            // Optional: Sign out user or redirect to login
                            FirebaseAuth.getInstance().signOut()
                            startActivity(Intent(this, LoginActivity::class.java))
                            finish()
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("MainActivity", "Failed to fetch user role", exception)
                    // Optional: show error or redirect to login
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Optional: setContentView(R.layout.activity_main)
        // You can show a loading screen here while checking auth
    }
}
