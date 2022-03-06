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
import com.apkaproj.metaportrait.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.apkaproj.metaportrait.R
import com.apkaproj.metaportrait.helpers.PreferenceUtils
import com.techiness.progressdialoglibrary.ProgressDialog

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
            Toast.makeText(this,"Make sure that you have entered the details properly !", Toast.LENGTH_LONG).show()
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
                    progressDialog.dismiss()
                    PreferenceUtils.getInstance(this).needsDbUpdate = true
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