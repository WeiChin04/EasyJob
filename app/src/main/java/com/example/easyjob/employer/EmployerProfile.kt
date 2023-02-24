package com.example.easyjob.employer

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.easyjob.MainActivity
import com.example.easyjob.databinding.FragmentEmployerProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class EmployerProfile : Fragment() {
    private var _binding: FragmentEmployerProfileBinding? =null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var dbref: DatabaseReference
    private lateinit var uid: String
    private lateinit var employer: EmployerData

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEmployerProfileBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}