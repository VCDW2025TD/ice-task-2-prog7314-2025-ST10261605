package com.example.memestream

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class LoginActivity {

    class LoginActivity : AppCompatActivity() {

        private lateinit var auth: FirebaseAuth
        private lateinit var googleSignInClient: GoogleSignInClient

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_login)

            // 1. Initialize Firebase Auth
            auth = FirebaseAuth.getInstance()

            // 2. Configure Google Sign-In
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // from google-services.json
                .requestEmail()
                .build()

            googleSignInClient = GoogleSignIn.getClient(this, gso)

            // 3. Button click launches sign-in
            val googleBtn = findViewById<Button>(R.id.googleSignInBtn)
            googleBtn.setOnClickListener {
                signIn()
            }
        }

        // Use ActivityResultLauncher (modern way, replaces onActivityResult)
        private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try {
                    val account: GoogleSignInAccount = task.getResult(ApiException::class.java)
                    firebaseAuthWithGoogle(account.idToken!!)
                } catch (e: ApiException) {
                    Log.w("LoginActivity", "Google sign in failed", e)
                    Toast.makeText(this, "Google sign-in failed", Toast.LENGTH_SHORT).show()
                }
            }
        }

        private fun signIn() {
            val signInIntent = googleSignInClient.signInIntent
            launcher.launch(signInIntent)
        }

        private fun firebaseAuthWithGoogle(idToken: String) {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            auth.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // ✅ User signed in
                        val user = auth.currentUser
                        Toast.makeText(this, "Welcome ${user?.displayName}", Toast.LENGTH_SHORT).show()

                        // navigate to main screen & trigger biometric auth
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, "Firebase Auth failed", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
}