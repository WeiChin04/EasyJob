package com.example.easyjob.user

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.easyjob.R
import com.example.easyjob.databinding.FragmentUserMyWalletBinding

class UserMyWallet : Fragment() {

    private  var _binding: FragmentUserMyWalletBinding? =null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentUserMyWalletBinding.inflate(inflater,container,false)
        // Inflate the layout for this fragment
        return binding.root
    }

}