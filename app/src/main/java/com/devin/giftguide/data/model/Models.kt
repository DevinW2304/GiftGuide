package com.devin.giftguide.data.model

data class RecipientInfo(
    val ageRange: String,
    val relationship: String
)

data class RecommendationRequest(
    val recipient: RecipientInfo,
    val occasion: String,
    val budgetMin: Int,
    val budgetMax: Int,
    val interests: List<String>,
    val vibe: String
)

data class Product(
    val productId: String,
    val name: String,
    val price: Double,
    val imageUrl: String,
    val productUrl: String,
    val tags: List<String>,
    val score: Double
)

data class RecommendationResponse(
    val recommendations: List<Product>
)
