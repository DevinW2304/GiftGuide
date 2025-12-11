package com.devin.giftguide.data.remote

import com.devin.giftguide.data.model.RecommendationRequest
import com.devin.giftguide.data.model.RecommendationResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface GiftApiService {

    @POST("ai/recommendations")
    suspend fun getRecommendations(
        @Body request: RecommendationRequest
    ): RecommendationResponse
}
