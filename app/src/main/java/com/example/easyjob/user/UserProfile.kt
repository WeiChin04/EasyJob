package com.example.easyjob.user

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.easyjob.MainActivity
import com.example.easyjob.R
import com.example.easyjob.databinding.FragmentUserProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class UserProfile : Fragment() {

    private var _binding: FragmentUserProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var dbref: DatabaseReference
    private lateinit var uid: String
    private lateinit var user: UserData
    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentUserProfileBinding.inflate(inflater, container, false)
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
                binding.tvUserName.text = user.name
                binding.tvUserEmail.text = user.email
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        binding.tvProfile.setOnClickListener {
            navController = Navigation.findNavController(binding.tvProfile)
            navController.navigate(R.id.action_userProfile_to_viewUserProfileFragment)
        }



        Logout()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun Logout()
    {
        binding.viewLogout.setOnClickListener {
            auth.signOut()
            activity?.let {
                val intent = Intent (it, MainActivity::class.java).also {
                    it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                it.startActivity(intent)
            }
        }
        binding.tvLogOutArrow.setOnClickListener {
            auth.signOut()
            activity?.let {
                val intent = Intent (it, MainActivity::class.java).also {
                    it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                it.startActivity(intent)
            }
        }
    }
}