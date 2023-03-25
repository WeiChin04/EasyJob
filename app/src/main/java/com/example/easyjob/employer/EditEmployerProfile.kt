package com.example.easyjob.employer

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.bumptech.glide.Glide
import com.example.easyjob.R
import com.example.easyjob.databinding.FragmentEditEmployerProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class EditEmployerProfile : Fragment() {
    private var _binding: FragmentEditEmployerProfileBinding? = null
    private val binding get()  = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var dbref: DatabaseReference
    private lateinit var employerDataViewModel: EmployerDataViewModel
    private var imageUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEditEmployerProfileBinding.inflate(inflater, container, false)

        //action bar
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
            showConfirmationDialog()
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()

        binding.btnUpdateProfile.setOnClickListener {

            validateInput()
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
            binding.etEmployerAddress.setText(employerData.address)
            binding.etCompanyIndustry.setText(employerData.companyIndustry)
            binding.etEmployerOverview.setText(employerData.overview)
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
        progressDialog.setMessage(getString(R.string.uploading))
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

    private fun updateData(name: String, contact: String,address: String,companyIndustry: String,
                           overview: String,profileStatus: String) {
        val navController = findNavController()

        dbref = FirebaseDatabase.getInstance().getReference("Employers")

        val employerMap = mapOf(
            "name" to name,
            "contact" to contact,
            "address" to address,
            "companyIndustry" to companyIndustry,
            "overview" to overview,
            "profileStatus" to profileStatus
        )

        val update = dbref.child(auth.currentUser!!.uid).updateChildren(employerMap)
        if(imageUri!= null){
            uploadImage()
        }
        update.addOnSuccessListener {
            navController.popBackStack()
            Toast.makeText(requireContext(), getString(R.string.profile_save), Toast.LENGTH_SHORT).show()
        }.addOnFailureListener{
            Toast.makeText(requireContext(), getString(R.string.profile_update_fail), Toast.LENGTH_SHORT).show()
        }
    }

    private fun validateInput(){
        val name = binding.etEmployerFullname.text.toString()
        val contact = binding.etEmployerContact.text.toString()
        val address = binding.etEmployerAddress.text.toString()
        val companyIndustry = binding.etCompanyIndustry.text.toString()
        val overview = binding.etEmployerOverview.text.toString()
        var profileStatus = ""

        if(name.isEmpty()) {
            binding.etEmployerFullname.error = getString(R.string.enter_name)
        }
        if(contact.isEmpty()) {
            binding.etEmployerContact.error = getString(R.string.enter_contact)
        }

        profileStatus = if(address.isEmpty()||overview.isEmpty()){
            "Incomplete"
        }else{
            "Completed"
        }

        if(name.isEmpty()||contact.isEmpty()){
            Toast.makeText(context,getString(R.string.ensure_fill_correct),Toast.LENGTH_SHORT).show()
        }else {
            updateData(name,contact,address,companyIndustry,overview,profileStatus)
        }


    }

    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                showConfirmationDialog()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showConfirmationDialog() {
        val alertDialog = AlertDialog.Builder(requireContext())
        alertDialog.setTitle(getString(R.string.messageDialog_confirm))
        alertDialog.setMessage(getString(R.string.exit_sure))
        alertDialog.setPositiveButton(getString(R.string.messageDialog_yes)) { _, _ ->
            requireActivity().onBackPressed()
        }
        alertDialog.setNegativeButton(getString(R.string.messageDialog_no)) { dialog, _ ->
            dialog.cancel()
        }
        alertDialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}