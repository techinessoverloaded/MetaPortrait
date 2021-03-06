package com.apkaproj.metaportrait.activities

import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import com.apkaproj.metaportrait.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.apkaproj.metaportrait.R
import com.apkaproj.metaportrait.helpers.EncryptionUtils
import com.apkaproj.metaportrait.helpers.IOUtils
import com.apkaproj.metaportrait.helpers.PreferenceUtils
import com.apkaproj.metaportrait.helpers.displayToast
import com.apkaproj.metaportrait.models.User
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.techiness.progressdialoglibrary.ProgressDialog
import java.io.File
import java.io.FileOutputStream

class LoginActivity : AppCompatActivity()
{
    private lateinit var binding : ActivityLoginBinding
    private lateinit var progressDialog : ProgressDialog
    private lateinit var mAuth : FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mAuth = FirebaseAuth.getInstance()
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
            progressDialog = ProgressDialog(this,ProgressDialog.THEME_FOLLOW_SYSTEM)
        else
            progressDialog = ProgressDialog(this)
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
        binding.loginButton.setOnClickListener {
            loginUser()
        }
        binding.emailInputLogin.addTextChangedListener(InputValidation(binding.emailInputLogin))
        binding.pwdInputLogin.addTextChangedListener(InputValidation(binding.pwdInputLogin))
    }
    private fun loginUser()
    {
        if (!validateEmail() || !validatePassword())
        {
            displayToast("Make sure that you have entered the details properly !")
        }
        else
        {
            val email = binding.emailInputLogin.text.toString().trim()
            val password = binding.pwdInputLogin.text.toString().trim()
            progressDialog.setMessage("Logging in User...")
            progressDialog.show()
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
                if(it.isSuccessful)
                {
                    with(PreferenceUtils.getInstance(this))
                    {
                        needsDbUpdate = true
                        isFirstTime = false
                    }
                    intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                    val userId = mAuth.currentUser!!.uid
                    val fireStore = Firebase.firestore
                    val storageRef = Firebase.storage.reference
                    val imagesRef = storageRef.child("images")
                    val userFolderReference = imagesRef.child(userId)
                    var keyForDecryption: String? = null
                    val ioUtils = IOUtils(this)
                    val dir = ioUtils.getImagesDirectoryAsFile()
                    fireStore.collection("Users")
                        .document(userId)
                        .get()
                        .addOnSuccessListener { snapshot ->
                            val userObject = snapshot.toObject(User::class.java)
                            keyForDecryption = userObject?.tempKey
                            if (userObject == null)
                            {
                                Log.d("error","User Object is null")
                                return@addOnSuccessListener
                            }
                        }.addOnFailureListener { exception ->
                            exception.printStackTrace()
                            return@addOnFailureListener
                        }
                    if(keyForDecryption == null)
                    {
                        Log.d("error","Decryption Key is null")
                    }
                    userFolderReference.listAll().addOnSuccessListener { result ->
                        val items = result.items
                        Log.d("Number of items on Cloud:",items.size.toString());
                        if (items.isEmpty())
                        {
                            Log.d("error", "No images were found on the Cloud !")
                            return@addOnSuccessListener
                        }
                        val oneMegabyte: Long = 1024 * 1024 * 10
                        for (item in items)
                        {
                            val fileName = item.name
                            item.getBytes(oneMegabyte).addOnSuccessListener { byteArray ->
                                val bitmap =
                                    EncryptionUtils.getDecryptedImageAsBitmap(byteArray, keyForDecryption!!)
                                val file = File(dir, fileName)
                                if (bitmap == null)
                                {
                                    Log.d("error", "Bitmap is null")
                                    return@addOnSuccessListener
                                }
                                Log.d("fileName",fileName)
                                Log.d("file",file.absolutePath)
                                if(!file.exists())
                                {
                                    dir.mkdirs()
                                    file.createNewFile()
                                }
                                with(FileOutputStream(file))
                                {
                                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, this)
                                    flush()
                                    close()
                                }
                                displayToast("Retrieved Image ${items.indexOf(item)+1} of ${items.size} from the Cloud !")
                            }.addOnFailureListener { exception ->
                                exception.printStackTrace()
                                displayToast("Failed to retrieve Image ${items.indexOf(item)+1} of ${items.size} from the Cloud !")
                            }
                        }
                    }.addOnFailureListener { exception ->
                        Log.d("error","Error in retrieving images from Cloud !")
                        exception.printStackTrace()
                        return@addOnFailureListener
                    }
                    progressDialog.dismiss()
                    Toast.makeText(this,"Logged in successfully !",Toast.LENGTH_LONG).show()
                    startActivity(Intent(this@LoginActivity,MainActivity::class.java))
                    finish()
                }
                else
                {
                    progressDialog.dismiss()
                    Toast.makeText(this,"Failed to login ! Try Again !",Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    private fun requestFocus(view: View)
    {
        if (view.requestFocus())
        {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        }
    }
    private fun validateEmail(): Boolean
    {
        return if (binding.emailInputLogin.text.toString().trim().isEmpty())
        {
            binding.emailLayoutLogin.error = "Email is required ! Eg: abc@example.com"
            requestFocus(binding.emailInputLogin)
            false
        }
        else if (!Patterns.EMAIL_ADDRESS.matcher(binding.emailInputLogin.text.toString().trim()).matches())
        {
            binding.emailLayoutLogin.error = "Enter Valid Email ! Eg: abc@example.com"
            requestFocus(binding.emailInputLogin)
            false
        }
        else
        {
            binding.emailLayoutLogin.error = null
            true
        }
    }
    private fun validatePassword(): Boolean
    {
        return if (binding.pwdInputLogin.text.toString().trim().isEmpty())
        {
            binding.pwdLayoutLogin.error = "Password is required ! Eg: axy234bvc"
            requestFocus(binding.pwdInputLogin)
            false
        }
        else if (binding.pwdInputLogin.text.toString().trim().length < 8)
        {
            binding.pwdLayoutLogin.error = "Enter Valid Password !"
            requestFocus(binding.pwdInputLogin)
            false
        }
        else
        {
            binding.pwdLayoutLogin.error = null
            true
        }
    }
    private inner class InputValidation(private val vi: View) : TextWatcher
    {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: Editable)
        {
            when (vi.id)
            {
                R.id.emailInputLogin -> validateEmail()
                R.id.pwdInputLogin -> validatePassword()
            }
        }
    }
}