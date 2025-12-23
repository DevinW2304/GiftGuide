package com.devin.giftguide.ui.main

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.devin.giftguide.data.model.Product

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val recommendations by viewModel.recommendations.collectAsState()
    val savedIds by viewModel.savedProductIds.collectAsState()

    val savedProducts: List<Product> = remember(recommendations, savedIds) {
        recommendations.filter { savedIds.contains(it.productId) }
    }

    Scaffold(
        containerColor = GG_Surface,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Saved gifts",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = GG_Black
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = GG_Black
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GG_Surface
                )
            )
        }
    ) { inner ->
        Surface(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize(),
            color = GG_Surface
        ) {
            if (savedProducts.isEmpty()) {
                Surface(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = Color.White
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Nothing saved yet",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = GG_Black
                        )
                        Text(
                            text = "Run the quiz, then tap the heart on any gift to keep it here.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xB3000000)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(savedProducts, key = { it.productId }) { product ->
                        SavedRecommendationCard(
                            product = product,
                            isSaved = true,
                            onToggleFavorite = { viewModel.toggleSaved(product) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SavedRecommendationCard(
    product: Product,
    isSaved: Boolean,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val cardTint = if (isSaved) Color(0xFFF8B8D0) else Color.White // GG_PinkSoft but literal to avoid private access

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable {
                val url = product.productUrl
                if (!url.isNullOrBlank()) {
                    try {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    } catch (_: Exception) {
                        // ignore/log
                    }
                }
            },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardTint),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val imageUrl = product.imageUrl
            if (!imageUrl.isNullOrBlank()) {
                Image(
                    painter = rememberAsyncImagePainter(imageUrl),
                    contentDescription = product.name,
                    modifier = Modifier
                        .size(76.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFCFEBB6)), // GG_GreenSoft literal
                    contentAlignment = Alignment.Center
                ) {
                    Text("Gift", color = GG_Black, style = MaterialTheme.typography.labelMedium)
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 4.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = product.name ?: "Product",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = GG_Black
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val price = product.price
                    if (price != null) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = GG_Green
                        ) {
                            Text(
                                text = "$${price}",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = GG_Black,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    val store = product.tags?.firstOrNull()
                    if (!store.isNullOrBlank()) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = GG_Blue.copy(alpha = 0.45f)
                        ) {
                            Text(
                                text = store,
                                style = MaterialTheme.typography.labelSmall,
                                color = GG_Black,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        imageVector = if (isSaved) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = if (isSaved) "Saved" else "Save",
                        tint = if (isSaved) Color(0xFFF194B8) else GG_Black // GG_Pink literal for safety
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.OpenInNew,
                            contentDescription = "Open",
                            modifier = Modifier.size(16.dp),
                            tint = GG_Black
                        )
                        Text(
                            text = "Open",
                            style = MaterialTheme.typography.labelMedium,
                            color = GG_Black,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}
