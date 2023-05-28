package com.example.easyjob.employer

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.bumptech.glide.Glide
import com.example.easyjob.R
import com.example.easyjob.TransactionHistoryData
import com.example.easyjob.WalletData
import com.example.easyjob.databinding.FragmentApplicantDetailsBinding
import com.example.easyjob.user.MySingleton
import com.example.easyjob.user.UserApplicationData
import com.example.easyjob.user.UserData
import com.example.easyjob.user.UserInformationFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.util.*

class ApplicantDetailsFragment : Fragment() {

    private  var _binding: FragmentApplicantDetailsBinding? =null
    private val binding get() = _binding!!
    private lateinit var dbRef: DatabaseReference
    private lateinit var database: FirebaseDatabase
    private val FCM_API = "https://fcm.googleapis.com/fcm/send"
    private val serverKey = "key=AAAAhetHQJI:APA91bHKV1nH51kVk_DDwNBODLC3jc6eUHVUGNo25t3Qg8ANqrpo_WmM6srPeXLmSC7UL7-YgqNBEyeCr98RoD92lvNn0LTxZ1_vzw6TUusqKSRHWd-yk8foP94zERZbmR3TQxH7iyKd"
    private val contentType = "application/json"
    private val TAG = "NOTIFICATION TAG"
    private var NOTIFICATION_TITLE = ""
    private var NOTIFICATION_MESSAGE = ""
    private var TOPIC = ""
    private var deviceToken: String? =null
    private var jobTitle: String? = null
    private var Deposit: Float? = null
    private val channelId = "Channel_id_01"

    @RequiresApi(Build.VERSION_CODES.O)
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

//        binding.tvUserResume.text = getString(R.string.empty_resume)

        //get resume file
        val applicantId = arguments?.getString("applicant_id")
        val pdfChillName = "PDFFiles/$applicantId.pdf"
        val pdfRef = storageRef.child(pdfChillName)

        pdfRef.downloadUrl.addOnSuccessListener {
            binding.tvUserResume.text = arguments?.getString("name") +"_" + getString(R.string.pdf_name)
        }.addOnFailureListener {}

        val downloadsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

        var fileName = arguments?.getString("name") +"_" + getString(R.string.pdf_name)
        var count = 1
        while (File(downloadsFolder, fileName).exists()) {
            fileName = arguments?.getString("name") +"_" +"Resume($count).pdf"
            count++
        }

        val file = File(downloadsFolder, fileName)
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
                    Log.e("DOWNLOAD", "Download failed: $applicantId")
                    Toast.makeText(requireContext(),"Download Failed", Toast.LENGTH_SHORT).show()
                }
            }
        }

        getJobTitle()
        getDeposit()

        deviceToken = arguments?.getString("deviceToken")

        //check job type
        val jobId = arguments?.getString("job_id")
        var jobType = ""
        dbRef = FirebaseDatabase.getInstance().reference
        dbRef = FirebaseDatabase.getInstance().getReference("Jobs/$jobId")
        dbRef.addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val job = snapshot.getValue(JobData::class.java)
                jobType = job?.jobType?.toString()?.replace("[", "")?.replace("]", "").toString()
                Log.d("jobtype","Show JobType: $jobType")
                if(jobType != "Temporary Work"){
                    binding.btnApplicantAttend.visibility = View.GONE
                    binding.btnApplicantAbsent.visibility = View.GONE
                }
                if(jobType == "Temporary Work" && arguments?.getString("apply_status") == "Approved" ){
                    binding.tvShowApproved.visibility = View.GONE
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.w(ContentValues.TAG, "Failed to read value.", error.toException())
                Toast.makeText(requireContext(), "Failed to read value", Toast.LENGTH_SHORT).show()
            }
        })


