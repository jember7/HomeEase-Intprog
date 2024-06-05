package com.capstone.homeease

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ExpertAcceptedBookingsAdapter(
    private val bookings: List<Booking>,
    private val onBookingClicked: (Booking) -> Unit
) : RecyclerView.Adapter<ExpertAcceptedBookingsAdapter.BookingViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_expert_accepted_booking, parent, false)
        return BookingViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        val booking = bookings[position]
        holder.userNameTextView.text = booking.userName
        holder.itemView.setOnClickListener {
            onBookingClicked(booking)
        }
    }

    override fun getItemCount(): Int {
        return bookings.size
    }

    class BookingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userNameTextView: TextView = itemView.findViewById(R.id.userNameTextView)
    }
}
