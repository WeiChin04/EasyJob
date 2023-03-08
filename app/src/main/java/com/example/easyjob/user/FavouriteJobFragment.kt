package com.example.easyjob.user

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.easyjob.R
import com.example.easyjob.databinding.FragmentFavouriteJobBinding
import com.example.easyjob.databinding.FragmentUserHomeBinding

class FavouriteJobFragment : Fragment() {

    private var _binding : FragmentFavouriteJobBinding? = null
    private val binding get() = _binding!!



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentFavouriteJobBinding.inflate(inflater, container, false)
        return binding.root
    }

}