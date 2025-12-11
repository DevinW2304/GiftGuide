package com.devin.giftguide.data.remote.models

data class ProductDto(
    val productId: String,
    val name: String,
    val price: Float?,
    val imageUrl: String?,
    val productUrl: String?,
    val tags: List<String>?,
    val score: Float?
)
