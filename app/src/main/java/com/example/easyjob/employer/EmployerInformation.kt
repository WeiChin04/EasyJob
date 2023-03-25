package com.example.easyjob.employer

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.bumptech.glide.Glide
import com.example.easyjob.R
import com.example.easyjob.databinding.FragmentEmployerInformationBinding
import com.example.easyjob.user.UserData
import com.example.easyjob.user.UserDataViewModel
import com.example.easyjob.user.UserHome
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.storage.FirebaseStorage
import java.io.File

class EmployerInformation : Fragment() {
    private var _binding: FragmentEmployerInformationBinding? = null
    private val binding get()  = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var navController: NavController
    private lateinit var employerDataViewModel: EmployerDataViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEmployerInformationBinding.inflate(inflater, container, false)
        //action bar
        val toolbar = binding.toolbar
        setHasOptionsMenu(true)
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        val actionBar = (activity as? AppCompatActivity)?.supportActionBar
        actionBar?.setDisplayShowTitleEnabled(false)
        actionBar?.setDisplayHomeAsUpEnabled(true)
        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        NavigationUI.setupActionBarWithNavController(activity as AppCompatActivity, navController, appBarConfiguration)

        //hide bottom navigation bar
        (activity as EmployerHome).hideBottomNavigationView()

        auth = FirebaseAuth.getInstance()

        //get profile image
        val storageRef = FirebaseStorage.getInstance().reference
        val filePathAndName = "EmployerProfileImages/"+ auth.currentUser!!.uid
        val imageRef = storageRef.child(filePathAndName)

        if(imageRef!=null) {
            imageRef.downloadUrl.addOnSuccessListener { uri ->
                Glide.with(this)
                    .load(uri)
                    .into(binding.ivEmployerProfileImg)
            }.addOnFailureListener {}
        }

        //get user data from view model
        employerDataViewModel = ViewModelProvider(requireActivity())[EmployerDataViewModel::class.java]

        employerDataViewModel.getData(auth.currentUser!!.uid)
        employerDataViewModel.employerData.observe(viewLifecycleOwner) { employerData ->
            binding.tvEmployerFullname.text = employerData.name
            binding.tvEmployerEmail.text = employerData.email
            binding.tvEmployerContact.text = employerData.contact
            binding.tvEmployerAddress.text = employerData.address
            binding.tvCompanyIndustry.text = employerData.companyIndustry
            binding.tvEmployerOverview.text = employerData.overview
        }

        //back to forward page
        binding.btnCancel.setOnClickListener {
            navController.navigateUp()
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        binding.btnEmployerEditProfile.setOnClickListener {
            navController = Navigation.findNavController(binding.btnEmployerEditProfile)
            navController.navigate(R.id.action_employerInformation_to_editEmployerProfile)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                requireActivity().onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}