//        if(jobType != "Temporary Work"){
//            binding.btnApplicantAttend.visibility = View.GONE
//            binding.btnApplicantAbsent.visibility = View.GONE
//            //binding.tvShowApproved.visibility = View.VISIBLE
//        }

        if(arguments?.getString("apply_status") == "Rejected"){
            binding.btnApprove.visibility = View.GONE
            binding.btnReject.visibility = View.GONE
            binding.tvShowRejected.visibility = View.VISIBLE
        }else if (arguments?.getString("apply_status") == "Approved" ){

            binding.btnApprove.visibility = View.GONE
            binding.btnReject.visibility = View.GONE

            binding.btnApplicantAbsent.visibility = View.VISIBLE
            binding.btnApplicantAttend.visibility = View.VISIBLE

            if(jobType !="Temporary Work"){
                binding.tvShowApproved.visibility = View.VISIBLE
            }

            binding.btnApplicantAttend.setOnClickListener {
                depositTransferToApplicant()
            }

            binding.btnApplicantAbsent.setOnClickListener {
                depositRefundToEmployer()
            }

//        }else if (arguments?.getString("apply_status") == "Approved"){
//
//            binding.btnApprove.visibility = View.GONE
//            binding.btnReject.visibility = View.GONE
//            binding.tvShowApproved.visibility = View.VISIBLE

        }else if (arguments?.getString("apply_status") == "Completed"){

            binding.btnApprove.visibility = View.GONE
            binding.btnReject.visibility = View.GONE
            binding.btnApplicantAbsent.visibility = View.GONE
            binding.btnApplicantAttend.visibility = View.GONE

            binding.tvShowCompleted.visibility = View.VISIBLE
        }
        else{

            binding.btnApprove.visibility = View.VISIBLE
            binding.btnReject.visibility = View.VISIBLE
            binding.btnReject.setOnClickListener {
                val alertDialog = AlertDialog.Builder(requireContext())
                alertDialog.setTitle(getString(R.string.messageDialog_confirm))
                alertDialog.setMessage(getString(R.string.ask_confirm_reject)+arguments?.getString("name")+" ?")
                alertDialog.setPositiveButton(getString(R.string.messageDialog_yes)) { _, _ ->
                    rejectApplicant()
                    val message = getString(R.string.employer_reject)
                    TOPIC = deviceToken.toString()
                    NOTIFICATION_TITLE = jobTitle.toString()
                    NOTIFICATION_MESSAGE = message

                    val notification = JSONObject()
                    val notifcationBody = JSONObject()
                    try {
                        notifcationBody.put("title", NOTIFICATION_TITLE)
                        notifcationBody.put("message", NOTIFICATION_MESSAGE)
                        notification.put("to", TOPIC)
                        notification.put("data", notifcationBody)
                    } catch (e: JSONException) {
                        Log.e(TAG, "onCreate: " + e.message)
                    }
                    sendNotification(notification)
                }
                alertDialog.setNegativeButton(getString(R.string.messageDialog_no)) { dialog, _ ->
                    dialog.cancel()
                }
                alertDialog.show()
            }

            binding.btnApprove.setOnClickListener {

                //check job type
                val jobId = arguments?.getString("job_id")
                //var jobType = ""
                 dbRef = FirebaseDatabase.getInstance().reference
                dbRef = FirebaseDatabase.getInstance().getReference("Jobs/$jobId")
                dbRef.addListenerForSingleValueEvent(object :ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val job = snapshot.getValue(JobData::class.java)
                        val jobType = job?.jobType?.toString()?.replace("[", "")?.replace("]", "")
                        val jobSalary = job?.jobSalary?.toInt()
                        val employerId = job?.employerId.toString()
                        jobTitle = job?.jobTitle.toString()
                        Log.d("jobId","job Id: $jobId")
                        Log.d("jobType","type: $jobType")

                        if(jobType == "Temporary Work"){

                            Deposit = jobSalary?.times(0.3)?.toFloat()
                            val totalDeposit = String.format("%.2f", Deposit)


                            Log.d("deposit","total:RM $totalDeposit")
                            val alertDialog = AlertDialog.Builder(requireContext())
                            alertDialog.setTitle(getString(R.string.messageDialog_confirm))
                            alertDialog.setMessage(getString(R.string.approvedConfirmation)+arguments?.getString("name")+" ?\n"+
                                    getString(R.string.messageDialog_need_to_pay_deposit1)+totalDeposit)
                            alertDialog.setPositiveButton(getString(R.string.messageDialog_yes)) { _, _ ->

//                                validation(employerId)
                                //payment(employerId)
                                payment(employerId)


                            }
                            alertDialog.setNegativeButton(getString(R.string.messageDialog_no)) { dialog, _ ->
                                dialog.cancel()
                            }
                            alertDialog.show()
                        }
                        else{
                            Log.d("Run else","Run else")
                            val alertDialog = AlertDialog.Builder(requireContext())
                            alertDialog.setTitle(getString(R.string.messageDialog_confirm))
                            alertDialog.setMessage("Do you want to approve "+arguments?.getString("name")+" ?")
                            alertDialog.setPositiveButton(getString(R.string.messageDialog_yes)) { _, _ ->
                                approveApplicant()
                                sendNotificationToApplicant()

                            }
                            alertDialog.setNegativeButton(getString(R.string.messageDialog_no)) { dialog, _ ->
                                dialog.cancel()
                            }
                            alertDialog.show()
                        }

                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.w(ContentValues.TAG, "Failed to read value.", error.toException())
                        Toast.makeText(requireContext(), "Failed to read value", Toast.LENGTH_SHORT).show()
                    }

                })
            }
        }

        return binding.root
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

    private fun depositRefundToEmployer() {

        val alertDialog = AlertDialog.Builder(requireContext())
        alertDialog.setTitle(getString(R.string.messageDialog_confirm))
        alertDialog.setMessage(getString(R.string.messageDialog_deposit_will_refund))
        alertDialog.setPositiveButton(getString(R.string.messageDialog_yes)) { _, _ ->

            val currentId = FirebaseAuth.getInstance().currentUser!!.uid
            Log.d("employerId","employerId details； $currentId")
            val applicantRef = FirebaseDatabase.getInstance().getReference("Employers").child(currentId)
            var walletId = ""

            applicantRef.addListenerForSingleValueEvent(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val applicant = snapshot.getValue(EmployerData::class.java)
                    if(applicant != null){
                        walletId = applicant.walletId.toString()
                        Log.d("WalletId","wallet details； $walletId")
                        transferFund()

                    }

                }
                private fun transferFund() {
                    val totalDeposit = String.format("%.2f", Deposit)
                    val walletRef = FirebaseDatabase.getInstance().getReference("Wallets/$walletId")

                    walletRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val wallet = snapshot.getValue(WalletData::class.java)
                            if (wallet != null) {
                                val currentBalance = wallet.total_balance ?: 0f
                                val newBalance = currentBalance + Deposit!!

                                wallet.total_balance = newBalance
                                Log.d("CurrentBalance", "CurrentBalance $currentBalance")
                                Log.d("deposit", "deposit RM: $Deposit")

                                walletRef.setValue(wallet).addOnSuccessListener {
                                    // Deposit successfully saved to wallet balance
                                    Log.d("Wallet", "Deposit saved to wallet balance")
                                    updateStatus()
                                    generateTransactionHistoryForRefund(walletId,"ci")

                                }.addOnFailureListener { exception ->
                                    // Failed to save deposit to wallet balance
                                    Log.w("Wallet", "Error saving deposit to wallet balance", exception)
                                    Toast.makeText(requireContext(), "Failed to save deposit to wallet balance. Please try again later.", Toast.LENGTH_LONG).show()
                                }
                            } else {
                                // Wallet not found
                                Log.w("Wallet", "Wallet with ID $walletId not found")
                                Toast.makeText(requireContext(), "Wallet not found. Please try again later.", Toast.LENGTH_LONG).show()
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.w(TAG, "Failed to read value.", error.toException())
                            Toast.makeText(requireContext(), "Failed to read value.", Toast.LENGTH_LONG).show()
                        }
                    })
                }


                override fun onCancelled(error: DatabaseError) {
                    Log.w(TAG, "Failed to read value.", error.toException())
                    Toast.makeText(requireContext(), "Failed to read value.", Toast.LENGTH_LONG).show()
                }

            })


        }
        alertDialog.setNegativeButton(getString(R.string.messageDialog_no)) { dialog, _ ->
            dialog.cancel()
        }
        alertDialog.show()
    }

    private fun depositTransferToApplicant() {

        val alertDialog = AlertDialog.Builder(requireContext())
        alertDialog.setTitle(getString(R.string.messageDialog_confirm))
        alertDialog.setMessage(getString(R.string.messageDialog_deposit_will_transfer))
        alertDialog.setPositiveButton(getString(R.string.messageDialog_yes)) { _, _ ->

            val applicantId = arguments?.getString("applicant_id").toString()
            Log.d("applicationId","applicationId details； $applicantId")
            val applicantRef = FirebaseDatabase.getInstance().getReference("Users").child(applicantId)
            var walletId = ""

            applicantRef.addListenerForSingleValueEvent(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val applicant = snapshot.getValue(UserData::class.java)
                    if(applicant != null){
                        walletId = applicant.walletId.toString()
                        Log.d("WalletId","wallet details； $walletId")
                        transferFund()
                    }
                }
                private fun transferFund() {
                    val walletRef = FirebaseDatabase.getInstance().getReference("Wallets/$walletId")

                    walletRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val wallet = snapshot.getValue(WalletData::class.java)
                            if (wallet != null) {
                                val currentBalance = wallet.total_balance ?: 0f
                                val newBalance = currentBalance + Deposit!!

                                wallet.total_balance = newBalance
                                Log.d("CurrentBalance", "CurrentBalance $currentBalance")
                                Log.d("deposit", "deposit RM: $Deposit")



                                walletRef.setValue(wallet).addOnSuccessListener {
                                    // Deposit successfully saved to wallet balance
                                    Log.d("Wallet", "Deposit saved to wallet balance")
                                    updateStatus()
                                    generateTransactionHistory(walletId,"ci")

                                }.addOnFailureListener { exception ->
                                    // Failed to save deposit to wallet balance
                                    Log.w("Wallet", "Error saving deposit to wallet balance", exception)
                                    Toast.makeText(requireContext(), "Failed to save deposit to wallet balance. Please try again later.", Toast.LENGTH_LONG).show()
                                }
                            } else {
                                // Wallet not found
                                Log.w("Wallet", "Wallet with ID $walletId not found")
                                Toast.makeText(requireContext(), "Wallet not found. Please try again later.", Toast.LENGTH_LONG).show()
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.w(TAG, "Failed to read value.", error.toException())
                            Toast.makeText(requireContext(), "Failed to read value.", Toast.LENGTH_LONG).show()
                        }
                    })
                }


                override fun onCancelled(error: DatabaseError) {
                    Log.w(TAG, "Failed to read value.", error.toException())
                    Toast.makeText(requireContext(), "Failed to read value.", Toast.LENGTH_LONG).show()
                }

            })


        }
        alertDialog.setNegativeButton(getString(R.string.messageDialog_no)) { dialog, _ ->
            dialog.cancel()
        }
        alertDialog.show()


    }

    private fun sendNotificationToApplicant() {
        val message = getString(R.string.employer_approve)
        TOPIC = deviceToken.toString()
        NOTIFICATION_TITLE = jobTitle.toString()
        NOTIFICATION_MESSAGE = message

        val notification = JSONObject()
        val notifcationBody = JSONObject()
        try {
            notifcationBody.put("title", NOTIFICATION_TITLE)
            notifcationBody.put("message", NOTIFICATION_MESSAGE)
            notification.put("to", TOPIC)
            notification.put("data", notifcationBody)
        } catch (e: JSONException) {
            Log.e(TAG, "onCreate: " + e.message)
        }
        sendNotification(notification)
    }

    private fun payment(employerId: String) {

        val employerRef = FirebaseDatabase.getInstance().getReference("Employers").child(employerId)
        var walletId = ""

        employerRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val employer = snapshot.getValue(EmployerData::class.java)
                if (employer != null) {
                    walletId = employer.walletId.toString()
                    val employerName = employer.name.toString()
                    Log.d("WalletId","wallet details； $walletId" )
                    deductWallet()
                }
            }

            private fun deductWallet() {

                val walletRef = FirebaseDatabase.getInstance().getReference("Wallets/$walletId")

                walletRef.addListenerForSingleValueEvent(object :ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val wallet = snapshot.getValue(WalletData::class.java)
                        Log.d("WalletCheck","Wallet $wallet" )
                        if (wallet != null) {
                            val balance = wallet.total_balance

                            if ((balance ?: 0f) >= (Deposit ?: 0f)) {
                                val newBalance = balance!! - Deposit!!
                                wallet.total_balance = newBalance
                                walletRef.setValue(wallet)
                                Log.d("Deposit","Deposit :RM $Deposit" )
                                Log.d("TotalBalance","Wallet Balance :RM $balance" )
                                Log.d("NewBalance","Wallet New Balance :RM $newBalance" )
                                approveApplicant()
                                sendNotificationToApplicant()
                                generateTransactionHistory(walletId,"co")
                            }else{
                                Log.d("Wallet","Wallet $wallet" )
                                Log.d("TotalBalance","Wallet Balance :RM $balance" )
                                val message = "Your balance is insufficient to pay the deposit. Please top up your balance."
                                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
                            }

                        }else{
                            // show a message to indicate that the user does not have a wallet
                            Log.d("Wallet","wallet details； $wallet" )
                            val message = "Unable to complete the payment. Please try again later."
                            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.w(ContentValues.TAG, "Failed to read value.", error.toException())
                        Toast.makeText(requireContext(), "Failed to read value", Toast.LENGTH_SHORT).show()
                    }

                })

            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "Failed to read value.", error.toException())
                Toast.makeText(requireContext(), "Failed to read value.", Toast.LENGTH_LONG).show()
            }
        })


    }

    private fun generateTransactionHistory(walletId: String,historyType:String) {

        val subTitleDeposit = "Deposit"

        val currentTimeInMS = System.currentTimeMillis().toString()
        val status = "Successful"
        val transactionNo = UUID.randomUUID().toString()

        val newTransaction = TransactionHistoryData(
            jobTitle,
            subTitleDeposit,
            Deposit,
            currentTimeInMS,
            status,
            transactionNo,
            historyType
        )

        val historyRef = FirebaseDatabase.getInstance().getReference("WalletHistory/$walletId")
        val newHistoryRef = historyRef.push()
        newHistoryRef.setValue(newTransaction)



    }

    private fun generateTransactionHistoryForRefund(walletId: String,historyType:String) {

        val currentId = FirebaseAuth.getInstance().currentUser!!.uid
        val employerRef = FirebaseDatabase.getInstance().getReference("Employers/$currentId")

        employerRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val employer = snapshot.getValue(EmployerData::class.java)
                if (employer != null) {
                    Log.d("currentId","current Id: $currentId")

                    val currentTimeInMS = System.currentTimeMillis().toString()
                    val status = "Successful"
                    val transactionNo = UUID.randomUUID().toString()

                    val newTransaction = TransactionHistoryData(
                        jobTitle,
                        "Refund",
                        Deposit,
                        currentTimeInMS,
                        status,
                        transactionNo,
                        historyType
                    )

                    val historyRef = FirebaseDatabase.getInstance().getReference("WalletHistory/$walletId")
                    val newHistoryRef = historyRef.push()
                    newHistoryRef.setValue(newTransaction)
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "Failed to read value.", error.toException())
                Toast.makeText(requireContext(), "Failed to read value.", Toast.LENGTH_LONG).show()
            }
        })

    }

    private fun approveApplicant() {
        dbRef = FirebaseDatabase.getInstance().reference
        val currentTimeInMS = System.currentTimeMillis().toString()
        val applicationId = arguments?.getString("application_id")
        val applicationRef = dbRef.child("Applications").child(applicationId.toString())

        applicationRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val application = snapshot.getValue(UserApplicationData::class.java)

                application?.apply {
                    status = "Approved"
                    approvedAt = currentTimeInMS
                }

                applicationRef.setValue(application).addOnCompleteListener {
                    if (it.isSuccessful) {
                        val deviceToken = arguments?.getString("deviceToken")
                        Log.d("UserId", "UserId-JobId: $applicationId")
                        Toast.makeText(requireContext(), getString(R.string.show_approved), Toast.LENGTH_SHORT).show()
                        requireActivity().onBackPressed()
                        analysisApproved()
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

    private fun analysisApproved() {
        val jobId = arguments?.getString("job_id")
        Log.d("jobId", "status: $jobId")
        database = FirebaseDatabase.getInstance()
        dbRef = database.getReference("Analysis").child(jobId.toString())
        val approvedCountRef = dbRef.child("totalApproved")
        approvedCountRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                var approveCount = currentData.getValue(Int::class.java)
                if (approveCount == null) {
                    approveCount = 0
                }
                currentData.value = approveCount + 1

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

    private fun rejectApplicant() {

        dbRef = FirebaseDatabase.getInstance().reference
        val currentTimeInMS = System.currentTimeMillis().toString()
        val applicationId = arguments?.getString("application_id")
        val applicationRef = dbRef.child("Applications").child(applicationId.toString())

        applicationRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val application = snapshot.getValue(UserApplicationData::class.java)

                application?.apply {
                    status = "Rejected"
                    rejectAt = currentTimeInMS
                }

                applicationRef.setValue(application).addOnCompleteListener {
                    if (it.isSuccessful) {
                        Toast.makeText(requireContext(), "You have rejected the applicant", Toast.LENGTH_SHORT).show()
                        requireActivity().onBackPressed()
                        analysisRejected()
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

    private fun analysisRejected() {
        val jobId = arguments?.getString("job_id")
        Log.d("jobId", "status: $jobId")
        database = FirebaseDatabase.getInstance()
        dbRef = database.getReference("Analysis").child(jobId.toString())
        val rejectedCountRef = dbRef.child("totalRejected")
        rejectedCountRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                var rejectedCount = currentData.getValue(Int::class.java)
                if (rejectedCount == null) {
                    rejectedCount = 0
                }
                currentData.value = rejectedCount + 1

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

    private fun getJobTitle(){
        val jobId = arguments?.getString("job_id")
        val jobNode = "Jobs"

        dbRef = FirebaseDatabase.getInstance().getReference("$jobNode/$jobId")
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val job = snapshot.getValue(JobData::class.java)
                jobTitle = job!!.jobTitle.toString()
                Log.d("Token", "Job Title: $jobTitle")
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(),"Failed to get device token",Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun getDeposit() {
        //check job type
        val jobId = arguments?.getString("job_id")
        //var jobType = ""
        dbRef = FirebaseDatabase.getInstance().reference
        dbRef = FirebaseDatabase.getInstance().getReference("Jobs/$jobId")
        dbRef.addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val job = snapshot.getValue(JobData::class.java)
                val jobSalary = job?.jobSalary?.toInt()
                val employerId = job?.employerId.toString()
                val employerName = job
                jobTitle = job?.jobTitle.toString()
                Log.d("jobId","job Id: $jobId")

                Deposit = jobSalary?.times(0.3)?.toFloat()
                val totalDeposit = String.format("%.2f", Deposit)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(ContentValues.TAG, "Failed to read value.", error.toException())
                Toast.makeText(requireContext(), "Failed to read value", Toast.LENGTH_SHORT).show()
            }

        })
    }

    private fun updateStatus(){

        dbRef = FirebaseDatabase.getInstance().reference
        val currentTimeInMS = System.currentTimeMillis().toString()
        val applicationId = arguments?.getString("application_id")
        val applicationRef = dbRef.child("Applications").child(applicationId.toString())

        applicationRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val application = snapshot.getValue(UserApplicationData::class.java)

                application?.apply {
                    status = "Completed"
                    completedAt = currentTimeInMS
                }

                applicationRef.setValue(application).addOnCompleteListener {
                    if (it.isSuccessful) {
                        Log.d("UserId", "UserId-JobId: $applicationId")
                        Toast.makeText(requireContext(), getString(R.string.show_job_completed), Toast.LENGTH_SHORT).show()
                        requireActivity().onBackPressed()
                        analysisApproved()
                    } else {
                        Toast.makeText(requireContext(), "Something error, this application cannot be updated", Toast.LENGTH_SHORT).show()
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