package com.capstone.homeease

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.capstone.homeease.databinding.ActivityConversationBinding
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ConversationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityConversationBinding
    private lateinit var messagesAdapter: MessagesAdapter
    private lateinit var bookingId: String
    private lateinit var expertId: String
    private lateinit var expertName: String
    private val firestore = FirebaseFirestore.getInstance()
    private val currentUser = FirebaseAuth.getInstance().currentUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConversationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get the booking details from the intent
        bookingId = intent.getStringExtra("bookingId") ?: return
        expertId = intent.getStringExtra("expertId") ?: return
        expertName = intent.getStringExtra("expertName") ?: return

        supportActionBar?.title = expertName

        binding.messagesRecyclerView.layoutManager = LinearLayoutManager(this)
        messagesAdapter = MessagesAdapter(mutableListOf(), currentUser?.uid)
        binding.messagesRecyclerView.adapter = messagesAdapter

        fetchMessages()

        binding.sendButton.setOnClickListener {
            sendMessage()
        }

        // Add TextWatcher to enable/disable send button based on EditText content
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

        if (currentUser != null && !messageText.isNullOrBlank() && messageText.trim().isNotEmpty()) {
            val message = Message(
                senderId = currentUser.uid,
                receiverId = expertId,
                text = messageText,
                timestamp = Timestamp.now(),
                bookingId = bookingId
            )

            firestore.collection("messages")
                .add(message)
                .addOnSuccessListener {
                    binding.messageEditText.text?.clear() // Use safe call operator
                }
                .addOnFailureListener { e ->
                    Log.w("ConversationActivity", "Error adding message", e)
                }
        }
    }
}
