package com.devin.giftguide.ui.main

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (quizState.errorMessage != null) {
                    Text(
                        text = quizState.errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !quizState.isLoading,
                    onClick = { viewModel.submitQuiz() }
                ) {
                    if (quizState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Finding gifts…")
                    } else {
                        Text("See gift ideas")
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // QUIZ SECTION (scrollable)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Find the perfect gift",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Text("Who is this for?", style = MaterialTheme.typography.labelLarge)
                SingleSelectChipsRow(
                    options = GiftQuizOptions.relationships,
                    selected = quizState.relationship,
                    onSelected = { viewModel.updateRelationship(it) }
                )

                Text("Age range", style = MaterialTheme.typography.labelLarge)
                SingleSelectChipsRow(
                    options = GiftQuizOptions.ageRanges,
                    selected = quizState.ageRange,
                    onSelected = { viewModel.updateAgeRange(it) }
                )

                Text("Occasion", style = MaterialTheme.typography.labelLarge)
                SingleSelectChipsRow(
                    options = GiftQuizOptions.occasions,
                    selected = quizState.occasion,
                    onSelected = { viewModel.updateOccasion(it) }
                )

                Text("Budget", style = MaterialTheme.typography.labelLarge)
                BudgetSlider(
                    min = quizState.budgetMin,
                    max = quizState.budgetMax,
                    onChange = { min, max -> viewModel.updateBudget(min, max) }
                )

                Text("Interests", style = MaterialTheme.typography.labelLarge)
                MultiSelectChips(
                    options = GiftQuizOptions.interests,
                    selected = quizState.selectedInterests,
                    onToggle = { viewModel.toggleInterest(it) }
                )

                Text("Vibe", style = MaterialTheme.typography.labelLarge)
                SingleSelectChipsRow(
                    options = GiftQuizOptions.vibes,
                    selected = quizState.vibe,
                    onSelected = { viewModel.updateVibe(it) }
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Recommendations",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Toggle: For you / Saved
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = !showSavedOnly,
                        onClick = { showSavedOnly = false },
                        label = { Text("For you") }
                    )
                    FilterChip(
                        selected = showSavedOnly,
                        onClick = { showSavedOnly = true },
                        label = { Text("Saved") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // derive list for Saved tab from recommendations + savedIds
            val displayedItems =
                if (showSavedOnly) recommendations.filter { savedIds.contains(it.productId) }
                else recommendations

            RecommendationsList(
                items = displayedItems,
                savedIds = savedIds,
                onToggleFavorite = { product -> viewModel.toggleSaved(product) },
                modifier = Modifier.weight(1f),
                showSavedOnly = showSavedOnly
            )
        }
    }
}

// --- helper composables ---

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
                label = { Text(option) }
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
                label = { Text(option) }
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
    Column {
        Text(text = "$${min.toInt()} – $${max.toInt()}")
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
            modifier = modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (showSavedOnly) {
                    "You haven’t saved any gifts yet.\nTap \"♡ Save\" on a gift to add it here."
                } else {
                    "Fill out the quiz and tap \"See gift ideas\" to get suggestions."
                },
                style = MaterialTheme.typography.bodySmall
            )
        }
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
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
                        // ignore/log as needed
                    }
                }
            }
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
                    modifier = Modifier.size(72.dp),
                    contentScale = ContentScale.Crop
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            ) {
                val nameText = try { product.name } catch (_: Exception) { "Product" }
                Text(
                    text = nameText,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )

                val priceText = try { product.price?.toString() } catch (_: Exception) { null }
                if (!priceText.isNullOrBlank()) {
                    Text(
                        text = "$$priceText",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                val tagsText = try { product.tags?.joinToString(" • ") } catch (_: Exception) { null }
                if (!tagsText.isNullOrBlank()) {
                    Text(
                        text = tagsText,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            TextButton(onClick = onToggleFavorite) {
                Text(
                    text = if (isSaved) "♥ Saved" else "♡ Save",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
