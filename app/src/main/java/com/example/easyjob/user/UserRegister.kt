package com.example.easyjob.user

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.easyjob.LoginActivity
import com.example.easyjob.databinding.ActivityUserRegisterBinding
import com.example.easyjob.employer.EmployerRegister
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class UserRegister : AppCompatActivity() {

    private lateinit var binding: ActivityUserRegisterBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var dbref : DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserRegisterBinding.inflate(layoutInflater)

        setContentView(binding.root)

        binding.tvUserLogin.setOnClickListener {
            startActivity(Intent(this,LoginActivity::class.java))
        }

        //link firebase
        auth = FirebaseAuth.getInstance()

        //Switch to employer login
        binding.tvUserLogin.setOnClickListener {
            startActivity(Intent(this,LoginActivity::class.java))
        }

        binding.tvRegisterEmployer.setOnClickListener {
            startActivity(Intent(this,EmployerRegister::class.java))
        }


        binding.btnUserRegister.setOnClickListener {
            validation()
        }
    }

    //Create
    private fun registerUser(email: String,password:String)
    {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this){
                if (it.isSuccessful){
                    storeData()
                    auth.currentUser?.sendEmailVerification()
                        ?.addOnSuccessListener {
                            Toast.makeText(this, "Please verify your email",Toast.LENGTH_LONG).show()
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
        val email = binding.etUserEmail.text.toString().trim()
        val password = binding.etUserPassword.text.toString().trim()
        val cpassword = binding.etUserConfirmPassword.text.toString().trim()
        if(email.isEmpty()){
            binding.etUserEmail.error = "Email cannot be empty"
        }

        if (password.length < 8){
            binding.etUserPassword.error = "Password must be at least 8"
        }

        if(cpassword.isEmpty()){
            binding.etUserConfirmPassword.error = "Confirm Password cannot be empty"
        }

        if(email.isEmpty()||password.length<8){
            Toast.makeText(this@UserRegister,"Please ensure that the information entered is correct!! ",Toast.LENGTH_SHORT).show()
        }else {
            if(binding.checkboxTnc.isChecked && cpassword == password) {
                registerUser(email, password)
            }else{
                if(cpassword != password) {
                    binding.etUserConfirmPassword.error = "Password and Confirm Password No Match"
                }else {
                    Toast.makeText(this@UserRegister,"Please Agree T&C to Continue", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun storeData()
    {
        val email  = binding.etUserEmail.text.toString()
        val profileStatus = "Incomplete"
        val educationLevel = ""

        dbref = FirebaseDatabase.getInstance().getReference("Users")
        val currentUser = FirebaseAuth.getInstance().currentUser!!.uid
        //User Data
        val userdata = UserData(
            "","",email,"","","",educationLevel,"","","",profileStatus,currentUser,""
        )
        dbref.child(currentUser).setValue(userdata)
    }
}