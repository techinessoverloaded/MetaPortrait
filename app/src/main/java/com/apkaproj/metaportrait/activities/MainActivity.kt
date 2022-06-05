package com.apkaproj.metaportrait.activities

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.OpenableColumns
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import com.apkaproj.metaportrait.databinding.ActivityMainBinding
import com.apkaproj.metaportrait.helpers.EncryptionUtils
import com.apkaproj.metaportrait.helpers.IOUtils
import com.apkaproj.metaportrait.helpers.PreferenceUtils
import com.apkaproj.metaportrait.helpers.displayToast
import com.apkaproj.metaportrait.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.ByteArrayOutputStream
import java.io.File

class MainActivity : AppCompatActivity()
{
    companion object {
        const val KEY_IMAGE_URI = "imageUri"
        const val IS_FROM_CAMERA = "isFromCamera"
    }

    private lateinit var binding : ActivityMainBinding
    private lateinit var mAuth : FirebaseAuth
    private lateinit var firestore : FirebaseFirestore
    private lateinit var user : FirebaseUser
    private lateinit var userId : String
    private lateinit var userName : String
    private lateinit var userObject : User
    private lateinit var takePictureAndSaveToUri : ActivityResultLauncher<Uri>
    private lateinit var getContentUriFromActivity : ActivityResultLauncher<String>
    private lateinit var uriForOpenCamera : Uri

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mAuth = FirebaseAuth.getInstance()
        user = mAuth.currentUser!!
        firestore = FirebaseFirestore.getInstance()
        userId = user.uid
        takePictureAndSaveToUri = registerForActivityResult(ActivityResultContracts.TakePicture()) { isSuccessful ->
            if (isSuccessful)
            {
                Intent(this@MainActivity, EditImageActivity::class.java).also { editImageIntent ->
                    editImageIntent.putExtra(KEY_IMAGE_URI, uriForOpenCamera)
                    editImageIntent.putExtra(IS_FROM_CAMERA, true)
                    editImageIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                    editImageIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    val mAuth = FirebaseAuth.getInstance()
                    val userId = mAuth.currentUser!!.uid
                    val storageRef = Firebase.storage.reference
                    val imagesRef = storageRef.child("images")
                    var fileName: String = ""
                    contentResolver.query(uriForOpenCamera,null,
                        null,null)?.use { cursor ->
                        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        cursor.moveToFirst()
                        fileName = cursor.getString(nameIndex)
                    }
                    val userImagesRef = imagesRef.child("${userId}/${fileName}")
                    val bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(uriForOpenCamera))
                    with(ByteArrayOutputStream())
                    {
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, this)
                        val data = toByteArray()
                        val uploadTask = userImagesRef.putBytes(data)
                        uploadTask.addOnSuccessListener {
                            displayToast("Image backed up to Cloud successfully !")
                        }.addOnFailureListener { exception ->
                            exception.printStackTrace()
                            displayToast("Unable to back up Image to Cloud ! Make sure that the internet is turned on !")
                        }
                    }
                    startActivity(editImageIntent)
                }
            }
            else
            {
                displayToast("Taking Picture through Camera Cancelled !")
            }
        }
        getContentUriFromActivity = registerForActivityResult(ActivityResultContracts.GetContent())
        {
            imageUri : Uri? ->
            imageUri?.let {
                Intent(this@MainActivity, EditImageActivity::class.java).also { editImageIntent ->
                editImageIntent.putExtra(KEY_IMAGE_URI, imageUri)
                editImageIntent.putExtra(IS_FROM_CAMERA, false)
                startActivity(editImageIntent)
                }
            }
        }
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

    private fun setListeners() {
        binding.buttonEditNewImage.setOnClickListener {
            getContentUriFromActivity.launch("image/*")
        }

        binding.buttonViewSavedImages.setOnClickListener {
            Intent(this@MainActivity, SavedImageActivity::class.java).also {
                startActivity(it)
            }
        }

        binding.buttonOpenCamera.setOnClickListener {
            val ioUtils = IOUtils(this)
            val mediaStorageDirectory = ioUtils.getImagesDirectoryAsFile()
            val fileName = ioUtils.getUniqueFileName()
            if (!mediaStorageDirectory.exists()) {
                mediaStorageDirectory.mkdirs()
            }
            val file = File(mediaStorageDirectory, fileName)
            uriForOpenCamera = FileProvider.getUriForFile(
                applicationContext,
                "${applicationContext.packageName}.provider",
                file
            )
            takePictureAndSaveToUri.launch(uriForOpenCamera)
        }

        binding.logoutBtn.setOnClickListener {
            mAuth.signOut()
            with(PreferenceUtils.getInstance(this))
            {
                isFirstTime = true
                needsDbUpdate = true
            }
            val dir = IOUtils(this).getImagesDirectoryAsFile()
            dir.listFiles()?.let { files ->
                for (file in files) {
                    file.delete()
                }
            }
            startActivity(Intent(this@MainActivity, LoginActivity::class.java))
            finish()
        }
    }
}