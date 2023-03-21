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
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.bumptech.glide.Glide
import com.example.easyjob.databinding.FragmentUserJobDetailBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*
import com.example.easyjob.R
import com.google.firebase.storage.FirebaseStorage

class UserJobDetailFragment : Fragment() {

    private  var _binding: FragmentUserJobDetailBinding? =null
    private val binding get() = _binding!!
    private var currentUser = FirebaseAuth.getInstance().currentUser!!.uid
    private lateinit var database: FirebaseDatabase
    private lateinit var dbRef: DatabaseReference
    private var isJobInFavorites = false


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment

        _binding = FragmentUserJobDetailBinding.inflate(inflater,container,false)
        database = FirebaseDatabase.getInstance()

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

        if(arguments?.getString("fromAppliedJob") == "yes"){
            binding.btnApplyJob.visibility = View.GONE
            binding.btnCancelJob.visibility = View.VISIBLE
        }

        val jobId = arguments?.getString("job_id")
        checkJobInFavorite()
        binding.btnFavoriteGray.setOnClickListener {

            dbRef = database.getReference("Users").child(currentUser).child("Favorites")
            dbRef.child(jobId.toString()).setValue(true)
                .addOnSuccessListener {
                    binding.btnFavoriteGray.visibility = View.GONE
                    Toast.makeText(requireContext(), getString(R.string.show_favorite_added), Toast.LENGTH_SHORT).show()
                }.addOnFailureListener{ exception ->
                    Log.e(ContentValues.TAG, "Failed to add job to favorites", exception)
                    Toast.makeText(requireContext(), "Failed to add job to favorites", Toast.LENGTH_SHORT).show()
                }
            addFavouriteCount(jobId!!)
        }

