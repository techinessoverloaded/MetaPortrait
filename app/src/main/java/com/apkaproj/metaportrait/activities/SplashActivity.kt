package com.apkaproj.metaportrait.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.apkaproj.metaportrait.databinding.ActivitySplashBinding
import com.apkaproj.metaportrait.helpers.PreferenceUtils
import com.google.firebase.auth.FirebaseAuth
import com.apkaproj.metaportrait.R

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity()
{
    private lateinit var binding : ActivitySplashBinding
    private lateinit var handler : Handler
    private lateinit var mAuth : FirebaseAuth
    private lateinit var bounceAnimation: Animation

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mAuth = FirebaseAuth.getInstance()
        handler = Handler(mainLooper)
        bounceAnimation = AnimationUtils.loadAnimation(this, R.anim.logo_bounce_anim)
    }

    override fun onStart()
    {
        super.onStart()
        binding.logoView.startAnimation(bounceAnimation)
        binding.titleView.startAnimation(bounceAnimation)
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