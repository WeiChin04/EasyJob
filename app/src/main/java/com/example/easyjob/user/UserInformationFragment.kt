package com.example.easyjob.user

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.bumptech.glide.Glide
import com.example.easyjob.R
import com.example.easyjob.databinding.FragmentUserInformationBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import java.io.File


class UserInformationFragment : Fragment() {
    private var _binding: FragmentUserInformationBinding? = null
    private val binding get()  = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var dbref: DatabaseReference
    private lateinit var uid: String
    private lateinit var user: UserData
    private lateinit var navController: NavController
    private lateinit var userDataViewModel: UserDataViewModel


    companion object {
        private const val REQUEST_CODE_STORAGE_PERMISSION = 100
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentUserInformationBinding.inflate(inflater, container, false)
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
        (activity as UserHome).hideBottomNavigationView()

        auth = FirebaseAuth.getInstance()

        //get profile image
        val storageRef = FirebaseStorage.getInstance().reference
        val filePathAndName = "ProfileImages/"+ auth.currentUser!!.uid
        val imageRef = storageRef.child(filePathAndName)

        if(imageRef!=null) {
            imageRef.downloadUrl.addOnSuccessListener { uri ->
                Glide.with(this)
                    .load(uri)
                    .into(binding.userProfileImg)
            }.addOnFailureListener {}
        }

        //get resume file
        val pdfChillName = "PDFFiles/${auth.currentUser!!.uid}.pdf"
        val pdfRef = storageRef.child(pdfChillName)

        pdfRef.downloadUrl.addOnSuccessListener { uri->
            binding.tvUserResume.text = "Resume.pdf"
        }.addOnFailureListener {}


        //get user data from view model
        userDataViewModel = ViewModelProvider(requireActivity())[UserDataViewModel::class.java]

        userDataViewModel.getData(auth.currentUser!!.uid)
        userDataViewModel.userData.observe(viewLifecycleOwner) { userData ->
            binding.tvUserFullname.text = userData.name
            binding.tvUserGender.text = userData.gender
            binding.tvUserEmail.text = userData.email
            binding.tvUserContact.text = userData.contact
            binding.tvUserJobSalary.text = userData.jobsalary
            binding.tvUserAddress.text = userData.address
            binding.tvEducationLevel.text = userData.education_level
            binding.tvUserAboutMe.text = userData.about_me
        }

        val downloadsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(downloadsFolder, "Resume.pdf")
        val downloadTask = pdfRef.getFile(file)
        val permissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)

        binding.tvUserResume.setOnClickListener {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(), permissions, REQUEST_CODE_STORAGE_PERMISSION)
            } else {
                // 权限已授权，执行需要权限的操作
                downloadTask.addOnSuccessListener {
                    Log.d("DOWNLOAD", "Download completed: ${file.absolutePath}")
                    Toast.makeText(requireContext(),"Download Completed",Toast.LENGTH_SHORT).show()
                }.addOnFailureListener { exception ->
                    Log.e("DOWNLOAD", "Download failed: $exception")
                    Toast.makeText(requireContext(),"Download Failed",Toast.LENGTH_SHORT).show()
                }
            }
        }

        //back to forward page
        binding.btnCancel.setOnClickListener {
            navController.navigateUp()
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
//        auth = FirebaseAuth.getInstance()
//        dbref = FirebaseDatabase.getInstance().getReference("Users")
//        uid = auth.currentUser?.uid.toString()
//
//        dbref.child(uid).addValueEventListener(object: ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot){
//                user = snapshot.getValue(UserData::class.java)!!
//                binding.tvUserFullname.text = user.name
//                binding.tvUserEmail.text = user.email
//                binding.tvUserContact.text = user.contact
//                binding.tvUserJobSalary.text = user.jobsalary
//                binding.tvUserAddress.text = user.address
//                binding.tvEducationLevel.text = user.education_level
//                binding.tvUserAboutMe.text = user.about_me
//            }
//
//            override fun onCancelled(error: DatabaseError) {}
//        })

        binding.btnUserEditProfile.setOnClickListener {
            navController = findNavController(binding.btnUserEditProfile)
            navController.navigate(R.id.action_userInformationFragment_to_editUserProfileFragment)
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