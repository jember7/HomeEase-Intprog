package com.capstone.homeease

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ExpertBookingsAdapter(var context: Context, var bookings: List<Booking>) : RecyclerView.Adapter<ExpertBookingsAdapter.BookingViewHolder>() {

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private var ongoingBookings: List<Booking> = emptyList()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_bookings, parent, false)
        return BookingViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        val booking = bookings[position]

        // Bind data to the views
        holder.expertNameTextView.text = booking.userName
        holder.address.text = "Address: ${booking.userAddress}"
        holder.bookingStatusTextView.text = "Status: ${booking.status}"
        holder.note.text = "Note: ${booking.note}"
        holder.bookingTimestampTextView.text = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(
            Date(booking.timestamp)
        )
        // Set visibility and click listeners for the buttons
        if (booking.status == "Pending") {
            holder.acceptButton.visibility = View.VISIBLE
            holder.declineButton.visibility = View.VISIBLE
            holder.completeButton.visibility = View.GONE
            holder.cancelButton.visibility = View.GONE
        } else {
            holder.acceptButton.visibility = View.GONE
            holder.declineButton.visibility = View.GONE
            holder.completeButton.visibility = View.VISIBLE
            holder.cancelButton.visibility = View.GONE
        }
        holder.acceptButton.setOnClickListener {
            updateBookingStatus(booking, "Accepted", holder)
        }

        holder.declineButton.setOnClickListener {
            updateBookingStatus(booking, "Declined", holder)
        }

        holder.completeButton.setOnClickListener {
            updateBookingStatus(booking, "Completed", holder)
        }
        // Check if the booking is ongoing
        if (booking.status == "Accepted") {

            holder.completeButton.setOnClickListener {
                // Update booking status to "Completed" when the button is clicked
                updateBookingStatus(booking, "Completed", holder)
            }
        } else {
            holder.completeButton.visibility = View.GONE
        }
    }


    override fun getItemCount(): Int {
        return bookings.size
    }
    fun setNewBookings(newBookings1: List<Booking>) {
        this.bookings = newBookings1
        notifyDataSetChanged()
    }

    private fun updateBookingStatus(booking: Booking, status: String, holder: BookingViewHolder) {
        val bookingRef = firestore.collection("bookings").document(booking.id)
        bookingRef.update("status", status)
            .addOnSuccessListener {
                Toast.makeText(context, "Booking $status", Toast.LENGTH_SHORT).show()
                val updatedBookings = bookings.toMutableList()
                val index = updatedBookings.indexOfFirst { it.id == booking.id }
                if (index != -1) {
                    val updatedBooking = booking.copy(status = status)
                    updatedBookings[index] = updatedBooking
                    bookings = updatedBookings.toList()
                    notifyDataSetChanged()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }


    fun updateBookings(newBookings: List<Booking>) {
        this.bookings = newBookings
        notifyDataSetChanged()
    }
    fun updateOngoingBookings(newOngoingBookings: List<Booking>) {
        this.ongoingBookings = newOngoingBookings
        notifyDataSetChanged()
    }


    class BookingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val expertNameTextView: TextView = itemView.findViewById(R.id.expertNameTextView)
        val address: TextView = itemView.findViewById(R.id.address)
        val bookingStatusTextView: TextView = itemView.findViewById(R.id.bookingStatusTextView)
        val bookingTimestampTextView: TextView = itemView.findViewById(R.id.bookingTimestampTextView)
        val acceptButton: Button = itemView.findViewById(R.id.acceptButton)
        val note: TextView = itemView.findViewById(R.id.noteTextView)
        val declineButton: Button = itemView.findViewById(R.id.declineButton)
        val completeButton: Button = itemView.findViewById(R.id.completeButton)
        val cancelButton: Button = itemView.findViewById(R.id.cancelButton)
    }
}

