package com.example.socialapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.example.socialapp.daos.PostDao
import com.example.socialapp.models.Post
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity(), IPostAdapter {

    private lateinit var adapter: PostAdapter
    private lateinit var postDao: PostDao
    val auth = Firebase.auth
    private lateinit var kk: SignInActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val fb = findViewById<TextView>(R.id.PlusSign)
        fb.setOnClickListener{
            val intent = Intent(this, CreatePostActivity::class.java)
            startActivity(intent)
        }
        setUpRecyclerView()
        val sout= findViewById<TextView>(R.id.signOut)
        sout.setOnClickListener{
//            SignOut()
            auth.signOut()
            Toast.makeText(this,"Signed Out", Toast.LENGTH_SHORT).show()
            val intent1 = Intent(this, SignInActivity::class.java)
            startActivity(intent1)
        }
    }

    private fun setUpRecyclerView() {
        postDao = PostDao()
        val postsCollections = postDao.postCollections
        val query = postsCollections.orderBy("createdAt", Query.Direction.DESCENDING)
        val recyclerViewOptions = FirestoreRecyclerOptions.Builder<Post>().setQuery(query, Post::class.java).build()

        adapter = PostAdapter(recyclerViewOptions, this)
        val recycler=findViewById<RecyclerView>(R.id.recyclerView)
        recycler.adapter = adapter
        recycler.layoutManager = LinearLayoutManager(this)
    }

    override fun onStart() {
        super.onStart()
        adapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter.stopListening()
    }

    override fun onLikeClicked(postId: String) {
        postDao.updateLikes(postId)
    }
}