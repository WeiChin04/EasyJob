package com.example.easyjob.user

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.FileProvider
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
    private lateinit var navController: NavController
    private lateinit var userDataViewModel: UserDataViewModel
    private val channelId = "Channel_id_01"

    companion object {
        internal const val REQUEST_CODE_STORAGE_PERMISSION = 100
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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
        val filePathAndName = "UsersProfileImages/"+ auth.currentUser!!.uid
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

        pdfRef.downloadUrl.addOnSuccessListener {
            binding.tvUserResume.text = getString(R.string.pdf_name)
            createNotificationChannel()
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
        var fileName = "Resume.pdf"
        var count = 1
        while (File(downloadsFolder, fileName).exists()) {
            fileName = "Resume($count).pdf"
            count++
        }
        val file = File(downloadsFolder, fileName)
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
                    Toast.makeText(requireContext(),getString(R.string.download_completed),Toast.LENGTH_SHORT).show()

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
                    val pendingIntent = PendingIntent.getActivity(requireContext(), 0, intent,  PendingIntent.FLAG_IMMUTABLE)
                    val notification = NotificationCompat.Builder(requireContext(), channelId)
                        .setSmallIcon(R.mipmap.ic_launcher_round)
                        .setContentTitle("$fileName Downloaded")
                        .setContentText(getString(R.string.click_to_view_download_file))
                        .setContentIntent(pendingIntent)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setAutoCancel(true)
                        .build()
                    val notificationManager = NotificationManagerCompat.from(requireContext())
                    notificationManager.notify(notificationId, notification)

                }.addOnFailureListener { exception ->
                    Log.e("DOWNLOAD", "Download failed: $exception")
                    Toast.makeText(requireContext(),getString(R.string.download_fail),Toast.LENGTH_SHORT).show()
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

    private fun createNotificationChannel(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val notificationTitle = "Notification Title"
            val descriptionText = "Notification Description"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId,notificationTitle,importance).apply {
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