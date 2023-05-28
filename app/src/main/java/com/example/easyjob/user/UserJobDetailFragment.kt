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
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.bumptech.glide.Glide
import com.example.easyjob.databinding.FragmentUserJobDetailBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.example.easyjob.R
import com.example.easyjob.employer.EmployerData
import com.google.firebase.storage.FirebaseStorage
import org.json.JSONException
import org.json.JSONObject

class UserJobDetailFragment : Fragment() {

    private var _binding: FragmentUserJobDetailBinding? =null
    private val binding get() = _binding!!
    private var currentUser = FirebaseAuth.getInstance().currentUser!!.uid
    private lateinit var database: FirebaseDatabase
    private lateinit var dbRef: DatabaseReference
    private var isJobInFavorites = false
    private val FCM_API = "https://fcm.googleapis.com/fcm/send"
    private val serverKey = "key=AAAAhetHQJI:APA91bHKV1nH51kVk_DDwNBODLC3jc6eUHVUGNo25t3Qg8ANqrpo_WmM6srPeXLmSC7UL7-YgqNBEyeCr98RoD92lvNn0LTxZ1_vzw6TUusqKSRHWd-yk8foP94zERZbmR3TQxH7iyKd"
    private val contentType = "application/json"
    private val TAG = "NOTIFICATION TAG"
    private var NOTIFICATION_TITLE = ""
    private var NOTIFICATION_MESSAGE = ""
    private var TOPIC = ""
    private var deviceToken: String? =null


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
        }else{
            binding.btnApplyJob.visibility = View.VISIBLE
        }

        Log.d("JobTypeShow","show here: $arguments?.getString(\"apply_status\"")

        if(arguments?.getString("apply_status") == "Approved" &&
            arguments?.getString("job_type") != "Temporary Work"){
            binding.btnCancelJob.visibility = View.GONE
            binding.tvShowMessage.visibility = View.VISIBLE
            binding.tvShowMessage.text = getString(R.string.employer_approved_job)
        }else if (arguments?.getString("apply_status") == "Pending"){
            binding.btnCancelJob.visibility = View.VISIBLE
            binding.tvShowMessage.visibility = View.GONE
            binding.tvShowMessage.text = getString(R.string.employer_approved_job)
        }else if (arguments?.getString("apply_status") == "Approved" &&
            arguments?.getString("job_type") == "Temporary Work"){
            binding.btnCancelJob.visibility = View.GONE
            binding.tvShowMessage.visibility = View.VISIBLE
            binding.tvShowMessage.text = getString(R.string.employer_paid_deposit)
        }else if (arguments?.getString("apply_status") == "Rejected"){
            binding.btnCancelJob.visibility = View.GONE
            binding.tvShowMessage.visibility = View.VISIBLE
            binding.tvShowMessage.text = getString(R.string.employer_reject)
        }
        else{
            binding.btnCancelJob.visibility = View.GONE
            binding.tvShowMessage.visibility = View.GONE
            binding.tvShowMessage.text = getString(R.string.employer_paid_deposit)
        }

        if(arguments?.getString("apply_status") == "Completed"){
            binding.btnCancelJob.visibility = View.GONE
            binding.tvShowMessage.visibility = View.VISIBLE
            binding.tvShowMessage.text = getString(R.string.deposit_received)
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

        val employerId = arguments?.getString("employer_id")

        val jobTitle = arguments?.getString("job_title")
        val message = getString(R.string.new_applicants)

        getDeviceToken()
        binding.btnApplyJob.setOnClickListener{
            checkApplication()
            //send notification
            val appliedJobId = "$currentUser-$jobId"

            val ref = FirebaseDatabase.getInstance().getReference("Applications").child(appliedJobId)

            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.show_repeat_applied_job_message),
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        TOPIC = deviceToken.toString()
                        NOTIFICATION_TITLE = jobTitle.toString()
                        NOTIFICATION_MESSAGE = message

                        val notification = JSONObject()
                        val notificationBody = JSONObject()
                        try {
                            notificationBody.put("title", NOTIFICATION_TITLE)
                            notificationBody.put("message", NOTIFICATION_MESSAGE)
                            notification.put("to", TOPIC)
                            notification.put("data", notificationBody)
                        } catch (e: JSONException) {
                            Log.e(TAG, "onCreate: " + e.message)
                        }
                        sendNotification(notification)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.w(ContentValues.TAG, "Failed to read value.", error.toException())
                    Toast.makeText(requireContext(), "Failed to read value", Toast.LENGTH_SHORT).show()
                }
            })
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
        if (isAdded) {
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
                currentData: DataSnapshot?,
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
                        analysisCancelJob()
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

    private fun analysisCancelJob() {
        val jobId = arguments?.getString("job_id")
        Log.d("jobId", "status: $jobId")
        database = FirebaseDatabase.getInstance()
        dbRef = database.getReference("Analysis").child(jobId.toString())
        val clickCountRef = dbRef.child("totalCancel")
        clickCountRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                var cancelCount = currentData.getValue(Int::class.java)
                if (cancelCount == null) {
                    cancelCount = 0
                }
                currentData.value = cancelCount + 1

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

    private fun checkApplication(){

        dbRef = database.getReference("Applications")
        val jobId = arguments?.getString("job_id")
        val appliedJobId = "$currentUser-$jobId"

        val ref = FirebaseDatabase.getInstance().getReference("Applications").child(appliedJobId)
        val status = "Pending"
        val currentTime = System.currentTimeMillis().toString()


        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    Toast.makeText(requireContext(), getString(R.string.show_repeat_applied_job_message), Toast.LENGTH_SHORT).show()
                } else {
                    val newApplication = UserApplicationData(
                        jobId,
                        currentUser,
                        status,
                        currentTime,
                        "",
                        "",
                        appliedJobId,
                        ""
                    )
                    dbRef.child(appliedJobId).setValue(newApplication)
                    Toast.makeText(requireContext(), getString(R.string.show_applied_success_message), Toast.LENGTH_SHORT).show()

                    analysisApplyJob()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(ContentValues.TAG, "Failed to read value.", error.toException())
                Toast.makeText(requireContext(), "Failed to read value", Toast.LENGTH_SHORT).show()
            }
        })

    }

    private fun analysisApplyJob() {
        val jobId = arguments?.getString("job_id")
        Log.d("jobId", "status: $jobId")
        database = FirebaseDatabase.getInstance()
        dbRef = database.getReference("Analysis").child(jobId.toString())
        val currentTime = System.currentTimeMillis().toString()
        val applyCountRef = dbRef.child("totalApply")
        val lastApplyTimeRef = dbRef.child("lastApplyAt")
        applyCountRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                var applyCount = currentData.getValue(Int::class.java)
                if (applyCount == null) {
                    applyCount = 0
                }
                currentData.value = applyCount + 1

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
                if (committed) {
                    lastApplyTimeRef.setValue(currentTime)
                }
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

    private fun sendNotification(notification: JSONObject) {
        val jsonObjectRequest: JsonObjectRequest = object : JsonObjectRequest(
            Method.POST,
            FCM_API,
            notification,
            Response.Listener { response ->
                Log.i(TAG, "onResponse: $response")
            },
            Response.ErrorListener {
                Toast.makeText(requireContext(), "Request error", Toast.LENGTH_LONG).show()
                Log.i(TAG, "onErrorResponse: Didn't work")
            }) {
            override fun getHeaders(): MutableMap<String, String> {
                val params: MutableMap<String, String> = HashMap()
                params["Authorization"] = serverKey
                params["Content-Type"] = contentType
                return params
            }
        }
        MySingleton.getInstance(requireContext()).addToRequestQueue(jsonObjectRequest)
    }

    private fun getDeviceToken(){
        val employerId = arguments?.getString("employer_id")
        val employerNode = "Employers"

        dbRef = FirebaseDatabase.getInstance().getReference("$employerNode/$employerId")
        dbRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val employer = snapshot.getValue(EmployerData::class.java)
                    deviceToken = employer!!.deviceToken.toString()
                    Log.d("token", "token: $deviceToken")
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(),"Failed to get device token",Toast.LENGTH_SHORT).show()
                }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}