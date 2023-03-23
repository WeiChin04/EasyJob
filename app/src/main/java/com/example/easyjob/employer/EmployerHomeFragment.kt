package com.example.easyjob.employer

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.example.easyjob.MainActivity
import com.example.easyjob.R
import com.example.easyjob.databinding.FragmentEmployerHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class EmployerHomeFragment : Fragment() {

    private var _binding: FragmentEmployerHomeBinding? =null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var dbref: DatabaseReference
    private lateinit var uid: String
    private lateinit var employer: EmployerData
    private lateinit var employerDataViewModel: EmployerDataViewModel


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEmployerHomeBinding.inflate(inflater,container,false)

        auth = FirebaseAuth.getInstance()

        //get user data from view model
        employerDataViewModel = ViewModelProvider(requireActivity())[EmployerDataViewModel::class.java]

        employerDataViewModel.getData(auth.currentUser!!.uid)
        employerDataViewModel.employerData.observe(viewLifecycleOwner) { employerData ->
            binding.tvtest.text = employerData.email
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        binding.button2.setOnClickListener {
            auth.signOut()
            activity?.let{
                val intent = Intent (it, MainActivity::class.java).also {
                    it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                it.startActivity(intent)
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}