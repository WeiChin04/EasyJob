package com.example.easyjob.employer

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.easyjob.LoginActivity
import com.example.easyjob.WalletData
import com.example.easyjob.R
import com.example.easyjob.databinding.ActivityEmployerRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.*

class EmployerRegister : AppCompatActivity() {
    private lateinit var binding: ActivityEmployerRegisterBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var dbref : DatabaseReference
    private lateinit var dbWalletRef : DatabaseReference

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
                            Toast.makeText(this, getString(R.string.please_verify_email),Toast.LENGTH_LONG).show()
                            startActivity(Intent(this, LoginActivity::class.java))
                        }
                        ?.addOnFailureListener {
                            Toast.makeText(this,it.toString(),Toast.LENGTH_SHORT).show()
                        }
                }
                else{
                    Toast.makeText(this,getString(R.string.email_has_been_registered), Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun validation(){
        val email = binding.etEmployerEmail.text.toString().trim()
        val password = binding.etEmployerPassword.text.toString().trim()
        val cpassword = binding.etEmployerConfirmPassword.text.toString().trim()
        if(email.isEmpty()){
            binding.etEmployerEmail.error = getString(R.string.email_empty)
        }

        if (password.length < 8){
            binding.etEmployerPassword.error = getString(R.string.password_at_least)
        }

        if(cpassword.isEmpty()){
            binding.etEmployerConfirmPassword.error = getString(R.string.confirm_password_empty)
        }

        if(email.isEmpty()||password.length<8){
            Toast.makeText(this@EmployerRegister,getString(R.string.ensure_fill_correct),Toast.LENGTH_SHORT).show()
        }else {
            if(binding.checkboxTnc.isChecked && cpassword == password) {
                registerEmployer(email, password)
            }else{
                if(cpassword != password) {
                    binding.etEmployerConfirmPassword.error = getString(R.string.password_cpassword_not_match)
                }else {
                    Toast.makeText(this@EmployerRegister,getString(R.string.t_C), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun storeData()
    {
        val email  = binding.etEmployerEmail.text.toString()
        val profileStatus = "Incomplete"

        dbref = FirebaseDatabase.getInstance().getReference("Employers")
        dbWalletRef = FirebaseDatabase.getInstance().getReference("Wallets")
        val currentUser = FirebaseAuth.getInstance().currentUser!!.uid
        val walletId = UUID.randomUUID().toString()
        //User Data
        val employerData = EmployerData(
            currentUser,"", email, "", "", "", "", profileStatus,"",walletId)

        val walletData = WalletData(
            0f,currentUser
        )

        dbref.child(currentUser).setValue(employerData)
        dbWalletRef.child(walletId).setValue(walletData)
    }
}