package com.example.lightideataxi.ui.activity

import activityViewBinding
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.lightideataxi.common.SIGN_IN_REQUEST_CODE
import com.example.lightideataxi.databinding.ActivityLoginBinding
import com.example.lightideataxi.util.isUserSignedIn
import com.example.lightideataxi.util.showToast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions


class LoginActivity : AppCompatActivity() {
    private val binding by activityViewBinding(ActivityLoginBinding::inflate)
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        if (isUserSignedIn()) {
            startActivity(Intent(this, HomeActivity::class.java))
        }
        binding.btnLogIn.setOnClickListener {
            signIn()
        }

    }

    private fun signIn() {
        if (!(this.isUserSignedIn())) {
            val signInIntent = mGoogleSignInClient.signInIntent
            startActivityForResult(signInIntent, SIGN_IN_REQUEST_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // The Task returned from this call is always completed, no need to attach
        // a listener.
        if (requestCode == SIGN_IN_REQUEST_CODE) {
            Log.d("asdf", "result")

            handleSignData(data)
        }
    }

    private fun handleSignData(data: Intent?) {
        Log.d("asdf", "handlesignindata")

        // The Task returned from this call is always completed, no need to attach
        // a listener.
        GoogleSignIn.getSignedInAccountFromIntent(data)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    // user successfully logged-in
                    showToast("Successfully Logged In")
                    startActivity(Intent(this, HomeActivity::class.java))
                } else {
                    // authentication failed
                    showToast("Log In Failed")
                }
            }

    }

}