package com.devin.giftguide.data.repository

import com.devin.giftguide.data.model.Product
import com.devin.giftguide.data.model.RecipientInfo
import com.devin.giftguide.data.model.RecommendationRequest
import com.devin.giftguide.data.remote.GiftApiService

class GiftRepository(
    private val api: GiftApiService
) {

    suspend fun fetchRecommendations(
        recipientInfo: RecipientInfo,
        occasion: String,
        budgetMin: Int,
        budgetMax: Int,
        interests: List<String>,
        vibe: String
    ): List<Product> {
        val request = RecommendationRequest(
            recipient = recipientInfo,
            occasion = occasion,
            budgetMin = budgetMin,
            budgetMax = budgetMax,
            interests = interests,
            vibe = vibe
        )
        val response = api.getRecommendations(request)
        return response.recommendations
    }
}
