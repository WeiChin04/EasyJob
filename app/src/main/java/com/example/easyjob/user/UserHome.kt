package com.example.easyjob.user

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.easyjob.MainActivity
import com.example.easyjob.databinding.ActivityUserHomeBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class UserHome : AppCompatActivity() {
    private lateinit var binding: ActivityUserHomeBinding
    private lateinit var navController: NavController


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserHomeBinding.inflate(layoutInflater)

        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(binding.root)

        val fragmentId = binding.fragmentContainerView2.id
        val navHostFragment = supportFragmentManager.findFragmentById(fragmentId) as NavHostFragment

        val nv = findViewById<BottomNavigationView>(binding.btmNvUser.id)

        navController = navHostFragment.navController

        nv.setupWithNavController(navController)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    fun showBottomNavigationView() {
        val bottomNavigationView = findViewById<BottomNavigationView>(binding.btmNvUser.id)
        bottomNavigationView.visibility = View.VISIBLE
    }

    fun hideBottomNavigationView() {
        val bottomNavigationView = findViewById<BottomNavigationView>(binding.btmNvUser.id)
        bottomNavigationView.visibility = View.GONE
    }
}