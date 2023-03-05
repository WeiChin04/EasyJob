package com.example.easyjob.user

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.example.easyjob.R
import com.example.easyjob.databinding.FragmentEditUserProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class EditUserProfileFragment : Fragment() {

    private var _binding: FragmentEditUserProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var dbref: DatabaseReference
    private lateinit var uid: String
    private lateinit var user: UserData
    private lateinit var userDataViewModel: UserDataViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEditUserProfileBinding.inflate(inflater,container,false)

        //action bar
        val toolbar = binding.toolbar
        setHasOptionsMenu(true)
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        val actionBar = (activity as? AppCompatActivity)?.supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        NavigationUI.setupActionBarWithNavController(activity as AppCompatActivity, navController, appBarConfiguration)

        //hide bottom navigation bar
        (activity as UserHome).hideBottomNavigationView()

        //back to forward page
        binding.btnCancel.setOnClickListener {
            navController.navigateUp()
        }
        return binding.root
    }

    override fun onResume() {
        super.onResume()

        auth = FirebaseAuth.getInstance()
        dbref = FirebaseDatabase.getInstance().getReference("Users")
        uid = auth.currentUser?.uid.toString()

        if (uid.isNotEmpty()){
            getUserData()
        }

        binding.btnUpdateProfile.setOnClickListener {

            val name = binding.etUserFullname.text.toString()
            val email = binding.etUserEmail.text.toString()
            val contact = binding.etUserContact.text.toString()
            val jobsalary = binding.etUserExpectationSalary.text.toString()
            val address = binding.etUserAddress.text.toString()
            val education_level = binding.spEducationLevel.selectedItem.toString()
            val about_me = binding.etUserAboutMe.text.toString()
            val profile_status = "1"

            updateData(name,email,contact,jobsalary,address,education_level,about_me,profile_status)
        }


    }

    private fun getSpinner() {

        val adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.education_level,
            android.R.layout.simple_spinner_item
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        val spinner = binding.spEducationLevel

        val educationLevels = arrayListOf("Click To Select Education Level","Secondary School", "Pre-University", "Diploma", "Bachelor's Degree", "Master's Degree", "PhD")
        spinner.adapter = adapter

        dbref.child(uid).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(UserData::class.java)
                user?.let {
                    val educationLevel = it.education_level
                    val index = educationLevels.indexOf(educationLevel)
                    spinner.setSelection(index)
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun getUserData(){
        auth = FirebaseAuth.getInstance()
        auth = FirebaseAuth.getInstance()

        //get user data from view model
        userDataViewModel = ViewModelProvider(requireActivity())[UserDataViewModel::class.java]

        userDataViewModel.getData(auth.currentUser!!.uid)
        userDataViewModel.userData.observe(viewLifecycleOwner) { userData ->
            binding.etUserFullname.setText(userData.name)
            binding.etUserEmail.setText(userData.email)
            binding.etUserContact.setText(userData.contact)
            binding.etUserExpectationSalary.setText(userData.jobsalary)
            binding.etUserAddress.setText(userData.address)
            binding.etUserAboutMe.setText(userData.about_me)
        }

        getSpinner()
    }



    private fun updateData(name: String, email: String,contact: String,jobsalary: String, address: String, education_level: String, about_me: String, profile_status: String) {
        val navController = findNavController()

        dbref = FirebaseDatabase.getInstance().getReference("Users")

        val userMap = mapOf(
            "name" to name,
            "email" to email,
            "contact" to contact,
            "jobsalary" to jobsalary,
            "address" to address,
            "education_level" to education_level,
            "about_me" to about_me,
            "profile_status" to profile_status
        )

        val update = dbref.child(auth.currentUser!!.uid).updateChildren(userMap)

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