package com.example.easyjob.user

import android.Manifest
import android.app.Activity.RESULT_OK
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.bumptech.glide.Glide
import com.example.easyjob.R
import com.example.easyjob.databinding.FragmentEditUserProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import java.io.File

class EditUserProfileFragment : Fragment() {

    private var _binding: FragmentEditUserProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var dbref: DatabaseReference
    private lateinit var uid: String
    private lateinit var userDataViewModel: UserDataViewModel
    private val CHANNEL_ID = "Channel_id_example_01"
    private var imageUri: Uri? = null
    private var pdfUri: Uri? = null

    companion object {
        private const val REQUEST_CODE_STORAGE_PERMISSION = 100
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEditUserProfileBinding.inflate(inflater, container, false)

        //action bar
        val toolbar = binding.toolbar
        setHasOptionsMenu(true)
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        val actionBar = (activity as? AppCompatActivity)?.supportActionBar
        actionBar?.setDisplayShowTitleEnabled(false)
        actionBar?.setDisplayHomeAsUpEnabled(true)
        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        NavigationUI.setupActionBarWithNavController(
            activity as AppCompatActivity,
            navController,
            appBarConfiguration
        )

        //hide bottom navigation bar
        (activity as UserHome).hideBottomNavigationView()

        //Get Profile Image
        auth = FirebaseAuth.getInstance()
        val storageRef = FirebaseStorage.getInstance().reference
        val imageChillName = "UsersProfileImages/"+ auth.currentUser!!.uid
        val imageRef = storageRef.child(imageChillName)

        imageRef.downloadUrl.addOnSuccessListener { uri ->
            Glide.with(this)
                .load(uri)
                .into(binding.userProfileImg)
        }.addOnFailureListener {}

        //get resume file
        val pdfChillName = "PDFFiles/${auth.currentUser!!.uid}.pdf"
        val pdfRef = storageRef.child(pdfChillName)

        pdfRef.downloadUrl.addOnSuccessListener {
            binding.tvResumeFile.text = getString(R.string.pdf_name)
        }.addOnFailureListener {}

        //get User Data
        getUserData()

        binding.userProfileImg.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT

            startActivityForResult(intent, 100)
        }

