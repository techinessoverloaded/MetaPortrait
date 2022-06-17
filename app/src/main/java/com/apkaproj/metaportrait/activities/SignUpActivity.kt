package com.apkaproj.metaportrait.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.apkaproj.metaportrait.R
import com.apkaproj.metaportrait.databinding.ActivitySignUpBinding
import com.apkaproj.metaportrait.helpers.EncryptionUtils
import com.apkaproj.metaportrait.helpers.PreferenceUtils
import com.apkaproj.metaportrait.helpers.RandomStringGenerator
import com.apkaproj.metaportrait.models.User
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.techiness.progressdialoglibrary.ProgressDialog

class SignUpActivity : AppCompatActivity()
{
    private lateinit var binding : ActivitySignUpBinding
    private lateinit var progressDialog : ProgressDialog
    private lateinit var mAuth : FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mAuth = FirebaseAuth.getInstance()
        progressDialog = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
            ProgressDialog(this,ProgressDialog.THEME_FOLLOW_SYSTEM)
        else
            ProgressDialog(this)
        binding.switchToLoginButton.setOnClickListener {
            val intent = Intent(this@SignUpActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
        binding.signupButton.setOnClickListener {
            registerUser()
        }
        binding.nameInputSignup.addTextChangedListener(InputValidation(binding.nameInputSignup))
        binding.emailInputSignup.addTextChangedListener(InputValidation(binding.emailInputSignup))
        binding.pwdInputSignup.addTextChangedListener(InputValidation(binding.pwdInputSignup))
        binding.cnfmPwdInputSignup.addTextChangedListener(InputValidation(binding.cnfmPwdInputSignup))
        progressDialog.setMessage("Registering User...")
    }
    private fun requestFocus(view: View)
    {
        if (view.requestFocus())
        {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        }
    }
    private fun validateName(): Boolean
    {
        return if (binding.nameInputSignup.text.toString().trim().isEmpty())
        {
            binding.nameLayoutSignup.error = "Name is required ! Eg: Kris"
            requestFocus(binding.nameInputSignup)
            false
        }
        else
        {
            binding.nameLayoutSignup.error = null
            true
        }
    }
    private fun validateEmail(): Boolean
    {
        return if (binding.emailInputSignup.text.toString().trim().isEmpty())
        {
            binding.emailLayoutSignup.error = "Email is required ! Eg: abc@example.com"
            requestFocus(binding.emailInputSignup)
            false
        }
        else if (!Patterns.EMAIL_ADDRESS.matcher(binding.emailInputSignup.text.toString().trim()).matches())
        {
            binding.emailLayoutSignup.error = "Enter Valid Email ! Eg: abc@example.com"
            requestFocus(binding.emailInputSignup)
            false
        }
        else
        {
            binding.emailLayoutSignup.error = null
            true
        }
    }
    private fun validatePassword(): Boolean
    {
        return if (binding.pwdInputSignup.text.toString().trim().isEmpty())
        {
            binding.pwdLayoutSignup.error = "Password is required ! Eg: axy234bvc"
            requestFocus(binding.pwdInputSignup)
            false
        }
        else if (binding.pwdInputSignup.text.toString().trim().length < 8)
        {
            binding.pwdLayoutSignup.error = "Enter Valid Password of Minimum length 8 !"
            requestFocus(binding.pwdInputSignup)
            false
        }
        else
        {
            binding.pwdLayoutSignup.error = null
            true
        }
    }
    private fun validateConfirmPassword(): Boolean
    {
        return if (binding.cnfmPwdInputSignup.text.toString().trim().isEmpty())
        {
            binding.cnfmPwdLayoutSignup.error = "Enter the password again !"
            requestFocus(binding.cnfmPwdInputSignup)
            false
        }
        else if (binding.cnfmPwdInputSignup.text.toString().trim() != binding.pwdInputSignup.text.toString().trim())
        {
            binding.cnfmPwdLayoutSignup.error = "The Passwords do not match !"
            requestFocus(binding.cnfmPwdInputSignup)
            false
        }
        else
        {
            binding.cnfmPwdLayoutSignup.error = null
            true
        }
    }
    private fun registerUser()
    {
        if (!validateName() || !validateConfirmPassword() || !validateEmail() || !validatePassword())
        {
            Toast.makeText(this,"Make sure that you have entered the details properly !",Toast.LENGTH_LONG).show()
        }
        else
        {
            var name: String
            var email: String
            val preferenceUtils = PreferenceUtils.getInstance(this)
            email = binding.emailInputSignup.text.toString().trim()
            val password = binding.pwdInputSignup.text.toString().trim()
            name = binding.nameInputSignup.text.toString().trim()
            val tempKey = RandomStringGenerator.getRandomString(16)
            progressDialog.show()
            mAuth.createUserWithEmailAndPassword(email, password).addOnSuccessListener {
                val db: FirebaseFirestore = FirebaseFirestore.getInstance()
                val userId = mAuth.currentUser!!.uid
                name = EncryptionUtils.encrypt(name,tempKey)
                email = EncryptionUtils.encrypt(email,tempKey)
                Log.d("name", name)
                Log.d("email", email)
                val user = User(name,email,userId,tempKey)
                db.collection("Users").document(userId)
                    .set(user).addOnSuccessListener {
                        progressDialog.dismiss()
                        preferenceUtils.needsDbUpdate = true
                        Toast.makeText(this,"User successfully registered ! Logging in now !",Toast.LENGTH_LONG).show()
                        startActivity(
                            Intent(
                                this@SignUpActivity,
                                MainActivity::class.java
                            )
                        )
                        finish()
                    }.addOnFailureListener { exception ->
                        Log.d("ErrorWithDB",exception.toString())
                        mAuth.currentUser!!.delete()
                        progressDialog.dismiss()
                        Toast.makeText(this,"Failed to Register User ! Try Again !",Toast.LENGTH_LONG).show()
                    }
            }.addOnFailureListener { exception ->
                progressDialog.dismiss()
                Log.d("ErrorWithAuth", exception.toString())
                Toast.makeText(this,"Failed to Register User ! Try Again !",Toast.LENGTH_LONG).show()
            }
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
                R.id.emailInputSignup -> validateEmail()
                R.id.nameInputSignup -> validateName()
                R.id.pwdInputSignup -> validatePassword()
                R.id.cnfmPwdInputSignup -> validateEmail()
            }
        }
    }
}