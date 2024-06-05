package com.capstone.homeease

import com.google.firebase.firestore.DocumentSnapshot

data class Booking(
    var id: String = "",
    var expertId: String = "",
    var expertName: String = "",
    var userId: String = "",
    var userName: String = "",
    var status: String = "",
    val timestamp: Long = 0L,
    var note: String = "",
    var rate: String = "",
    var expertAddress: String = "",
    var expertImageUrl: String = "",
    var userAddress: String = "",
)
