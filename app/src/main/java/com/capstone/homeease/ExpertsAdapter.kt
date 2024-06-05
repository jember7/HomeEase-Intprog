package com.capstone.homeease

import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ExpertsAdapter(private val context: Context, private val experts: MutableList<Expert>) : RecyclerView.Adapter<ExpertsAdapter.ExpertViewHolder>() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpertViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_expert, parent, false)
        return ExpertViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExpertViewHolder, position: Int) {
        val expert = experts[position]
        holder.fullNameTextView.text = expert.fullName
        holder.address.text = expert.address
        firestore.collection("experts").get()
            .addOnSuccessListener { documents ->
                val experts = mutableListOf<Expert>()
                for (document in documents) {
                    val id = document.id
                    val fullName = document.getString("fullName") ?: ""
                    val address = document.getString("address") ?: ""
                    val imageUrl = document.getString("imageUrl") ?: "" // Retrieve imageUrl from database
                    val expert = Expert(id, fullName, address, imageUrl)
                    experts.add(expert)
                }
                // Update your RecyclerView adapter with the fetched experts list
            }
            .addOnFailureListener { exception ->
                // Handle failure
            }

        Glide.with(holder.itemView.context)
            .load(expert.imageUrl)
            .placeholder(R.drawable.img_36) // Placeholder image
            .into(holder.expertImageView)
        holder.bookNowButton.setOnClickListener {
            val userId = auth.currentUser?.uid
            if (userId != null) {
                // Show the dialog to add a note
                showAddNoteDialog(userId, expert, position)
            } else {
                Toast.makeText(context, "User not authenticated", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun getItemCount(): Int {
        return experts.size
    }

    private fun showAddNoteDialog(userId: String, expert: Expert, position: Int) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_note, null)
        val notesEditText = dialogView.findViewById<EditText>(R.id.notesEditText)

        AlertDialog.Builder(context)
            .setTitle("Add Note")
            .setView(dialogView)
            .setPositiveButton("Submit") { dialog, _ ->
                val note = notesEditText.text.toString()
                addBooking(userId, expert, note, position)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun addBooking(userId: String, expert: Expert, note: String, position: Int) {
        // Fetch user name
        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { userDocument ->
                val userName = userDocument.getString("fullName")
                val userAddress = userDocument.getString("address")
                if (userName != null) {
                    val bookingData = hashMapOf(

                        "expertId" to expert.id,
                        "expertName" to expert.fullName,
                        "expertAddress" to expert.address, // Include expert address
                        "expertImageUrl" to expert.imageUrl,
                        "userId" to userId,
                        "userName" to userName,
                        "note" to note,
                        "status" to "Pending",
                        "timestamp" to System.currentTimeMillis(),
                        "userAddress" to userAddress
                    )
                    Log.d("ExpertId", "Expert ID: ${expert.id}")
                    Log.d("ExpertId", "Expert ID: ${expert.fullName}")
                    firestore.collection("bookings").add(bookingData)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Booking request sent", Toast.LENGTH_SHORT).show()
                            experts.removeAt(position)
                            notifyItemRemoved(position)
                            notifyItemRangeChanged(position, experts.size)
                        }
                        .addOnFailureListener { e ->
                            Log.e("ExpertsAdapter", "Error creating booking", e)
                            Toast.makeText(context, "Error creating booking: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(context, "Failed to get user name", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Log.e("ExpertsAdapter", "Error fetching user data", e)
                Toast.makeText(context, "Error fetching user data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }


    class ExpertViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val fullNameTextView: TextView = itemView.findViewById(R.id.fullNameTextView)
        val bookNowButton: Button = itemView.findViewById(R.id.bookNowButton)
        val expertImageView: ImageView = itemView.findViewById(R.id.expertImageView)
        val address: TextView = itemView.findViewById(R.id.address)
    }
}
