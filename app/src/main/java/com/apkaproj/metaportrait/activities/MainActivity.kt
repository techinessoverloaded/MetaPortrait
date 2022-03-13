package com.apkaproj.metaportrait.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.apkaproj.metaportrait.databinding.ActivityMainBinding
import com.apkaproj.metaportrait.helpers.EncryptionUtils
import com.apkaproj.metaportrait.helpers.PreferenceUtils
import com.apkaproj.metaportrait.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import com.techiness.progressdialoglibrary.ProgressDialog

class MainActivity : AppCompatActivity()
{
    private lateinit var binding : ActivityMainBinding
    private lateinit var mAuth : FirebaseAuth
    private lateinit var firestore : FirebaseFirestore
    private lateinit var user : FirebaseUser
    private lateinit var userId : String
    private lateinit var userName : String
    private lateinit var userObject : User

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mAuth = FirebaseAuth.getInstance()
        user = mAuth.currentUser!!
        firestore = FirebaseFirestore.getInstance()
        userId = user.uid
    }

    fun getData(source : Source)
    {
        firestore.collection("Users")
            .document(userId).get(source)
            .addOnCompleteListener {
                if (it.isSuccessful)
                {
                    userObject = it.result!!.toObject(User::class.java)!!
                    userName = EncryptionUtils.decrypt(userObject.name, userObject.tempKey)
                    binding.textTitle.text = "Welcome to MetaPortrait, $userName !"
                }
                else
                {
                    binding.textTitle.text = "Welcome to MetaPortrait, User !"
                }
            }
    }

    override fun onResume()
    {
        super.onResume()
        val preferenceUtils = PreferenceUtils.getInstance(this)
        if(preferenceUtils.needsDbUpdate)
        {
            getData(Source.SERVER)
            preferenceUtils.needsDbUpdate = false
        }
        else
        {
            getData(Source.CACHE)
        }
    }
}