package com.capstone.homeease

import android.content.ContentValues.TAG
import android.content.Context
import android.media.Image
import android.provider.ContactsContract.CommonDataKinds.Im
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BookingsAdapter(var context: Context, var bookings: List<Booking>) : RecyclerView.Adapter<BookingsAdapter.BookingViewHolder>() {

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.booking_item, parent, false)
        return BookingViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        val booking = bookings[position]

        // Bind data to the views
        holder.expertNameTextView.text = booking.expertName
        holder.bookingStatusTextView.text = "Status: ${booking.status}"
        holder.bookingTimestampTextView.text = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(
            Date(booking.timestamp)
        )
        holder.addressTextView.text = booking.expertAddress
// Fetch expert details
        Glide.with(holder.itemView.context)
            .load(booking.expertImageUrl)
            .placeholder(R.drawable.img_36) // Placeholder image
            .into(holder.expertImageView)
        // Set visibility and click listeners for the buttons
        if (booking.status == "Pending") {
            holder.acceptButton.visibility = View.GONE
            holder.declineButton.visibility = View.GONE
            holder.cancelButton.visibility = View.VISIBLE // Show cancel button for pending bookings

            holder.acceptButton.setOnClickListener {
                updateBookingStatus(booking, "Accepted", holder)
            }

            holder.declineButton.setOnClickListener {
                updateBookingStatus(booking, "Declined", holder)
            }

            holder.cancelButton.setOnClickListener {
                // Handle cancel button click for pending booking
                cancelBooking(booking)
            }
        } else {
            holder.acceptButton.visibility = View.GONE
            holder.declineButton.visibility = View.GONE
            holder.cancelButton.visibility = View.GONE
        }
    }


    override fun getItemCount(): Int {
        return bookings.size
    }
    private fun cancelBooking(booking: Booking) {
        val bookingRef = firestore.collection("bookings").document(booking.id)
        bookingRef.update("status", "Cancelled")
            .addOnSuccessListener {
                Toast.makeText(context, "Booking Cancelled", Toast.LENGTH_SHORT).show()
                booking.status = "Cancelled"
                notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
    private fun updateBookingStatus(booking: Booking, status: String, holder: BookingViewHolder) {
        val bookingRef = firestore.collection("bookings").document(booking.id)
        bookingRef.update("status", status)
            .addOnSuccessListener {
                Toast.makeText(holder.itemView.context, "Booking $status", Toast.LENGTH_SHORT).show()
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
                Toast.makeText(holder.itemView.context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }


    fun updateBookings(newBookings: List<Booking>) {
        this.bookings = newBookings
        notifyDataSetChanged()
    }
    fun setNewBookings(newBookings1: List<Booking>) {
        this.bookings = newBookings1
        notifyDataSetChanged()
    }


    class BookingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val expertNameTextView: TextView = itemView.findViewById(R.id.expertNameTextView)
        val addressTextView: TextView = itemView.findViewById(R.id.address)
        val bookingStatusTextView: TextView = itemView.findViewById(R.id.bookingStatusTextView)
        val expertImageView: ImageView = itemView.findViewById(R.id.expertImageView)
        val bookingTimestampTextView: TextView = itemView.findViewById(R.id.bookingTimestampTextView)
        val acceptButton: Button = itemView.findViewById(R.id.acceptButton)
        val declineButton: Button = itemView.findViewById(R.id.declineButton)
        val cancelButton: Button = itemView.findViewById(R.id.cancelButton)
    }
}
