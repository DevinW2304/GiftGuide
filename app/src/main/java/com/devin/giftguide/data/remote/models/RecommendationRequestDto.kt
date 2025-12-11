package com.devin.giftguide.data.remote.models

data class RecommendationRequestDto(
    val recipient: RecipientInfoDto,
    val occasion: String,
    val budgetMin: Int,
    val budgetMax: Int,
    val interests: List<String>,
    val vibe: String
)
