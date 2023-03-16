package com.example.easyjob.employer

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.os.Message
import android.telecom.PhoneAccount.builder
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.bumptech.glide.Glide
import com.example.easyjob.R
import com.example.easyjob.databinding.FragmentApplicantDetailsBinding
import com.example.easyjob.user.UserApplicationData
import com.example.easyjob.user.UserInformationFragment
import com.google.android.datatransport.runtime.scheduling.jobscheduling.SchedulerConfig.ConfigValue.builder
import com.google.android.gms.auth.api.identity.BeginSignInRequest.GoogleIdTokenRequestOptions.builder
import com.google.firebase.database.*
import com.google.firebase.installations.InstallationTokenResult.builder
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.util.*
import java.util.stream.DoubleStream.builder
import java.util.stream.IntStream.builder

class ApplicantDetailsFragment : Fragment() {

    private  var _binding: FragmentApplicantDetailsBinding? =null
    private val binding get() = _binding!!
    private lateinit var database: DatabaseReference
//    private val deviceToken = arguments?.getString("deviceToken")


    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentApplicantDetailsBinding.inflate(inflater,container,false)

        //hide nav bar
        (activity as EmployerHome).hideBottomNavigationView()

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


        binding.tvApplicantName.text = arguments?.getString("name")
        binding.tvContact.text = arguments?.getString("contact")
        binding.tvEmail.text = arguments?.getString("email")
        binding.tvEducation.text = arguments?.getString("education_level")
        binding.tvExpecterdSalary.text = arguments?.getString("expected_salary")
        binding.tvAddress.text = arguments?.getString("address")
        binding.tvAboutMe.text = arguments?.getString("about_me")

        val storageRef = FirebaseStorage.getInstance().reference
        val filePathAndName = "ProfileImages/"+arguments?.getString("applicant_id")
        val imageRef = storageRef.child(filePathAndName)

        imageRef.downloadUrl.addOnSuccessListener { uri ->
            Glide.with(this)
                .load(uri)
                .placeholder(R.drawable.ic_person)
                .error(R.drawable.ic_person)
                .into(binding.imgApplicant)
        }.addOnFailureListener {}

        binding.tvUserResume.text = getString(R.string.empty_resume)

        //get resume file
        val pdfChillName = "PDFFiles/${arguments?.getString("applicant_id")}.pdf"
        val pdfRef = storageRef.child(pdfChillName)

        pdfRef.downloadUrl.addOnSuccessListener {
            binding.tvUserResume.text = arguments?.getString("name") +"_" + getString(R.string.pdf_name)
        }.addOnFailureListener {}

        val downloadsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(downloadsFolder, arguments?.getString("name") +"_" + getString(R.string.pdf_name))
        val downloadTask = pdfRef.getFile(file)
        val permissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        binding.tvUserResume.setOnClickListener {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(), permissions,
                    UserInformationFragment.REQUEST_CODE_STORAGE_PERMISSION
                )
            } else {
                downloadTask.addOnSuccessListener {
                    Log.d("DOWNLOAD", "Download completed: ${file.absolutePath}")
                    Toast.makeText(requireContext(),"Download Completed", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener { exception ->
                    Log.e("DOWNLOAD", "Download failed: $exception")
                    Toast.makeText(requireContext(),"Download Failed", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.btnReject.setOnClickListener {
                val alertDialog = AlertDialog.Builder(requireContext())
                alertDialog.setTitle("Confirm")
                alertDialog.setMessage("Do you want to reject "+arguments?.getString("name")+" ?")
                alertDialog.setPositiveButton("Yes") { _, _ ->
                    rejectApplicant()
                }
                alertDialog.setNegativeButton("No") { dialog, _ ->
                    dialog.cancel()
                }
                alertDialog.show()
        }

        binding.btnApprove.setOnClickListener {

            val alertDialog = AlertDialog.Builder(requireContext())
            alertDialog.setTitle("Confirm")
            alertDialog.setMessage("Do you want to approve "+arguments?.getString("name")+" ?")
            alertDialog.setPositiveButton("Yes") { _, _ ->
                approveApplicant()
            }
            alertDialog.setNegativeButton("No") { dialog, _ ->
                dialog.cancel()
            }
            alertDialog.show()
        }

        return binding.root
    }

    private fun approveApplicant() {
        database = FirebaseDatabase.getInstance().reference
        val currentDate = Date()
        val currentTimeInSeconds = currentDate.time / 1000 // divide by 1000 to get time in seconds
        val applicationId = arguments?.getString("application_id")
        val applicationRef = database.child("Applications").child(applicationId.toString())

        applicationRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val application = snapshot.getValue(UserApplicationData::class.java)

                application?.apply {
                    status = "Approved"
                    rejectAt = currentTimeInSeconds.toString()
                }

                applicationRef.setValue(application).addOnCompleteListener {
                    if (it.isSuccessful) {
                        val deviceToken = arguments?.getString("deviceToken")

                        Toast.makeText(requireContext(), "You have approved the applicant", Toast.LENGTH_SHORT).show()
                        requireActivity().onBackPressed()
                    } else {
                        Toast.makeText(requireContext(), "Something error, your job cannot be updated", Toast.LENGTH_SHORT).show()
                        requireActivity().onBackPressed()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(ContentValues.TAG, "Failed to read value.", error.toException())
                Toast.makeText(requireContext(), "Failed to read value", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun rejectApplicant() {

        database = FirebaseDatabase.getInstance().reference
        val currentTimeInSeconds = System.currentTimeMillis()
        val applicationId = arguments?.getString("application_id")
        val applicationRef = database.child("Applications").child(applicationId.toString())

        applicationRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val application = snapshot.getValue(UserApplicationData::class.java)

                application?.apply {
                    status = "Rejected"
                    rejectAt = currentTimeInSeconds.toString()
                }

                applicationRef.setValue(application).addOnCompleteListener {
                    if (it.isSuccessful) {
                        Toast.makeText(requireContext(), "You have rejected the applicant", Toast.LENGTH_SHORT).show()
                        requireActivity().onBackPressed()
                    } else {
                        Toast.makeText(requireContext(), "Something error, your job cannot be updated", Toast.LENGTH_SHORT).show()
                        requireActivity().onBackPressed()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(ContentValues.TAG, "Failed to read value.", error.toException())
                Toast.makeText(requireContext(), "Failed to read value", Toast.LENGTH_SHORT).show()
            }
        })

    }

}