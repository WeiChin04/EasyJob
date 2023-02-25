package com.example.easyjob.employer

import android.os.Binder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.easyjob.databinding.ActivityJobDetailBinding
import com.example.easyjob.databinding.ActivityUserHomeBinding

class JobDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityJobDetailBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityJobDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val title = intent.getStringExtra("job_title")

        binding.jobTitletest.text = title
    }
}