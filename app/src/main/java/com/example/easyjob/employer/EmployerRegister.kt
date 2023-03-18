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
            validation()
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
                    Toast.makeText(this,"Email Has Been Registered", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun validation(){
        val email = binding.etEmployerEmail.text.toString().trim()
        val password = binding.etEmployerPassword.text.toString().trim()
        val cpassword = binding.etEmployerConfirmPassword.text.toString().trim()
        if(email.isEmpty()){
            binding.etEmployerEmail.error = "Email cannot be empty"
        }

        if (password.length < 8){
            binding.etEmployerPassword.error = "Password must be at least 8"
        }

        if(cpassword.isEmpty()){
            binding.etEmployerConfirmPassword.error = "Confirm Password cannot be empty"
        }

        if(email.isEmpty()||password.length<8||binding.checkboxTnc.isChecked){
            Toast.makeText(this@EmployerRegister,"Please agree T&C",Toast.LENGTH_SHORT).show()
            if (cpassword != password) {
                Toast.makeText(this@EmployerRegister,"Password and Confirm Password No Match",Toast.LENGTH_SHORT).show()
            }else {
                registerEmployer(email, password)
            }
        }
    }

    private fun storeData()
    {
        val email  = binding.etEmployerEmail.text.toString()
        val profileStatus = "0"

        dbref = FirebaseDatabase.getInstance().getReference("Employers")
        val currentUser = FirebaseAuth.getInstance().currentUser!!.uid
        //User Data
        val employerData = EmployerData(
            "", email, "", "", "", "", profileStatus)
        dbref.child(currentUser).setValue(employerData)
    }
}