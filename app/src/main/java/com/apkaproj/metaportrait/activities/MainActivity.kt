package com.apkaproj.metaportrait.activities

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.registerForActivityResult
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
    companion object {
        private const val REQUEST_CODE_PICK_IMAGE = 1
        private const val REQUEST_CODE = "REQUEST_CODE"
        const val KEY_IMAGE_URI = "imageUri"
    }

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
        setListeners()
    }

    private fun getData(source : Source)
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

    private fun setListeners()
    {
        val getContentUriFromActivity = registerForActivityResult(ActivityResultContracts.GetContent())
        {
            imageUri : Uri? ->
            imageUri?.let {
                Intent(this@MainActivity, EditImageActivity::class.java).also { editImageIntent ->
                    editImageIntent.putExtra(KEY_IMAGE_URI, imageUri)
                    startActivity(editImageIntent)
                }
            }
        }

        binding.buttonEditNewImage.setOnClickListener {
            Intent (
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            ).also { pickerIntent ->
                pickerIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                pickerIntent.putExtra(REQUEST_CODE, REQUEST_CODE_PICK_IMAGE);
                //startActivityForResult.launch(pickerIntent)
                getContentUriFromActivity.launch("image/*")
            }
        }

        binding.buttonViewSavedImages.setOnClickListener {
            Intent(this@MainActivity,SavedImageActivity::class.java).also {
                startActivity(it)
            }
        }
    }
}