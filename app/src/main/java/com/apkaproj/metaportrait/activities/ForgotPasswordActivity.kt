package com.apkaproj.metaportrait.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.apkaproj.metaportrait.databinding.ActivityForgotPasswordBinding
import com.techiness.progressdialoglibrary.ProgressDialog

class ForgotPasswordActivity : AppCompatActivity()
{
    private lateinit var binding : ActivityForgotPasswordBinding
    private lateinit var progressDialog: ProgressDialog
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }
}