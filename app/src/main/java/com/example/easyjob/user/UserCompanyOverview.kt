package com.example.easyjob.user

import android.annotation.SuppressLint
import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.bumptech.glide.Glide
import com.example.easyjob.R
import com.example.easyjob.databinding.FragmentUserCompanyOverviewBinding
import com.example.easyjob.employer.EmployerData
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage

class UserCompanyOverview : Fragment() {

    private  var _binding: FragmentUserCompanyOverviewBinding? =null
    private val binding get() = _binding!!
    private lateinit var database: FirebaseDatabase
    private lateinit var dbRef: DatabaseReference

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentUserCompanyOverviewBinding.inflate(inflater,container,false)

        //hide nav bar
        (activity as UserHome).hideBottomNavigationView()

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

        //get company details
        dbRef = FirebaseDatabase.getInstance().reference
        val employerId = arguments?.getString("employer_id")

        val storageRef = FirebaseStorage.getInstance().reference
        val filePathAndName = "EmployerProfileImages/$employerId"
        val imageRef = storageRef.child(filePathAndName)


        imageRef.downloadUrl.addOnSuccessListener { uri ->
            Glide.with(this)
                .load(uri)
                .placeholder(R.drawable.ic_person)
                .error(R.drawable.ic_person)
                .into(binding.imgEmployer)
        }.addOnFailureListener {}

        val query = dbRef.child("Employers").orderByChild("employerId").equalTo(employerId)

        query.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for(snapshot in dataSnapshot.children){
                    val employerData = snapshot.getValue(EmployerData::class.java)

                    if(employerData != null){

                        binding.tvEmployerName.text = employerData.name
                        binding.tvContact.text = employerData.contact
                        binding.tvEmail.text = employerData.email
                        binding.tvAddress.text = employerData.address
                        if(employerData.companyIndustry == ""){
                            binding.tvCompanyIndustry.text = "-"
                        }else{
                            binding.tvCompanyIndustry.text = employerData.companyIndustry
                        }
                        binding.tvOverview.text = employerData.overview
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(ContentValues.TAG, "Failed to read value.", error.toException())
                Toast.makeText(requireContext(), "Failed to read value", Toast.LENGTH_SHORT).show()
            }

        })
        return binding.root
    }

}