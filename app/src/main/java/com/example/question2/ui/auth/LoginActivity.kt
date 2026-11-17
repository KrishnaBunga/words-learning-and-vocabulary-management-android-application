package com.example.question2.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.question2.MainActivity
import com.example.question2.databinding.ActivityLoginBinding
import com.example.question2.ui.viewmodel.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val authViewModel: AuthViewModel by viewModels()

    private var googleSignInClient: GoogleSignInClient? = null

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account.idToken
            if (!idToken.isNullOrBlank()) {
                authViewModel.handleGoogleIdToken(idToken)
            } else {
                Toast.makeText(this, "Signed in as ${account.email ?: account.displayName ?: "User"}", Toast.LENGTH_SHORT).show()
                navigateToMain()
            }
        } catch (e: ApiException) {
            handleSignInError(e.statusCode)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupGoogleSignInClient()
        observeViewModel()

        binding.btnGoogleSignIn.setOnClickListener {
            if (googleSignInClient != null) {
                val signInIntent = googleSignInClient!!.signInIntent
                googleSignInLauncher.launch(signInIntent)
            } else {
                showFirebaseConfigHelpDialog()
            }
        }

        binding.btnGuestMode.setOnClickListener {
            navigateToMain()
        }
    }

    private fun setupGoogleSignInClient() {
        try {
            val builder = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()

            val webClientIdResId = resources.getIdentifier("default_web_client_id", "string", packageName)
            if (webClientIdResId != 0) {
                val webClientId = getString(webClientIdResId)
                if (webClientId.isNotBlank()) {
                    builder.requestIdToken(webClientId)
                }
            }

            googleSignInClient = GoogleSignIn.getClient(this, builder.build())
        } catch (e: Exception) {
            googleSignInClient = null
        }
    }

    private fun handleSignInError(statusCode: Int) {
        val message = when (statusCode) {
            CommonStatusCodes.DEVELOPER_ERROR -> {
                "Code 10 (Developer Error):\n\nYour app's SHA-1 fingerprint is not added to Firebase Console, or google-services.json needs to be updated with your real Firebase Web Client ID."
            }
            12500 -> {
                "Code 12500 (Sign-In Failed):\n\nGoogle Services configuration error. Please ensure google-services.json is configured in Firebase."
            }
            CommonStatusCodes.NETWORK_ERROR -> {
                "Code 7 (Network Error):\n\nPlease check your internet connection."
            }
            CommonStatusCodes.SIGN_IN_REQUIRED -> {
                "Sign in required. Please select a valid Google account."
            }
            else -> {
                "Sign-In Error (Code $statusCode). Please check your Firebase configuration."
            }
        }

        binding.tvStatusMessage.text = message

        AlertDialog.Builder(this)
            .setTitle("Google Sign-In Error (Code $statusCode)")
            .setMessage("$message\n\nWould you like to continue in Guest Mode to test all scanner and vocabulary features?")
            .setPositiveButton("Continue as Guest") { _, _ ->
                navigateToMain()
            }
            .setNegativeButton("OK", null)
            .show()
    }

    private fun showFirebaseConfigHelpDialog() {
        AlertDialog.Builder(this)
            .setTitle("Firebase Not Configured")
            .setMessage("Google Sign-In requires your own 'google-services.json' file from Firebase Console.\n\nYou can continue in Guest Mode to test all app features!")
            .setPositiveButton("Continue as Guest") { _, _ ->
                navigateToMain()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun observeViewModel() {
        authViewModel.currentUser.observe(this) { user ->
            if (user != null) {
                navigateToMain()
            }
        }

        authViewModel.authMessage.observe(this) { message ->
            if (!message.isNullOrBlank()) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                authViewModel.clearAuthMessage()
            }
        }
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}