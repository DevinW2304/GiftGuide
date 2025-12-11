package com.devin.giftguide.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devin.giftguide.data.model.Product
import com.devin.giftguide.data.model.RecipientInfo
import com.devin.giftguide.data.model.RecommendationRequest
import com.devin.giftguide.data.model.RecommendationResponse
import com.devin.giftguide.data.remote.GiftApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// UI state for the quiz form
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

class MainViewModel(
    private val api: GiftApiService
) : ViewModel() {

    private val _quizState = MutableStateFlow(GiftQuizUiState())
    val quizState: StateFlow<GiftQuizUiState> = _quizState

    // Current recommendation results from backend
    private val _recommendations = MutableStateFlow<List<Product>>(emptyList())
    val recommendations: StateFlow<List<Product>> = _recommendations

    // Saved product IDs (for hearts + Saved tab)
    private val _savedProductIds = MutableStateFlow<Set<String>>(emptySet())
    val savedProductIds: StateFlow<Set<String>> = _savedProductIds

    // --- Quiz updates ---

    fun updateAgeRange(value: String) {
        _quizState.value = _quizState.value.copy(ageRange = value)
    }

    fun updateRelationship(value: String) {
        _quizState.value = _quizState.value.copy(relationship = value)
    }

    fun updateOccasion(value: String) {
        _quizState.value = _quizState.value.copy(occasion = value)
    }

    fun updateBudget(min: Float, max: Float) {
        val safeMin = min.coerceAtMost(max)
        val safeMax = max.coerceAtLeast(min)
        _quizState.value = _quizState.value.copy(
            budgetMin = safeMin,
            budgetMax = safeMax
        )
    }

    fun toggleInterest(interest: String) {
        val current = _quizState.value.selectedInterests.toMutableSet()
        if (!current.add(interest)) {
            current.remove(interest)
        }
        _quizState.value = _quizState.value.copy(selectedInterests = current)
    }

    fun updateVibe(value: String) {
        _quizState.value = _quizState.value.copy(vibe = value)
    }

    // --- Saved products (IDs only; UI derives list) ---

    fun toggleSaved(product: Product) {
        val currentIds = _savedProductIds.value.toMutableSet()

        if (currentIds.contains(product.productId)) {
            currentIds.remove(product.productId)
        } else {
            currentIds.add(product.productId)
        }

        _savedProductIds.value = currentIds
    }

    // --- Submit quiz + call backend ---

    fun submitQuiz() {
        val state = _quizState.value

        viewModelScope.launch {
            _quizState.value = state.copy(isLoading = true, errorMessage = null)

            try {
                val request = RecommendationRequest(
                    recipient = RecipientInfo(
                        ageRange = state.ageRange,
                        relationship = state.relationship
                    ),
                    occasion = state.occasion,
                    budgetMin = state.budgetMin.toInt(),
                    budgetMax = state.budgetMax.toInt(),
                    interests = state.selectedInterests.toList(),
                    vibe = state.vibe
                )

                // API returns a RecommendationResponse
                val response: RecommendationResponse = api.getRecommendations(request)
                _recommendations.value = response.recommendations

                _quizState.value = _quizState.value.copy(isLoading = false)
            } catch (e: Exception) {
                _quizState.value = _quizState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Something went wrong"
                )
            }
        }
    }
}
