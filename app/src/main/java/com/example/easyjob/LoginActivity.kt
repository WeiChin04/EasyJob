package com.example.easyjob

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.easyjob.databinding.ActivityLoginBinding
import com.example.easyjob.employer.EmployerHome
import com.example.easyjob.user.UserHome
import com.example.easyjob.user.UserRegister
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseDatabase
    private lateinit var dburef: DatabaseReference
    private lateinit var dberef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.tvUserSignUp.setOnClickListener {
            startActivity(Intent(this, UserRegister::class.java))
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
                            checkUser()
                        }
                        else
                        {
                            Toast.makeText(this,"Please verify your email", Toast.LENGTH_SHORT).show()
                        }
                    }
                    else{
                        Toast.makeText(this,"Sign in fail", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun checkUser()
    {
        db = FirebaseDatabase.getInstance()

        dburef = db.getReference("Users")
        dberef = db.getReference("Employers")

        if(auth.currentUser != null)
        {
            dburef.addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot){
                    if(snapshot.hasChild(auth.currentUser!!.uid))
                    {
                        startActivity(Intent(this@LoginActivity, UserHome::class.java).also {
                            it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(it)
                        })
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })

            dberef.addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.hasChild(auth.currentUser!!.uid))
                    {
                        Intent(this@LoginActivity, EmployerHome::class.java).also {
                            it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(it)
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
        }
    }
}