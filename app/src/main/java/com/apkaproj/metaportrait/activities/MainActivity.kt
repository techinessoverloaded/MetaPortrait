package com.apkaproj.metaportrait.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.apkaproj.metaportrait.databinding.ActivityMainBinding
import com.apkaproj.metaportrait.helpers.EncryptionUtils
import com.apkaproj.metaportrait.models.User
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source

class MainActivity : AppCompatActivity()
{
    private lateinit var binding : ActivityMainBinding
    private lateinit var mAuth : FirebaseAuth
    private lateinit var firestore : FirebaseFirestore
    private lateinit var user : FirebaseUser
    private lateinit var userId : String
    private lateinit var userName : String
    private lateinit var userObject : User
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mAuth = FirebaseAuth.getInstance()
        user = mAuth.currentUser!!
        firestore = FirebaseFirestore.getInstance()
        userId = user.uid
        firestore.collection("Users")
            .document(userId).get(Source.DEFAULT)
            .addOnCompleteListener {
                if(it.isSuccessful)
                {
                    userObject = it.result!!.toObject(User::class.java)!!
                    userName = EncryptionUtils.decrypt(userObject.name,userObject.tempKey)
                    binding.textView.text = "Welcome to MetaPortrait, $userName !"
                }
            }
    }
}