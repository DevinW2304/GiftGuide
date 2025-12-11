package com.devin.giftguide.ui.main

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.devin.giftguide.data.model.Product

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel
) {
    val quizState by viewModel.quizState.collectAsState()
    val recommendations by viewModel.recommendations.collectAsState()
    val savedIds by viewModel.savedProductIds.collectAsState()

    var showSavedOnly by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            BottomActionBar(
                isLoading = quizState.isLoading,
                errorMessage = quizState.errorMessage,
                onSubmit = { viewModel.submitQuiz() }
            )
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // App header
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "SmartGift Guide",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Tell me about them and I’ll find something they’ll actually like.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Quiz card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1.1f),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        SectionHeader("Who is this for?")
                        SingleSelectChipsRow(
                            options = GiftQuizOptions.relationships,
                            selected = quizState.relationship,
                            onSelected = { viewModel.updateRelationship(it) }
                        )

                        SectionHeader("Age range")
                        SingleSelectChipsRow(
                            options = GiftQuizOptions.ageRanges,
                            selected = quizState.ageRange,
                            onSelected = { viewModel.updateAgeRange(it) }
                        )

                        SectionHeader("Occasion")
                        SingleSelectChipsRow(
                            options = GiftQuizOptions.occasions,
                            selected = quizState.occasion,
                            onSelected = { viewModel.updateOccasion(it) }
                        )

                        SectionHeader("Budget")
                        BudgetSlider(
                            min = quizState.budgetMin,
                            max = quizState.budgetMax,
                            onChange = { min, max -> viewModel.updateBudget(min, max) }
                        )

                        SectionHeader("Interests")
                        MultiSelectChips(
                            options = GiftQuizOptions.interests,
                            selected = quizState.selectedInterests,
                            onToggle = { viewModel.toggleInterest(it) }
                        )

                        SectionHeader("Vibe")
                        SingleSelectChipsRow(
                            options = GiftQuizOptions.vibes,
                            selected = quizState.vibe,
                            onSelected = { viewModel.updateVibe(it) }
                        )
                    }
                }

                // Recommendations area
                val displayedItems =
                    if (showSavedOnly) recommendations.filter { savedIds.contains(it.productId) }
                    else recommendations

                RecommendationsSection(
                    quizState = quizState,
                    showSavedOnly = showSavedOnly,
                    onToggleTab = { showSavedOnly = it },
                    items = displayedItems,
                    savedIds = savedIds,
                    onToggleFavorite = { product -> viewModel.toggleSaved(product) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

// --- bottom bar ---

@Composable
private fun BottomActionBar(
    isLoading: Boolean,
    errorMessage: String?,
    onSubmit: () -> Unit
) {
    Surface(
        tonalElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (!errorMessage.isNullOrBlank()) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                onClick = onSubmit
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Finding gifts…")
                } else {
                    Text("See gift ideas")
                }
            }
        }
    }
}

// --- recommendations section ---

@Composable
private fun RecommendationsSection(
    quizState: GiftQuizUiState,
    showSavedOnly: Boolean,
    onToggleTab: (Boolean) -> Unit,
    items: List<Product>,
    savedIds: Set<String>,
    onToggleFavorite: (Product) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Recommendations",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            // Small summary "chip"
            SummaryChip(quizState = quizState)
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterChip(
                selected = !showSavedOnly,
                onClick = { onToggleTab(false) },
                label = { Text("For you") }
            )
            FilterChip(
                selected = showSavedOnly,
                onClick = { onToggleTab(true) },
                label = { Text("Saved") }
            )
        }

        RecommendationsList(
            items = items,
            savedIds = savedIds,
            onToggleFavorite = onToggleFavorite,
            showSavedOnly = showSavedOnly
        )
    }
}

@Composable
private fun SummaryChip(quizState: GiftQuizUiState) {
    val summaryText = buildString {
        append(quizState.relationship.replaceFirstChar { it.uppercase() })
        append(" • ")
        append(quizState.ageRange)
        append(" • ")
        append(quizState.occasion.replaceFirstChar { it.uppercase() })
        append(" • $${quizState.budgetMin.toInt()}–$${quizState.budgetMax.toInt()}")
    }

    Surface(
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)
    ) {
        Text(
            text = summaryText,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// --- helper composables ---

@Composable
private fun SectionHeader(label: String) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
private fun SingleSelectChipsRow(
    options: List<String>,
    selected: String,
    onSelected: (String) -> Unit
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { option ->
            FilterChip(
                selected = option == selected,
                onClick = { onSelected(option) },
                label = {
                    Text(
                        option.replaceFirstChar { it.uppercase() },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MultiSelectChips(
    options: List<String>,
    selected: Set<String>,
    onToggle: (String) -> Unit
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { option ->
            val isSelected = selected.contains(option)
            FilterChip(
                selected = isSelected,
                onClick = { onToggle(option) },
                label = {
                    Text(
                        option.replaceFirstChar { it.uppercase() },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            )
        }
    }
}

@Composable
private fun BudgetSlider(
    min: Float,
    max: Float,
    onChange: (Float, Float) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = "$${min.toInt()} – $${max.toInt()}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        Slider(
            value = (min + max) / 2f,
            onValueChange = { center ->
                val range = (max - min).coerceAtLeast(20f)
                val newMin = (center - range / 2f).coerceAtLeast(0f)
                val newMax = (center + range / 2f).coerceAtMost(500f)
                onChange(newMin, newMax)
            },
            valueRange = 0f..500f
        )
    }
}

@Composable
private fun RecommendationsList(
    items: List<Product>,
    savedIds: Set<String>,
    onToggleFavorite: (Product) -> Unit,
    modifier: Modifier = Modifier,
    showSavedOnly: Boolean = false
) {
    if (items.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (showSavedOnly) {
                    "You haven’t saved any gifts yet.\nTap \"♡ Save\" on a gift to add it here."
                } else {
                    "Fill out the quiz and tap \"See gift ideas\" to get suggestions."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(items) { product ->
            val isSaved = savedIds.contains(product.productId)
            RecommendationCard(
                product = product,
                isSaved = isSaved,
                onToggleFavorite = { onToggleFavorite(product) }
            )
        }
    }
}

@Composable
private fun RecommendationCard(
    product: Product,
    isSaved: Boolean,
    onToggleFavorite: () -> Unit
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                val url = try { product.productUrl } catch (_: Exception) { null }
                if (!url.isNullOrBlank()) {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        context.startActivity(intent)
                    } catch (_: Exception) {
                        // ignore/log
                    }
                }
            },
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val imageUrl = try { product.imageUrl } catch (_: Exception) { null }

            if (!imageUrl.isNullOrBlank()) {
                Image(
                    painter = rememberAsyncImagePainter(imageUrl),
                    contentDescription = product.toString(),
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(14.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val nameText = try { product.name } catch (_: Exception) { "Product" }
                Text(
                    text = nameText,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // Price + store line
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val priceText = try { product.price?.toString() } catch (_: Exception) { null }
                    if (!priceText.isNullOrBlank()) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = "$$priceText",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    val store = try { product.tags?.firstOrNull() } catch (_: Exception) { null }
                    if (!store.isNullOrBlank()) {
                        Text(
                            text = store,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Save button
            TextButton(onClick = onToggleFavorite) {
                Text(
                    text = if (isSaved) "♥ Saved" else "♡ Save",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
