package com.apkaproj.metaportrait.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.apkaproj.metaportrait.R
import com.apkaproj.metaportrait.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity()
{
    private lateinit var binding : ActivitySplashBinding
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}