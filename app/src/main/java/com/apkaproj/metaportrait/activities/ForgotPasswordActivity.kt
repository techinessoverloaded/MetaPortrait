package com.apkaproj.metaportrait.activities

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import com.apkaproj.metaportrait.R
import com.apkaproj.metaportrait.databinding.ActivityForgotPasswordBinding
import com.apkaproj.metaportrait.helpers.PreferenceUtils
import com.google.firebase.auth.FirebaseAuth
import com.techiness.progressdialoglibrary.ProgressDialog

class ForgotPasswordActivity : AppCompatActivity()
{
    private lateinit var binding : ActivityForgotPasswordBinding
    private lateinit var progressDialog : ProgressDialog
    private lateinit var mAuth : FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mAuth = FirebaseAuth.getInstance()
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
            progressDialog = ProgressDialog(this,ProgressDialog.THEME_FOLLOW_SYSTEM)
        else
            progressDialog = ProgressDialog(this)
        binding.emailInputForgot.addTextChangedListener(InputValidation(binding.emailInputForgot))
        binding.resetPwdButton.setOnClickListener{
            resetPassword()
        }
    }
    private fun resetPassword()
    {
        if (!validateEmail())
        {
            Toast.makeText(this,"Make sure that you have entered the email properly !", Toast.LENGTH_LONG).show()
        }
        else
        {
            val email = binding.emailInputForgot.text.toString().trim()
            progressDialog.setMessage("Sending Password Reset Email...")
            progressDialog.show()
            mAuth.sendPasswordResetEmail(email).addOnCompleteListener {
                if(it.isSuccessful)
                {
                    PreferenceUtils.getInstance(this).needsDbUpdate = true
                    progressDialog.dismiss()
                    Toast.makeText(this,"Check your email to reset the password ! You will be logged out now !",Toast.LENGTH_LONG).show()
                    mAuth.signOut()
                    startActivity(Intent(this@ForgotPasswordActivity,LoginActivity::class.java))
                    finish()
                }
                else
                {
                    progressDialog.dismiss()
                    Toast.makeText(this,"An error occurred ! Try Again !",Toast.LENGTH_LONG).show()
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
        return if (binding.emailInputForgot.text.toString().trim().isEmpty())
        {
            binding.emailLayoutForgot.error = "Email is required ! Eg: abc@example.com"
            requestFocus(binding.emailInputForgot)
            false
        }
        else if (!Patterns.EMAIL_ADDRESS.matcher(binding.emailInputForgot.text.toString().trim()).matches())
        {
            binding.emailLayoutForgot.error = "Enter Valid Email ! Eg: abc@example.com"
            requestFocus(binding.emailInputForgot)
            false
        }
        else
        {
            binding.emailLayoutForgot.error = null
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
                R.id.emailInputForgot -> validateEmail()
            }
        }
    }
}