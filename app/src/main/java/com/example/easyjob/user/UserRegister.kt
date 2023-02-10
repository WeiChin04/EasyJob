package com.example.easyjob.user

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.easyjob.databinding.ActivityUserRegisterBinding
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
            startActivity(Intent(this,UserLogin::class.java))
        }

        //link firebase
        auth = FirebaseAuth.getInstance()

        //Switch to employer login
        binding.tvUserLogin.setOnClickListener {
            onBackPressed()
        }


        binding.btnUserRegister.setOnClickListener {
            val email = binding.etUserEmail.text.toString().trim()
            val password = binding.etUserPassword.text.toString().trim()
            val cpassword = binding.etUserConfirmPassword.text.toString().trim()
            if(email.isNotEmpty()&& password.isNotEmpty() && cpassword.isNotEmpty()){
                if(cpassword==password) {
                    registerUser(email, password)
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

    //Create
    private fun registerUser(email: String,password:String)
    {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this){
                if (it.isSuccessful){
                    storeData()
                    startActivity(Intent(this, UserLogin::class.java))
                }
                else{
                    Toast.makeText(this,it.exception.toString(), Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun storeData()
    {
        val email  = binding.etUserEmail.text.toString()
        val profileStatus = "1"

        dbref = FirebaseDatabase.getInstance().getReference("User")
        val currentuser = FirebaseAuth.getInstance().currentUser!!.uid
        //User Data
        val userdata = UserData(
            "",email,"","","","",profileStatus
        )
        dbref.child(currentuser).setValue(userdata)
    }
}