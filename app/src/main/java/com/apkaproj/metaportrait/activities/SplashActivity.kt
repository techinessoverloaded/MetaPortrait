package com.apkaproj.metaportrait.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.apkaproj.metaportrait.databinding.ActivitySplashBinding
import com.apkaproj.metaportrait.helpers.PreferenceUtils
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity()
{
    private lateinit var binding : ActivitySplashBinding
    private lateinit var handler : Handler
    private lateinit var mAuth : FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mAuth = FirebaseAuth.getInstance()

        handler = Handler(mainLooper)
        val runnable = Runnable {
            lateinit var intent : Intent
            val user = mAuth.currentUser
            if(user == null)
            {
                val preferenceUtils = PreferenceUtils.getInstance(this)
                if (preferenceUtils.isFirstTime)
                {
                    intent = Intent(this@SplashActivity, SignUpActivity::class.java)
                    startActivity(intent)
                    preferenceUtils.isFirstTime = false
                    finish()
                }
                else
                {
                    intent = Intent(this@SplashActivity, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
            else
            {
                intent = Intent(this@SplashActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
        handler.postDelayed(runnable,2600)
    }
}