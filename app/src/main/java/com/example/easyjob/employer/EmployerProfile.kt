package com.example.easyjob.employer

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import com.example.easyjob.MainActivity
import com.example.easyjob.R
import com.example.easyjob.databinding.FragmentEmployerProfileBinding
import com.example.easyjob.user.UserDataViewModel
import com.example.easyjob.user.UserHome
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage

class EmployerProfile : Fragment() {
    private var _binding: FragmentEmployerProfileBinding? =null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var dbref: DatabaseReference
    private lateinit var uid: String
    private lateinit var employer: EmployerData
    private lateinit var navController: NavController
    private lateinit var employerDataViewModel: EmployerDataViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEmployerProfileBinding.inflate(inflater,container,false)

        auth = FirebaseAuth.getInstance()

        //show bottom navigation bar
        (activity as EmployerHome).showBottomNavigationView()

        //get profile image
        val storageRef = FirebaseStorage.getInstance().reference
        val filePathAndName = "EmployerProfileImages/"+ auth.currentUser!!.uid
        val imageRef = storageRef.child(filePathAndName)

        imageRef.downloadUrl.addOnSuccessListener { uri ->
            Glide.with(this)
                .load(uri)
                .into(binding.ivEmployerProfile)
        }.addOnFailureListener {}

        //get user data form view model
        employerDataViewModel = ViewModelProvider(requireActivity())[EmployerDataViewModel::class.java]

        employerDataViewModel.getData(auth.currentUser!!.uid)
        employerDataViewModel.employerData.observe(viewLifecycleOwner) { employerData ->
            binding.tvEmployerName.text = employerData.name
            binding.tvEmployerEmail.text = employerData.email
        }

        //to view user personal information
        information()
        logOut()
        return binding.root
    }

    private fun information(){
        binding.tvProfile.setOnClickListener {
            navController = Navigation.findNavController(binding.tvProfile)
            navController.navigate(R.id.action_employerProfile_to_employerInformation)
        }
        binding.tvProfileArrow.setOnClickListener {
            navController = Navigation.findNavController(binding.tvProfile)
            navController.navigate(R.id.action_employerProfile_to_employerInformation)
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