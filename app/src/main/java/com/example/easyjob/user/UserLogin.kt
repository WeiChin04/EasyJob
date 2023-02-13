package com.example.easyjob.user

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.easyjob.databinding.ActivityUserLoginBinding
import com.example.easyjob.employer.EmployerLogin
import com.google.firebase.auth.FirebaseAuth

class UserLogin : AppCompatActivity() {

    private lateinit var binding: ActivityUserLoginBinding
    private lateinit var auth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.tvUserSignUp.setOnClickListener {
            startActivity(Intent(this, UserRegister::class.java))
        }

        binding.tvSwitchToEmployer.setOnClickListener {
            startActivity(Intent(this, EmployerLogin::class.java))
        }

        binding.btnUserLogin.setOnClickListener {
            val email = binding.etUserEmail.text.toString()
            val password = binding.etUserPassword.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                auth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
                    if (it.isSuccessful) {
                        val verification = auth.currentUser?.isEmailVerified
                        if(verification == true)
                        {
                            startActivity((Intent(this, UserHome::class.java)))
                        }
                        else
                        {
                            Toast.makeText(this,"Please verify your email",Toast.LENGTH_SHORT).show()
                        }
                    }
                    else{
                        Toast.makeText(this,"Sign in fail",Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}