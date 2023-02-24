package com.example.easyjob.user

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.easyjob.MainActivity
import com.example.easyjob.databinding.ActivityUserHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class UserHome : AppCompatActivity() {
    private lateinit var binding: ActivityUserHomeBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var dbref: DatabaseReference
    private lateinit var uid: String
    private lateinit var user: UserData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        dbref = FirebaseDatabase.getInstance().getReference("Users")
        uid = auth.currentUser?.uid.toString()

        dbref.child(uid).addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot){
                user = snapshot.getValue(UserData::class.java)!!
                binding.tvt.text = user.email
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

        binding.button.setOnClickListener {
            auth.signOut()
            Intent(this, MainActivity::class.java).also{
                it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(it)
                Toast.makeText(this, "Logout Successful", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}