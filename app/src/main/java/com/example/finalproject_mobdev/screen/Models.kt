package com.example.finalproject_mobdev.models

// Data class for Pub
data class Pub(
    val id: String,        // ID único do pub
    val name: String,      // Nome do pub
    val craicScore: Int,   // Avaliação "Craic Score"
    val comment: String    // Comentários gerais sobre o pub
)

// Data class for detailed Pub information
data class PubDetails(
    val id: String,
    val name: String,
    val location: String,
    val craicRating: Double,
    val description: String,
    val imageUrl: String
)


