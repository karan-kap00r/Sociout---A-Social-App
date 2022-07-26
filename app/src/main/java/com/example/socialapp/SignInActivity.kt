package com.example.socialapp


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.socialapp.daos.UserDao
import com.example.socialapp.models.User
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext


class SignInActivity : AppCompatActivity() {


    private lateinit var auth: FirebaseAuth
    private val REQ_ONE_TAP = 2  // Can be any integer unique to the Activity
    private var showOneTapUI = true
    private val TAG = "SignInActivity Tag"
    private lateinit var googleSignInClient: GoogleSignInClient


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)


        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
        auth = Firebase.auth

            val v=findViewById<TextView>(R.id.textView)
            v.setOnClickListener{
                print("clicked textview:::");
                signIn();
            }
    }

    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        updateUI(currentUser)
    }
    private fun signIn() {

        val signInIntent = googleSignInClient.signInIntent

        startActivityForResult(signInIntent, REQ_ONE_TAP)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQ_ONE_TAP) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account =
                completedTask.getResult(ApiException::class.java)!!
            Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
            firebaseAuthWithGoogle(account.idToken!!)
        } catch (e: ApiException) {
            Log.w(TAG, "signInResult:failed code=" + e.statusCode)

        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)

        val v=findViewById<TextView>(R.id.textView)
        v.visibility=View.GONE
        findViewById<ProgressBar>(R.id.progressBar).visibility = View.VISIBLE
        GlobalScope.launch(Dispatchers.IO) {        // Use of Coroutine to avoid CallBack Hell
            val auth = auth.signInWithCredential(credential).await()
            val firebaseUser = auth.user
            withContext(Dispatchers.Main) {         //to switch to Main Thread after processing

                updateUI(firebaseUser)
            }
        }

    }
    private fun updateUI(firebaseUser: FirebaseUser?) {
        if(firebaseUser != null) {

            val user = User(firebaseUser.uid, firebaseUser.displayName, firebaseUser.photoUrl.toString())
            val usersDao = UserDao()
            usersDao.addUser(user)

            val mainActivityIntent = Intent(this, MainActivity::class.java)
            startActivity(mainActivityIntent)
            Toast.makeText(this,"Sign In Successful",Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this,"Welcome to Sociout!",Toast.LENGTH_SHORT).show()
//            b1.visibility = View.VISIBLE
            val v1=findViewById<TextView>(R.id.textView)
            v1.visibility= View.VISIBLE
            findViewById<ProgressBar>(R.id.progressBar).visibility = View.GONE
        }
    }

}