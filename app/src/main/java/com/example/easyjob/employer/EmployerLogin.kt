package com.example.easyjob.employer

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.easyjob.databinding.ActivityEmployerLoginBinding
import com.example.easyjob.user.UserLogin
import com.google.firebase.auth.FirebaseAuth

class EmployerLogin : AppCompatActivity() {
    private lateinit var binding: ActivityEmployerLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEmployerLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.tvEmployerSignUp.setOnClickListener{
            startActivity(Intent(this, EmployerRegister::class.java))
        }

        binding.tvSwitchToUser.setOnClickListener {
            startActivity(Intent(this, UserLogin::class.java))
        }

        binding.btnEmployerLogin.setOnClickListener {
            val email = binding.etEmployerEmail.text.toString()
            val password = binding.etEmployerPassword.text.toString()

            if(email.isNotEmpty()&&password.isNotEmpty())
            {
                auth.signInWithEmailAndPassword(email, password).addOnCompleteListener{
                    if(it.isSuccessful){
                        startActivity((Intent(this,EmployerHome::class.java)))
                    }
                }
            }
        }
    }
}