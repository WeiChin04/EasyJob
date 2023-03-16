package com.example.easyjob.user

import android.content.ContentValues
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SearchView
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.easyjob.R
import com.example.easyjob.databinding.FragmentUserSearchResultBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.FieldPosition
import java.util.*


class UserSearchResult : Fragment() {

    private var _binding: FragmentUserSearchResultBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var dbRef: DatabaseReference
    private lateinit var jobRecyclerView: RecyclerView
    private lateinit var jobArrayList: ArrayList<UserJobData>
    private lateinit var userJobAdapter: UserSearchJobAdapter
    private lateinit var customSpinner: CustomSpinner
    private var sortSpinnerPosition = 0
    private var salarySpinnerPosition = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentUserSearchResultBinding.inflate(inflater,container,false)
        (activity as UserHome).hideBottomNavigationView()
        jobRecyclerView = binding.myJobList
        jobRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        jobRecyclerView.setHasFixedSize(true)
        jobArrayList = arrayListOf<UserJobData>()
        userJobAdapter = UserSearchJobAdapter(jobArrayList)
        jobRecyclerView.adapter = userJobAdapter

        val spinner = binding.spJobType
        customSpinner = CustomSpinner(requireContext(), listOf("All","Internship", "Part Time", "Full Time"))
        spinner.adapter = customSpinner

        // 获取选中的位置
        val selectedPositions = customSpinner.getSelectedPositions()

        println("Selected positions: $selectedPositions")

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
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val filteredList = ArrayList<UserJobData>()
                        for (jobSnapshot in snapshot.children) {
                            val job = jobSnapshot.getValue(UserJobData::class.java)
                            if (job != null && job.jobTitle?.lowercase(Locale.ROOT)?.contains(query?.lowercase(Locale.ROOT) ?: "") == true) {
                                if (savedSearchResult != null) {
                                    filteredList.add(job)
                                }
                            }
                        }
                        val sharedPref = requireContext().getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
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
        salarySpinnerPosition =position
    }

    private fun updateRecyclerViewAdapter() {
        val filteredList = ArrayList<UserJobData>(jobArrayList)
        val defaultList = jobArrayList.toList()
        // 根据选定的 Spinner 选项筛选列表...
        if(sortSpinnerPosition == 0){
            filteredList.sortBy{it.currentDate}
        } else if (sortSpinnerPosition == 1) {
            filteredList.sortBy { it.ctr }
        } else if (sortSpinnerPosition == 2) {
            filteredList.sortByDescending { it.jobSalary }
        } else if (sortSpinnerPosition == 3) {
            filteredList.sortByDescending { it.currentDate }
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
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        savedInstanceState?.getInt("sortItem",0)?.let { binding.spSort.setSelection(it) }
        savedInstanceState?.getInt("salaryFilter",0)?.let { binding.spSalary.setSelection(it) }
    }
}