        binding.btnUploadFile.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.type = "application/pdf"
            startActivityForResult(intent, 200)
        }

        val downloadsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        var fileName = "Resume.pdf"
        var count = 1
        while (File(downloadsFolder, fileName).exists()) {
            fileName = "Resume($count).pdf"
            count++
        }
        val file = File(downloadsFolder, fileName)
        val downloadTask = pdfRef.getFile(file)
        val permissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        binding.tvResumeFile.setOnClickListener {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(), permissions,
                    UserInformationFragment.REQUEST_CODE_STORAGE_PERMISSION
                )
            } else {
                // 权限已授权，执行需要权限的操作
                downloadTask.addOnSuccessListener {
                    Log.d("DOWNLOAD", "Download completed: ${file.absolutePath}")
                    Toast.makeText(requireContext(),"Download Completed",Toast.LENGTH_SHORT).show()

                    // 下载完成后，创建通知
                    createNotificationChannel()
                    val notificationId = 1
                    val intent = Intent(Intent.ACTION_VIEW)
                    val fileUri = FileProvider.getUriForFile(
                        requireContext(),
                        "com.example.easyjob.fileprovider",
                        file
                    )
                    intent.setDataAndType(fileUri, "application/pdf")
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    val pendingIntent = PendingIntent.getActivity(requireContext(), 0, intent, 0)
                    val notification = NotificationCompat.Builder(requireContext(), CHANNEL_ID)
                        .setSmallIcon(R.mipmap.ic_launcher_round)
                        .setContentTitle("$fileName Downloaded")
                        .setContentText("Click to view the downloaded PDF file.")
                        .setContentIntent(pendingIntent)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setAutoCancel(true)
                        .build()
                    val notificationManager = NotificationManagerCompat.from(requireContext())
                    notificationManager.notify(notificationId, notification)

                }.addOnFailureListener { exception ->
                    Log.e("DOWNLOAD", "Download failed: $exception")
                    Toast.makeText(requireContext(),"Download Failed",Toast.LENGTH_SHORT).show()
                }
            }
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

    private fun getSpinner() {
        auth = FirebaseAuth.getInstance()
        dbref = FirebaseDatabase.getInstance().getReference("Users")
        uid = auth.currentUser?.uid.toString()

        val spinner = binding.spEducationLevel

        val educationLevels = arrayListOf("Click To Select Education Level","Secondary School", "Pre-University", "Diploma", "Bachelor's Degree", "Master's Degree", "PhD")

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            educationLevels
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        dbref.child(uid).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(UserData::class.java)
                user?.let {
                    val educationLevel = it.education_level
                    Log.d("education_level", "educationLevel: $educationLevel")
                    val index = educationLevels.indexOf(educationLevel)
                    Log.d("education", "educationLevel: $index")
                    spinner.setSelection(index)
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun getUserData(){
        auth = FirebaseAuth.getInstance()

        //get user data from view model
        userDataViewModel = ViewModelProvider(requireActivity())[UserDataViewModel::class.java]

        userDataViewModel.getData(auth.currentUser!!.uid)
        userDataViewModel.userData.observe(viewLifecycleOwner) { userData ->
            binding.etUserFullname.setText(userData.name)
            binding.etUserGender.setText(userData.gender)
            binding.etUserEmail.setText(userData.email)
            binding.etUserContact.setText(userData.contact)
            binding.etUserExpectationSalary.setText(userData.jobsalary)
            binding.etUserAddress.setText(userData.address)
            binding.etUserAboutMe.setText(userData.about_me)
        }
        getSpinner()
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 100 && resultCode == RESULT_OK){
            imageUri = data?.data!!
            binding.userProfileImg.setImageURI(imageUri)
        }
        else if(requestCode == 200 && resultCode == RESULT_OK){
            pdfUri = data?.data!!
            val selectedFile = File(pdfUri?.path!!)
            binding.tvResumeFile.text = selectedFile.name
        }
    }

    private fun uploadImage() {
        auth = FirebaseAuth.getInstance()
        dbref = FirebaseDatabase.getInstance().getReference("Users")

        val progressDialog = ProgressDialog(requireActivity())
        progressDialog.setMessage("Uploading...")
        progressDialog.setCancelable(true)
        progressDialog.show()

        val filePathAndName = "UsersProfileImages/"+ auth.currentUser!!.uid
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

    private fun uploadFile(){
        auth = FirebaseAuth.getInstance()
        dbref = FirebaseDatabase.getInstance().getReference("Users")

        val progressDialog = ProgressDialog(requireActivity())
        progressDialog.setMessage("Uploading...")
        progressDialog.setCancelable(true)
        progressDialog.show()
        val filePathAndName = "PDFFiles/${auth.currentUser!!.uid}.pdf"
        val reference = FirebaseStorage.getInstance().getReference(filePathAndName)

        val uploadTask = reference.putFile(pdfUri!!)

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
                dbref.child(auth.currentUser!!.uid).child("resume").setValue(downloadUri.toString())
                progressDialog.dismiss()
            }
        }
    }

    private fun updateData(name: String,gender: String,contact: String,jobsalary: String, address: String, education_level: String, about_me: String, profile_status: String, deviceToken: String) {
        val navController = findNavController()

        dbref = FirebaseDatabase.getInstance().getReference("Users")

        val userMap = mapOf(
            "name" to name,
            "gender" to gender,
            "contact" to contact,
            "jobsalary" to jobsalary,
            "address" to address,
            "education_level" to education_level,
            "about_me" to about_me,
            "profile_status" to profile_status,
            "deviceToken" to deviceToken
        )

        val update = dbref.child(auth.currentUser!!.uid).updateChildren(userMap)
        if(imageUri!= null){
            uploadImage()
        }
        if(pdfUri!= null){
            uploadFile()
        }
        update.addOnSuccessListener {
            navController.popBackStack()
            Toast.makeText(requireContext(), "Your profile has been saved!", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener{
            Toast.makeText(requireContext(), "Fail to update your profile!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun validateInput()
    {
        val name = binding.etUserFullname.text.toString()
        val gender = binding.etUserGender.text.toString()
        val contact = binding.etUserContact.text.toString()
        val jobsalary = binding.etUserExpectationSalary.text.toString()
        val address = binding.etUserAddress.text.toString()
        val education_level = binding.spEducationLevel.selectedItem.toString()
        val about_me = binding.etUserAboutMe.text.toString()
        val profile_status = "Completed"

        if(name.isEmpty()) {
            binding.etUserFullname.error = "Please Enter Name"
        }
        if(gender.isEmpty()){
            binding.etUserGender.error = "Please Enter Your Gender"
        }
        if(contact.isEmpty()) {
            binding.etUserContact.error = "Please Enter Contact Number"
        }
        if(jobsalary.isEmpty()) {
            binding.etUserExpectationSalary.error = "Please Enter Expectation Salary"
        }
        if(address.isEmpty()) {
            binding.etUserAddress.error = "Please Enter Your Address"
        }
        if(binding.spEducationLevel.selectedItem.toString() == "Click To Select Education Level") {
            Toast.makeText(context, "Please Select Your Education Level", Toast.LENGTH_SHORT).show()
        }
        if(about_me.isEmpty()){
            binding.etUserAboutMe.error = "Please Introduce about Yourself"
        }
        if(name.isEmpty()||contact.isEmpty()||jobsalary.isEmpty()||address.isEmpty()||
           binding.spEducationLevel.selectedItem.toString() == "Click To Select Education Level"||about_me.isEmpty()){
            Toast.makeText(context,"Please Complete Your Information",Toast.LENGTH_SHORT).show()
        }else {
           updateData(name,gender,contact,jobsalary,address,education_level,about_me,profile_status, "")
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
        alertDialog.setTitle("Confirm")
        alertDialog.setMessage("Do you want to exit without saving changes?")
        alertDialog.setPositiveButton("Yes") { _, _ ->
            requireActivity().onBackPressed()
        }
        alertDialog.setNegativeButton("No") { dialog, _ ->
            dialog.cancel()
        }
        alertDialog.show()
    }

    private fun createNotificationChannel(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val notificationTitle = "Notification Title"
            val descriptionText = "Notification Description"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID,notificationTitle,importance).apply {
                description = descriptionText
            }
            val notificationManager = requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


