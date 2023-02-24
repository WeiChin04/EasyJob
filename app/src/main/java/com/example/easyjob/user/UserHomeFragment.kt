package com.example.easyjob.user

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.easyjob.MainActivity
import com.example.easyjob.R
import com.example.easyjob.databinding.FragmentUserHomeBinding
import com.example.easyjob.employer.PostJob
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class UserHomeFragment : Fragment() {

    private var _binding: FragmentUserHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var dbref: DatabaseReference
    private lateinit var uid: String
    private lateinit var user: UserData

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentUserHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        auth = FirebaseAuth.getInstance()
        dbref = FirebaseDatabase.getInstance().getReference("Users")
        uid = auth.currentUser?.uid.toString()

        dbref.child(uid).addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot){
                user = snapshot.getValue(UserData::class.java)!!
                binding.tvt.text = user.email
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        binding.button.setOnClickListener {
            auth.signOut()
            activity?.let {
                val intent = Intent (it, MainActivity::class.java).also {
                    it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                it.startActivity(intent)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}