package com.example.easyjob

import android.content.ContentValues.TAG
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
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseDatabase
    private lateinit var userRef: DatabaseReference
    private lateinit var employerRef: DatabaseReference

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

        binding.tvUserResetPassword.setOnClickListener{
            startActivity(Intent(this, ResetPasswordActivity::class.java))
        }
    }

    private fun checkUser()
    {
        db = FirebaseDatabase.getInstance()

        userRef = db.getReference("Users")
        employerRef = db.getReference("Employers")

        if(auth.currentUser != null)
        {
            userRef.addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot){
                    if(snapshot.hasChild(auth.currentUser!!.uid))
                    {

                        // 检查设备是否支持 FCM。
                        FirebaseMessaging.getInstance().isAutoInitEnabled = true

                        // 启动 FCM，并注册该设备，以获取设备令牌。
                        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                            if (!task.isSuccessful) {
                                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                                return@addOnCompleteListener
                            }

                            // 获取设备令牌。
                            val deviceToken = task.result

                            // 将设备令牌与用户ID相关联，并将其保存到实时数据库中。
                                val database = FirebaseDatabase.getInstance().reference
                                val tokenRef = database.child("Users").child(auth.currentUser!!.uid).child("deviceToken")
                                tokenRef.setValue(deviceToken)
                        }

                        startActivity(Intent(this@LoginActivity, UserHome::class.java).also {
                            it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(it)
                        })
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })

            employerRef.addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.hasChild(auth.currentUser!!.uid))
                    {
                        // 检查设备是否支持 FCM。
                        FirebaseMessaging.getInstance().isAutoInitEnabled = true

                        // 启动 FCM，并注册该设备，以获取设备令牌。
                        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                            if (!task.isSuccessful) {
                                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                                return@addOnCompleteListener
                            }

                            // 获取设备令牌。
                            val deviceToken = task.result

                            // 将设备令牌与用户ID相关联，并将其保存到实时数据库中。
                            val userId = FirebaseAuth.getInstance().currentUser?.uid
                            if (userId != null) {
                                val database = FirebaseDatabase.getInstance().reference
                                val tokenRef = database.child("Employers").child(userId).child("deviceToken")
                                tokenRef.setValue(deviceToken)
                            }
                        }

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