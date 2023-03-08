package com.example.easyjob.user

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import com.example.easyjob.MainActivity
import com.example.easyjob.R
import com.example.easyjob.databinding.FragmentUserProfileBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage

class UserProfile : Fragment() {

    private var _binding: FragmentUserProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var dbref: DatabaseReference
    private lateinit var uid: String
    private lateinit var user: UserData
    private lateinit var navController: NavController
    private lateinit var userDataViewModel: UserDataViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentUserProfileBinding.inflate(inflater, container, false)

        auth = FirebaseAuth.getInstance()

        //show bottom navigation bar
        (activity as UserHome).showBottomNavigationView()

        //get profile image
        val storageRef = FirebaseStorage.getInstance().reference
        val filePathAndName = "ProfileImages/"+ auth.currentUser!!.uid
        val imageRef = storageRef.child(filePathAndName)

        if(imageRef!=null) {
            imageRef.downloadUrl.addOnSuccessListener { uri ->
                Glide.with(this)
                    .load(uri)
                    .into(binding.ivUserProfile)
            }.addOnFailureListener {}
        }

        //get user data form view model
        userDataViewModel = ViewModelProvider(requireActivity())[UserDataViewModel::class.java]

        userDataViewModel.getData(auth.currentUser!!.uid)
        userDataViewModel.userData.observe(viewLifecycleOwner) { userData ->
            binding.tvUserName.text = userData.name
            binding.tvUserEmail.text = userData.email
        }

        //to view user personal information
        information()
        logOut()
        return binding.root
    }

    private fun information(){
        binding.tvProfile.setOnClickListener {
            navController = Navigation.findNavController(binding.tvProfile)
            navController.navigate(R.id.action_userProfile_to_userInformationFragment)
        }
        binding.tvProfileArrow.setOnClickListener {
            navController = Navigation.findNavController(binding.tvProfile)
            navController.navigate(R.id.action_userProfile_to_userInformationFragment)
        }
    }

    private fun logOut()
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}