package com.devin.giftguide.ui

data class GiftQuizUiState(
    val ageRange: String = "18-24",
    val relationship: String = "brother",
    val occasion: String = "birthday",
    val budgetMin: Float = 25f,
    val budgetMax: Float = 150f,
    val selectedInterests: Set<String> = emptySet(),
    val vibe: String = "fun",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
