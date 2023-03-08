package com.example.easyjob.employer

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
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
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.bumptech.glide.Glide
import com.example.easyjob.R
import com.example.easyjob.databinding.FragmentEditEmployerProfileBinding
import com.example.easyjob.databinding.FragmentEditUserProfileBinding
import com.example.easyjob.databinding.FragmentEmployerInformationBinding
import com.example.easyjob.user.EditUserProfileFragment
import com.example.easyjob.user.UserData
import com.example.easyjob.user.UserDataViewModel
import com.example.easyjob.user.UserHome
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.io.File

class EditEmployerProfile : Fragment() {
    private var _binding: FragmentEditEmployerProfileBinding? = null
    private val binding get()  = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var dbref: DatabaseReference
    private lateinit var uid: String
    private lateinit var employer: EmployerData
    private lateinit var navController: NavController
    private lateinit var employerDataViewModel: EmployerDataViewModel
    private var imageUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEditEmployerProfileBinding.inflate(inflater, container, false)

        //action bar
        val toolbar = binding.toolbar
        setHasOptionsMenu(true)
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        val actionBar = (activity as? AppCompatActivity)?.supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        NavigationUI.setupActionBarWithNavController(
            activity as AppCompatActivity,
            navController,
            appBarConfiguration
        )

        //hide bottom navigation bar
        (activity as EmployerHome).hideBottomNavigationView()

        //Get Profile Image
        auth = FirebaseAuth.getInstance()
        val storageRef = FirebaseStorage.getInstance().reference
        val imageChillName = "EmployerProfileImages/"+ auth.currentUser!!.uid
        val imageRef = storageRef.child(imageChillName)

        imageRef.downloadUrl.addOnSuccessListener { uri ->
            Glide.with(this)
                .load(uri)
                .into(binding.ivEmployerProfileImg)
        }.addOnFailureListener {}

        //get Employer and Company Data
        getEmployerData()

        binding.ivEmployerProfileImg.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT

            startActivityForResult(intent, 100)
        }

        //back to forward page
        binding.btnCancel.setOnClickListener {
            navController.navigateUp()
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()

        binding.btnUpdateProfile.setOnClickListener {

            val name = binding.etEmployerFullname.text.toString()
            val email = binding.etEmployerEmail.text.toString()
            val contact = binding.etEmployerContact.text.toString()
            val companyName = binding.etCompanyFullname.text.toString()
            val companyEmail = binding.etCompanyEmail.text.toString()
            val companyAddress = binding.etCompanyAddress.text.toString()
            val companyIndustry = binding.etCompanyIndustry.text.toString()
            val companyOverview = binding.etCompanyOverview.text.toString()
            val profileStatus = "1"

            updateData(name,email,contact,companyName,
                companyEmail,companyAddress,companyIndustry,companyOverview,profileStatus)
        }
    }

    private fun getEmployerData(){
        auth = FirebaseAuth.getInstance()

        //get user data from view model
        employerDataViewModel = ViewModelProvider(requireActivity())[EmployerDataViewModel::class.java]

        employerDataViewModel.getData(auth.currentUser!!.uid)
        employerDataViewModel.employerData.observe(viewLifecycleOwner) { employerData ->
            binding.etEmployerFullname.setText(employerData.name)
            binding.etEmployerEmail.setText(employerData.email)
            binding.etEmployerContact.setText(employerData.contact)
            binding.etCompanyFullname.setText(employerData.companyName)
            binding.etCompanyEmail.setText(employerData.companyEmail)
            binding.etCompanyAddress.setText(employerData.companyAddress)
            binding.etCompanyIndustry.setText(employerData.companyIndustry)
            binding.etCompanyOverview.setText(employerData.companyOverview)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 100 && resultCode == Activity.RESULT_OK){
            imageUri = data?.data!!
            binding.ivEmployerProfileImg.setImageURI(imageUri)
        }
    }

    private fun uploadImage() {
        auth = FirebaseAuth.getInstance()
        dbref = FirebaseDatabase.getInstance().getReference("Employers")

        val progressDialog = ProgressDialog(requireActivity())
        progressDialog.setMessage("Uploading...")
        progressDialog.setCancelable(true)
        progressDialog.show()

        val filePathAndName = "EmployerProfileImages/"+ auth.currentUser!!.uid
        val reference = FirebaseStorage.getInstance().getReference(filePathAndName)

        val uploadTask = reference.putFile(imageUri!!)

        uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            reference.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUri = task.result
                dbref.child(auth.currentUser!!.uid).child("profile_image").setValue(downloadUri.toString())
                progressDialog.dismiss()
            }
        }
    }

    private fun updateData(name: String, email: String,contact: String,companyName: String,
                           companyEmail: String,companyAddress: String,companyIndustry: String,
                           companyOverview: String,profileStatus: String) {
        val navController = findNavController()

        dbref = FirebaseDatabase.getInstance().getReference("Employers")

        val employerMap = mapOf(
            "name" to name,
            "email" to email,
            "contact" to contact,
            "companyName" to companyName,
            "companyEmail" to companyEmail,
            "companyAddress" to companyAddress,
            "companyIndustry" to companyIndustry,
            "companyOverview" to companyOverview,
            "profileStatus" to profileStatus
        )

        val update = dbref.child(auth.currentUser!!.uid).updateChildren(employerMap)
        if(imageUri!= null){
            uploadImage()
        }
        update.addOnSuccessListener {
            navController.popBackStack()
            Toast.makeText(requireContext(), "Your profile has been saved!", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener{
            Toast.makeText(requireContext(), "Fail to update your profile!", Toast.LENGTH_SHORT).show()
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