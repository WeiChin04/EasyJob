package com.example.easyjob.user

import android.app.AlertDialog
import android.content.ContentValues
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.easyjob.databinding.FragmentUserSearchResultBinding
import com.example.easyjob.employer.JobData
import com.google.firebase.database.*
import java.util.*
import kotlin.collections.ArrayList


class UserSearchResult : Fragment() {

    private var _binding: FragmentUserSearchResultBinding? = null
    private val binding get() = _binding!!
    private lateinit var dbRef: DatabaseReference
    private lateinit var jobRecyclerView: RecyclerView
    private lateinit var jobArrayList: ArrayList<JobData>
    private lateinit var userJobAdapter: UserSearchJobAdapter
    private var sortSpinnerPosition = 0
    private var salarySpinnerPosition = 0
    private lateinit var selectedType: BooleanArray

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        jobRecyclerView = binding.myJobList
        jobRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        jobRecyclerView.setHasFixedSize(true)
        jobArrayList = arrayListOf<JobData>()
        userJobAdapter = UserSearchJobAdapter(jobArrayList)
        jobRecyclerView.adapter = userJobAdapter

        val sharedPref = requireContext().getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
        val savedSearchResult = sharedPref.getString("search_result", "")
        filterList(savedSearchResult)

        binding.searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                filterList(query)
                binding.spSort.setSelection(0)
                binding.spSalary.setSelection(0)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }

        })

        binding.spSort.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                sortItemList(position)
                updateRecyclerViewAdapter()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }

        binding.spSalary.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                filterSalary(position)
                updateRecyclerViewAdapter()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }

        jobType()
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserSearchResultBinding.inflate(inflater,container,false)
        (activity as UserHome).hideBottomNavigationView()

        binding.tvBackArrow.setOnClickListener {
            requireActivity().onBackPressed()
        }

        return binding.root
    }

    private fun filterList(query: String?) {

        dbRef = FirebaseDatabase.getInstance().getReference("Jobs")
        val sharedPref = requireContext().getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
        val savedSearchResult = sharedPref.getString("search_result", "")

        val status = "Available"
        dbRef.orderByChild("jobStatus").equalTo(status)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val filteredList = ArrayList<JobData>()
                        for (jobSnapshot in snapshot.children) {
                            val job = jobSnapshot.getValue(JobData::class.java)
                            if (job != null && job.jobTitle?.lowercase(Locale.ROOT)?.contains(query?.lowercase(Locale.ROOT) ?: "") == true) {
                                if (savedSearchResult != null) {
                                    filteredList.add(job)
                                }
                            }
                        }
                        val editor = sharedPref.edit()
                        editor.putString("search_result", query)
                        editor.apply()
                        jobArrayList.clear()
                        jobArrayList.addAll(filteredList)
                        userJobAdapter.setFilteredList(filteredList)
                        jobRecyclerView.adapter = userJobAdapter

                        if (filteredList.isEmpty()) {
                            binding.tvNoAppliedJobShow.visibility = View.VISIBLE
                        } else {
                            binding.tvNoAppliedJobShow.visibility = View.GONE
                        }
                    } else {
                        binding.myJobList.visibility = View.GONE
                        binding.tvNoAppliedJobShow.visibility = View.VISIBLE
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.w(ContentValues.TAG, "Failed to read value.", error.toException())
                    Toast.makeText(requireContext(), "Failed to read value", Toast.LENGTH_SHORT)
                        .show()
                }
            })
    }

    private fun sortItemList(position: Int) {
        sortSpinnerPosition = position
    }

    private fun filterSalary(position: Int){
        salarySpinnerPosition = position
    }

    private fun jobType(){
        val langList: ArrayList<Int> = ArrayList()
        val jobTypeArray = arrayOf("Internship","Part Time","Full Time")
        val jobType= binding.jobType
        selectedType = BooleanArray(jobTypeArray.size)

        jobType.setOnClickListener {
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Select Job Type")
            builder.setCancelable(false)
            builder.setMultiChoiceItems(jobTypeArray, selectedType) { _, i, b ->
                if (b) {
                    langList.add(i)
                    langList.sort()
                } else {
                    langList.remove(Integer.valueOf(i))
                }
            }
            builder.setPositiveButton("OK") { _, _ ->
                val stringBuilder = StringBuilder()
                for (j in langList.indices) {
                    stringBuilder.append(jobTypeArray[langList[j]])
                    if (j != langList.size - 1) {
                        stringBuilder.append(", ")
                    }
                }
                if (stringBuilder.isEmpty()) {
                    jobType.text = "All"
                } else {
                    jobType.text = stringBuilder.toString()
                }
                updateRecyclerViewAdapter()
            }
            builder.setNegativeButton("Cancel") { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
            builder.setNeutralButton("Clear All") { _, _ ->
                for (j in selectedType.indices) {
                    selectedType[j] = false
                }
                langList.clear()
                jobType.text = "All"
                updateRecyclerViewAdapter()
            }
            builder.show()
        }
    }

    private fun updateRecyclerViewAdapter() {
        val filteredList = ArrayList<JobData>(jobArrayList)
        val jobTypeText = binding.jobType.text
        val selectedJobTypes = binding.jobType.text.split(", ")


        if (jobTypeText != "All") {
            filteredList.retainAll{
                val jobType = it.jobType.toString()
                selectedJobTypes.any { selectedJobType -> jobType.contains(selectedJobType) }
            }
        }else if (jobTypeText == "All" || selectedJobTypes.isEmpty()) {
            filteredList.clear()
            filteredList.addAll(jobArrayList)
        }

        // 根据选定的 Spinner 选项筛选列表...
        when (sortSpinnerPosition) {
            0 -> {
                filteredList.sortBy{it.currentDate}
            }
            1 -> {
//                filteredList.sortBy { it.ctr }
            }
            2 -> {
                filteredList.sortByDescending { it.jobSalary?.toDoubleOrNull() ?: Double.POSITIVE_INFINITY }
            }
            3 -> {
                filteredList.sortByDescending { it.currentDate }
            }
        }

        if (salarySpinnerPosition > 0) {
            filteredList.retainAll {
                val salary = it.jobSalary!!.toDoubleOrNull()
                when (salarySpinnerPosition) {
                    1 -> salary != null && salary >= 1000
                    2 -> salary != null && salary >= 3000
                    3 -> salary != null && salary >= 5000
                    4 -> salary != null && salary >= 7000
                    5 -> salary != null && salary >= 10000
                    else -> true
                }
            }
        }

        userJobAdapter = UserSearchJobAdapter(filteredList)
        jobRecyclerView.adapter = userJobAdapter
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("sortItem",binding.spSort.selectedItemPosition)
        outState.putInt("salaryFilter",binding.spSalary.selectedItemPosition)
//        outState.putBooleanArray("selectedJobType", selectedType)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        savedInstanceState?.getInt("sortItem",0)?.let { binding.spSort.setSelection(it) }
        savedInstanceState?.getInt("salaryFilter",0)?.let { binding.spSalary.setSelection(it) }
//        if (savedInstanceState != null) {
//            selectedType = savedInstanceState.getBooleanArray("selectedJobType") ?: BooleanArray(3)
//            updateRecyclerViewAdapter()
//        }else{
//            selectedType = BooleanArray(jobTypeArray.size)
//        }
    }
}