        binding.btnFavorite.setOnClickListener {
            binding.btnFavorite.visibility = View.GONE
//            binding.btnFavoriteGray.visibility = View.VISIBLE
            dbRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUser).child("Favorites")
            dbRef.child(jobId.toString()).removeValue()
            Toast.makeText(requireContext(), getString(R.string.show_favorite_removed), Toast.LENGTH_SHORT).show()
            minusFavouriteCount(jobId!!)
        }

        binding.btnApplyJob.setOnClickListener{
            checkApplication()
        }

        binding.btnCancelJob.setOnClickListener {
            val alertDialog = AlertDialog.Builder(requireContext())
            alertDialog.setTitle(getString(R.string.messageDialog_confirm))
            alertDialog.setMessage(requireContext().getString(R.string.show_cancel_application_confirm_message))
            alertDialog.setPositiveButton(getString(R.string.messageDialog_yes)) { _, _ ->
                cancelApplication()
            }
            alertDialog.setNegativeButton(getString(R.string.messageDialog_no)) { dialog, _ ->
                dialog.cancel()
            }
            alertDialog.show()
        }

        val employerId = arguments?.getString("employer_id")
        binding.btnViewCompanyInfo.setOnClickListener {
            val bundle = Bundle().apply {
                putString("employer_id", employerId)
        }
            it.findNavController().navigate(R.id.action_userJobDetailFragment_to_uerCompanyOverview, bundle)

        }

        dbRef = FirebaseDatabase.getInstance().reference

        val storageRef = FirebaseStorage.getInstance().reference
        val filePathAndName = "EmployerProfileImages/$employerId"
        val imageRef = storageRef.child(filePathAndName)

        imageRef.downloadUrl.addOnSuccessListener { uri ->
            Glide.with(this)
                .load(uri)
                .placeholder(R.drawable.ic_person)
                .error(R.drawable.ic_person)
                .into(binding.imgJob)
        }.addOnFailureListener {}


        return binding.root
    }


    private fun checkJobInFavorite(){

        val usersNode = "Users"
        val favoriteNode = "Favorites"
        val jobId = arguments?.getString("job_id")
        dbRef = FirebaseDatabase.getInstance().getReference("$usersNode/$currentUser/$favoriteNode/$jobId")

        dbRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                isJobInFavorites = snapshot.exists()
                updateFavoriteButtonIcon()
                Log.d("favorite", "status: $isJobInFavorites")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("JobDetailsPage", "Failed to read value.", error.toException())

            }

        })

    }

    private fun updateFavoriteButtonIcon() {
        if (isAdded) { // check if the fragment is still attached to an activity
            if (isJobInFavorites) {
                binding.btnFavorite.visibility = View.VISIBLE
                binding.btnFavoriteGray.visibility = View.GONE
            } else {
                binding.btnFavoriteGray.visibility = View.VISIBLE
                binding.btnFavorite.visibility = View.GONE
            }
        }
    }

    private fun addFavouriteCount(jobId: String) {

        Log.d("jobId", "status: $jobId")
        database = FirebaseDatabase.getInstance()
        dbRef = database.getReference("Analysis").child(jobId)

        val clickCountRef = dbRef.child("favouriteCount")

        clickCountRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                var favouriteCount = currentData.getValue(Int::class.java)
                if (favouriteCount == null) {
                    favouriteCount = 0
                }
                currentData.value = favouriteCount + 1
                return Transaction.success(currentData)
            }

            override fun onComplete(
                error: DatabaseError?,
                committed: Boolean,
                currentData: DataSnapshot?
            ) {
                if (error != null) {
                    Log.w(ContentValues.TAG, "Failed to read value.", error.toException())
                }
            }
        })
    }

    private fun minusFavouriteCount(jobId: String) {
        database = FirebaseDatabase.getInstance()
        dbRef = database.getReference("Analysis").child(jobId)

        val clickCountRef = dbRef.child("favouriteCount")

        clickCountRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                var favouriteCount = currentData.getValue(Int::class.java)
                if (favouriteCount == null) {
                    favouriteCount = 0
                }
                currentData.value = favouriteCount - 1
                return Transaction.success(currentData)
            }

            override fun onComplete(
                error: DatabaseError?,
                committed: Boolean,
                currentData: DataSnapshot?
            ) {
                if (error != null) {
                    Log.w(ContentValues.TAG, "Failed to read value.", error.toException())
                }
            }
        })
    }

    private fun cancelApplication() {

        dbRef = FirebaseDatabase.getInstance().reference
        val appliedJobId = arguments?.getString("application_id")
        val applicationRef = dbRef.child("Applications").child(appliedJobId.toString())

        applicationRef.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

                if(snapshot.exists()){
                    applicationRef.removeValue().addOnSuccessListener {
                        Toast.makeText(requireContext(), getString(R.string.cancel_successful_application), Toast.LENGTH_SHORT).show()
                        requireActivity().onBackPressed()
                    }.addOnFailureListener{
                        Toast.makeText(requireContext(), getString(R.string.cancel_fail_application), Toast.LENGTH_SHORT).show()
                    }
                }

            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Database error: ${error.message}", Toast.LENGTH_SHORT).show()

            }

        })
    }

    private fun checkApplication(){

        dbRef = database.getReference("Applications")
        val jobId = arguments?.getString("job_id")
        val appliedJobId = "$currentUser-$jobId"

        val ref = FirebaseDatabase.getInstance().getReference("Applications").child(appliedJobId)
        val status = "Pending"
        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
        val appliedAt = sdf.format(Date())


        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    Toast.makeText(requireContext(), getString(R.string.show_repeat_applied_job_message), Toast.LENGTH_SHORT).show()
                } else {
                    val newApplication = UserApplicationData(
                        jobId,
                        currentUser,
                        status,
                        appliedAt,
                        "",
                        "",
                        appliedJobId
                    )
                    dbRef.child(appliedJobId).setValue(newApplication)
                    Toast.makeText(requireContext(), getString(R.string.show_applied_success_message), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(ContentValues.TAG, "Failed to read value.", error.toException())
                Toast.makeText(requireContext(), "Failed to read value", Toast.LENGTH_SHORT).show()
            }
        })
    }

    @SuppressLint("SetTextI18n")
    override fun onResume() {
        super.onResume()
        val title = arguments?.getString("job_title")
        val salary = arguments?.getString("job_salary")
        val location = arguments?.getString("job_location")
        val jobType = arguments?.getString("job_type")
        val jobWorkingHourStar = arguments?.getString("job_working_hour_start")
        val jobWorkingHourEnd = arguments?.getString("job_working_hour_end")
        val requirement = arguments?.getString("job_requirement")
        val responsibilities = arguments?.getString("job_responsibilities")

        binding.tvJobDetailTitle.text = title
        binding.tvJobDetailSalary.text = "RM $salary"
        binding.tvJobDetailsWorkPlace.text = location
        binding.tvJobDetailsJobType.text = jobType
        binding.tvJobDetailsWorkingHourStart.text = jobWorkingHourStar
        binding.tvJobDetailsWorkingHourEnd.text = jobWorkingHourEnd
        binding.tvJobDetailRequirementContent.text = requirement
        binding.tvJobDetailResContent.text = responsibilities

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}