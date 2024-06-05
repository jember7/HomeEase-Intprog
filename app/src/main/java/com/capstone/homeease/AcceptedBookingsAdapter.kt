package com.capstone.homeease

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class AcceptedBookingsAdapter(private val bookings: List<Booking>, private val onBookingClicked: (Booking) -> Unit) :
    RecyclerView.Adapter<AcceptedBookingsAdapter.BookingViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_accepted_booking, parent, false)
        return BookingViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        val booking = bookings[position]
        holder.expertNameTextView.text = booking.expertName
        Glide.with(holder.itemView.context)
            .load(booking.expertImageUrl) // Replace with your image URL or drawable
            .apply(RequestOptions.circleCropTransform()) // Makes the image circular
            .into(holder.expertImageView)
        holder.itemView.setOnClickListener {
            onBookingClicked(booking)
        }
    }

    override fun getItemCount(): Int {
        return bookings.size
    }

    class BookingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val expertImageView: ImageView = itemView.findViewById(R.id.expertImageView)
        val expertNameTextView: TextView = itemView.findViewById(R.id.expertNameTextView)
    }
}
