package com.example.easyjob.employer

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.easyjob.LoginActivity
import com.example.easyjob.databinding.ActivityEmployerRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class EmployerRegister : AppCompatActivity() {
    private lateinit var binding: ActivityEmployerRegisterBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var dbref : DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEmployerRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //link firebase
        auth = FirebaseAuth.getInstance()

        //Switch to employer login
        binding.tvLogin.setOnClickListener {
            startActivity(Intent(this,LoginActivity::class.java))
        }

        binding.tvRegisterUser.setOnClickListener {
            onBackPressed()
        }


        binding.btnEmployerRegister.setOnClickListener {
            val email = binding.etEmployerEmail.text.toString().trim()
            val password = binding.etEmployerPassword.text.toString().trim()
            val cpassword = binding.etEmployerConfirmPassword.text.toString().trim()
            if(email.isNotEmpty()&& password.isNotEmpty() && cpassword.isNotEmpty()){
                if(cpassword==password) {
                    registerEmployer(email, password)
                }
                else{
                    Toast.makeText(this,"Password not match", Toast.LENGTH_SHORT).show()
                }
            }
            else{
                Toast.makeText(this,"Empty Field is not allowed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    //Create Employer
    private fun registerEmployer(email: String,password:String)
    {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this){
                if (it.isSuccessful){
                    storeData()
                    auth.currentUser?.sendEmailVerification()
                        ?.addOnSuccessListener {
                            Toast.makeText(this, "Please verify your email", Toast.LENGTH_LONG).show()
                            startActivity(Intent(this, LoginActivity::class.java))
                        }
                        ?.addOnFailureListener {
                            Toast.makeText(this,it.toString(),Toast.LENGTH_SHORT).show()
                        }
                }
                else{
                    Toast.makeText(this,it.exception.toString(), Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun storeData()
    {
        val email  = binding.etEmployerEmail.text.toString()
        val profileStatus = "0"

        dbref = FirebaseDatabase.getInstance().getReference("Employers")
        val currentuser = FirebaseAuth.getInstance().currentUser!!.uid
        //User Data
        val employerdata = EmployerData(
            "", email, "", "", "", "", "", "", profileStatus)
        dbref.child(currentuser).setValue(employerdata)
    }
}