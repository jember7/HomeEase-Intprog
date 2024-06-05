package com.capstone.homeease

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.capstone.homeease.databinding.ActivityExpertConversationBinding
import com.google.firebase.Timestamp
import java.util.Date

class ExpertConversationActivity : AppCompatActivity() {

    companion object {
        private val firestore = FirebaseFirestore.getInstance()

        fun migrateTimestamps() {
            firestore.collection("messages")
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        val data = document.data
                        val timestamp = data["timestamp"]
                        if (timestamp is Long) {
                            val updatedTimestamp = Timestamp(Date(timestamp))
                            firestore.collection("messages").document(document.id)
                                .update("timestamp", updatedTimestamp)
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    Log.w("Migration", "Error migrating timestamps", exception)
                }
        }
    }

    private lateinit var binding: ActivityExpertConversationBinding
    private lateinit var messagesAdapter: MessagesAdapter
    private lateinit var bookingId: String
    private lateinit var userId: String
    private lateinit var userName: String
    private val currentUser = FirebaseAuth.getInstance().currentUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExpertConversationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get the booking details from the intent
        bookingId = intent.getStringExtra("bookingId") ?: return
        userId = intent.getStringExtra("userId") ?: return
        userName = intent.getStringExtra("userName") ?: return

        supportActionBar?.title = userName

        binding.messagesRecyclerView.layoutManager = LinearLayoutManager(this)
        messagesAdapter = MessagesAdapter(mutableListOf(), currentUser?.uid)
        binding.messagesRecyclerView.adapter = messagesAdapter

        fetchMessages()

        binding.sendButton.setOnClickListener {
            sendMessage()
        }

        binding.messageEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.sendButton.isEnabled = !s.isNullOrBlank() && s.trim().isNotEmpty()
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // Initially disable the send button
        binding.sendButton.isEnabled = false
    }

    private fun fetchMessages() {
        firestore.collection("messages")
            .whereEqualTo("bookingId", bookingId)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w("ConversationActivity", "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    val messages = snapshots.documents.mapNotNull { document ->
                        val message = document.toObject(Message::class.java)
                        message?.let {
                            it.copy(timestamp = it.getTimestampAsDate().time)
                        }
                    }
                    messagesAdapter.updateMessages(messages)
                    binding.messagesRecyclerView.scrollToPosition(messages.size - 1)
                }
            }
    }

    private fun sendMessage() {
        val messageText = binding.messageEditText.text?.toString()

        if (currentUser != null && !messageText.isNullOrBlank()) {
            val message = Message(
                senderId = currentUser.uid,
                receiverId = userId,
                text = messageText,
                timestamp = Timestamp.now(),
                bookingId = bookingId
            )

            firestore.collection("messages")
                .add(message)
                .addOnSuccessListener {
                    binding.messageEditText.text?.clear() // Null-safe call here
                }
                .addOnFailureListener { e ->
                    Log.w("ExpertConversation", "Error adding message", e)
                }
        }
    }
}
