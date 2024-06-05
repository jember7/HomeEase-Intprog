package com.capstone.homeease

data class Expert(
    var id: String = "",
    val fullName: String = "",
    val email: String = "",
    val dateOfBirth: String = "",
    val profession: String = "",
    val address: String = "",
    val number: String = "",
    val role: String = "",
    var imageUrl: String? = null
){
    constructor() : this("", "", "", "") // Default constructor
}