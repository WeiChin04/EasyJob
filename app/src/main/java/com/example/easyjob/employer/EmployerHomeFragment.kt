package com.example.easyjob.employer
import android.annotation.SuppressLint
import android.content.ContentValues
import android.os.Bundle
import android.text.method.TextKeyListener.clear
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.easyjob.AnalysisData
import com.example.easyjob.databinding.FragmentEmployerHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.core.RepoManager.clear
import java.util.ArrayList
class EmployerHomeFragment : Fragment() {

    private var _binding: FragmentEmployerHomeBinding? =null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var dbRef: DatabaseReference
    private lateinit var uid: String
    private lateinit var jobAnalysisRecyclerView: RecyclerView
    private lateinit var jobAnalysisArrayList: ArrayList<AnalysisData>
    private lateinit var jobDataArrayList: ArrayList<JobData>
    private lateinit var employerDataViewModel: EmployerDataViewModel
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        (activity as EmployerHome).showBottomNavigationView()


        super.onViewCreated(view, savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEmployerHomeBinding.inflate(inflater,container,false)

        auth = FirebaseAuth.getInstance()
        //get user data from view model
        employerDataViewModel = ViewModelProvider(requireActivity())[EmployerDataViewModel::class.java]
        employerDataViewModel.getData(auth.currentUser!!.uid)

        jobAnalysisRecyclerView = binding.myJobAnalysis
        jobAnalysisRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        jobAnalysisRecyclerView.setHasFixedSize(true)
        jobAnalysisArrayList = arrayListOf<AnalysisData>()
        jobDataArrayList = arrayListOf<JobData>()
        getAnalysisData()

        binding.swipeRefreshLayout.setOnRefreshListener {
            // Refresh data here
            refreshData()
        }

        return binding.root

    }

    @SuppressLint("NotifyDataSetChanged")
    private fun refreshData() {
        jobAnalysisArrayList.clear()
        jobDataArrayList.clear()
        getAnalysisData()
        jobAnalysisRecyclerView.adapter?.notifyDataSetChanged()
        binding.swipeRefreshLayout.isRefreshing = false
    }


    private fun getAnalysisData() {

        val currentId = FirebaseAuth.getInstance().currentUser?.uid
        val jobIds = mutableListOf<String>()
        dbRef = FirebaseDatabase.getInstance().getReference("Jobs")
        dbRef.orderByChild("employerId").equalTo(currentId)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()){
                        for(jobSnapshot in snapshot.children){
                            val myJob = jobSnapshot.getValue(JobData::class.java)
                            jobDataArrayList.add(myJob!!)
                            val jobId = myJob.jobId
                            jobId?.let { jobIds.add(it) }
                        }
                        jobAnalysisArrayList.clear()
                        val analysisRef  = FirebaseDatabase.getInstance().getReference("Analysis")
                        for(jobId in jobIds){
                            val analysisQuery = analysisRef.orderByKey().equalTo(jobId)
                            analysisQuery.addListenerForSingleValueEvent(object : ValueEventListener{
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    if(snapshot.exists()){
                                        for(analysisSnapshot in snapshot.children){
                                            val analysisKey =  analysisSnapshot.getValue(AnalysisData::class.java)
                                            jobAnalysisArrayList.add(analysisKey!!)
                                        }
                                    }
                                    jobAnalysisRecyclerView.adapter = jobAnalysisAdapter(jobDataArrayList,jobAnalysisArrayList)
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    Log.w(ContentValues.TAG, "Failed to read value.", error.toException())
                                    Toast.makeText(requireContext(), "Failed to read value", Toast.LENGTH_SHORT)
                                        .show()
                                }

                            })
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.w(ContentValues.TAG, "Failed to read value.", error.toException())
                    Toast.makeText(requireContext(), "Failed to read value", Toast.LENGTH_SHORT)
                        .show()
                }

            })
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}