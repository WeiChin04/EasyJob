package com.example.easyjob.employer

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.easyjob.MainActivity
import com.example.easyjob.databinding.ActivityEmployerHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class EmployerHome : AppCompatActivity() {
    private lateinit var binding: ActivityEmployerHomeBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var dbref: DatabaseReference
    private lateinit var uid: String
    private lateinit var employer: EmployerData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEmployerHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        dbref = FirebaseDatabase.getInstance().getReference("Employer")
        uid = auth.currentUser?.uid.toString()

        dbref.child(uid).addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot){
                employer = snapshot.getValue(EmployerData::class.java)!!
                binding.tvtest.text = employer.email
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

        binding.button2.setOnClickListener {
            auth.signOut()
            Intent(this, MainActivity::class.java).also{
                it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(it)
                Toast.makeText(this, "Logout Successful", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        binding.btnPostJob.setOnClickListener{
            startActivity(Intent(this,PostJob::class.java))
        }
    }
}