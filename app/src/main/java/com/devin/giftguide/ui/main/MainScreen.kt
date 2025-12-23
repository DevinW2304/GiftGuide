package com.devin.giftguide.ui.main

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.devin.giftguide.data.model.Product

// Humanized palette (pastel, not neon)
internal val GG_Green = Color(0xFFB9E192)     // #b9e192
private val GG_GreenSoft = Color(0xFFCFEBB6) // #cfebb6
internal val GG_Blue = Color(0xFFB3C7F7)      // #b3c7f7
private val GG_PinkSoft = Color(0xFFF8B8D0)  // #f8b8d0
private val GG_Pink = Color(0xFFF194B8)      // #f194b8
internal val GG_Black = Color(0xFF000000)     // #000000
internal val GG_Surface = Color(0xFFF8F8F6)   // warm off-white for a human feel
internal val GG_Border = Color(0x14000000)    // subtle black @ ~8%

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    onOpenSaved: () -> Unit
) {
    val quizState by viewModel.quizState.collectAsState()
    val recommendations by viewModel.recommendations.collectAsState()
    val savedIds by viewModel.savedProductIds.collectAsState()

    var showSavedOnly by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = GG_Surface,
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
                .background(GG_Surface),
            color = GG_Surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Header(onOpenSaved = onOpenSaved)

                // Quiz card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1.1f),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = GG_GreenSoft),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
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

@Composable
private fun Header(onOpenSaved: () -> Unit) {
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = GG_Green,
                    shadowElevation = 0.dp
                ) {
                    Text(
                        text = "GG",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        color = GG_Black,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column {
                    Text(
                        text = "SmartGift Guide",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = GG_Black
                    )
                    Text(
                        text = "A quick quiz → thoughtful gift ideas you can save.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xB3000000) // black @ ~70%
                    )
                }
            }

            // Saved shortcut (dedicated screen) — more noticeable pill
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = GG_PinkSoft,
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .clickable { onOpenSaved() }
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.FavoriteBorder,
                        contentDescription = "Open saved gifts",
                        tint = GG_Black,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Saved",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = GG_Black
                    )
                }
            }
        }

        // Soft “divider” pill
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp),
            color = GG_Border,
            content = {}
        )
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
        tonalElevation = 2.dp,
        color = GG_Surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (!errorMessage.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = GG_PinkSoft
                ) {
                    Text(
                        text = errorMessage,
                        color = GG_Black,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                    )
                }
            }

            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                onClick = onSubmit,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GG_Blue,
                    contentColor = GG_Black,
                    disabledContainerColor = Color(0xFFE7E7E7),
                    disabledContentColor = Color(0x99000000)
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = GG_Black
                    )
                    Spacer(Modifier.width(10.dp))
                    Text("Finding gifts…", fontWeight = FontWeight.SemiBold)
                } else {
                    Text("See gift ideas", fontWeight = FontWeight.SemiBold)
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
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Recommendations",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = GG_Black
            )

            SummaryChip(quizState = quizState)
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterChip(
                selected = !showSavedOnly,
                onClick = { onToggleTab(false) },
                label = { Text("For you") },
                colors = chipColors(selected = !showSavedOnly, accent = GG_Green)
            )
            FilterChip(
                selected = showSavedOnly,
                onClick = { onToggleTab(true) },
                label = { Text("Saved") },
                colors = chipColors(selected = showSavedOnly, accent = GG_Pink)
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
        color = Color.White
    ) {
        Text(
            text = summaryText,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = Color(0xCC000000) // black @ ~80%
        )
    }
}

// --- helper composables ---

@Composable
private fun SectionHeader(label: String) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        color = GG_Black
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
            val isSelected = option == selected
            FilterChip(
                selected = isSelected,
                onClick = { onSelected(option) },
                label = {
                    Text(
                        option.replaceFirstChar { it.uppercase() },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                colors = chipColors(selected = isSelected, accent = GG_Blue)
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
                },
                colors = chipColors(selected = isSelected, accent = GG_PinkSoft)
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
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color.White
        ) {
            Text(
                text = "$${min.toInt()} – $${max.toInt()}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = GG_Black,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
            )
        }

        Slider(
            value = (min + max) / 2f,
            onValueChange = { center ->
                val range = (max - min).coerceAtLeast(20f)
                val newMin = (center - range / 2f).coerceAtLeast(0f)
                val newMax = (center + range / 2f).coerceAtMost(500f)
                onChange(newMin, newMax)
            },
            valueRange = 0f..500f,
            colors = SliderDefaults.colors(
                thumbColor = GG_Black,
                activeTrackColor = GG_Blue,
                inactiveTrackColor = Color(0x22000000)
            )
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
        val msg = if (showSavedOnly) {
            "No saved gifts yet.\nTap the heart on any gift to keep it here."
        } else {
            "Fill out the quiz and tap “See gift ideas” to get suggestions."
        }

        Surface(
            modifier = modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(18.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = if (showSavedOnly) "Saved is empty" else "Ready when you are",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = GG_Black
                )
                Text(
                    text = msg,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xB3000000)
                )
            }
        }
        return
    }

    // NOTE: animateItemPlacement() is only available in newer Compose Foundation versions.
    // To keep this file compiling on your current setup, we remove it here.
    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 2.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(
            items = items,
            key = { it.productId }
        ) { product ->
            val isSaved = savedIds.contains(product.productId)

            RecommendationCard(
                product = product,
                isSaved = isSaved,
                onToggleFavorite = { onToggleFavorite(product) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun RecommendationCard(
    product: Product,
    isSaved: Boolean,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val cardTint = if (isSaved) GG_PinkSoft else Color.White

    Card(
        modifier = modifier
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
                        .background(GG_GreenSoft),
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
                SaveButton(
                    isSaved = isSaved,
                    onToggle = onToggleFavorite
                )

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

@Composable
private fun SaveButton(
    isSaved: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptics = LocalHapticFeedback.current

    val scale by animateFloatAsState(
        targetValue = if (isSaved) 1.12f else 1.0f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = 450f),
        label = "heartScale"
    )

    val tint by animateColorAsState(
        targetValue = if (isSaved) GG_Pink else GG_Black,
        animationSpec = tween(180),
        label = "heartTint"
    )

    IconButton(
        onClick = {
            onToggle()
            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        },
        modifier = modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
    ) {
        Icon(
            imageVector = if (isSaved) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
            contentDescription = if (isSaved) "Saved" else "Save",
            tint = tint
        )
    }
}

// --- chip theme helper ---

@Composable
private fun chipColors(selected: Boolean, accent: Color) = FilterChipDefaults.filterChipColors(
    selectedContainerColor = accent,
    selectedLabelColor = GG_Black,
    selectedLeadingIconColor = GG_Black,
    containerColor = Color.White,
    labelColor = Color(0xCC000000)
)
