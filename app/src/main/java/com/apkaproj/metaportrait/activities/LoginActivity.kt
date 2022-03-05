package com.apkaproj.metaportrait.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.apkaproj.metaportrait.R
import com.apkaproj.metaportrait.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity()
{
    private lateinit var binding : ActivityLoginBinding
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.switchToSignUpButton.setOnClickListener {
            val intent = Intent(this@LoginActivity, SignUpActivity::class.java)
            startActivity(intent)
            finish()
        }
        binding.forgotPwdButton.setOnClickListener {
            val intent = Intent(this@LoginActivity,ForgotPasswordActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}