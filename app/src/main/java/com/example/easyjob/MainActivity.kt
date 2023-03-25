package com.example.easyjob

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.easyjob.databinding.ActivityMainBinding
import com.example.easyjob.employer.EmployerHome
import com.example.easyjob.user.UserHome
import com.example.easyjob.user.UserRegister
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseDatabase
    private lateinit var dburef: DatabaseReference
    private lateinit var dberef: DatabaseReference
    lateinit var myPreference: MyPreference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()


        binding.btnLogin.setOnClickListener{
            startActivity(Intent(this, LoginActivity::class.java))
        }

        binding.btnRegister.setOnClickListener{
            startActivity(Intent(this, UserRegister::class.java))
        }
    }

    override fun onStart() {
        super.onStart()
        db = FirebaseDatabase.getInstance()

        dburef = db.getReference("Users")
        dberef = db.getReference("Employers")

        if(auth.currentUser != null)
        {
            dburef.addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot){
                    if(snapshot.hasChild(auth.currentUser!!.uid))
                    {
                        startActivity(Intent(this@MainActivity, UserHome::class.java).also {
                            it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(it)
                        })
                    }
                }
                override fun onCancelled(error: DatabaseError) {Log.d("user","This is a Employer")}
            })

            dberef.addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.hasChild(auth.currentUser!!.uid))
                    {
                        Intent(this@MainActivity, EmployerHome::class.java).also {
                            it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(it)
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {Log.d("employer","This is a User")}
            })
        }
    }

    override fun attachBaseContext(newBase: Context?) {

        myPreference = MyPreference(newBase!!)
        val lang:String = myPreference.getLoginCount()

        super.attachBaseContext(MyContextWrapper.wrap(newBase,lang))
    }

}