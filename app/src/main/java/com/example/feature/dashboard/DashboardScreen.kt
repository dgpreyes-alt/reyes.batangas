package com.example.feature.dashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.core.common.UiState
import com.example.core.model.SanJuanDestination
import com.example.core.model.SavedSpot
import com.example.core.model.TourismCategory
import com.example.core.model.TravelerProfile
import com.example.core.model.TripItineraryItem

@Composable
fun DashboardScreen(
    profile: TravelerProfile,
    destinations: List<SanJuanDestination>,
    selectedCategory: TourismCategory,
    searchQuery: String,
    savedSpotsState: UiState<List<SavedSpot>>,
    itineraryState: UiState<List<TripItineraryItem>>,
    onSelectCategory: (TourismCategory) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onToggleSave: (SanJuanDestination, Boolean) -> Unit,
    onAddToItinerary: (SanJuanDestination, Int, String) -> Unit,
    onNavigateToAiPlanner: () -> Unit,
    modifier: Modifier = Modifier
) {
    var activeDestination by remember { mutableStateOf<SanJuanDestination?>(null) }

    val savedSpotIds = remember(savedSpotsState) {
        when (savedSpotsState) {
            is UiState.Success -> savedSpotsState.data.map { it.spotId }.toSet()
            else -> emptySet()
        }
    }
    val itineraryItems = remember(itineraryState) {
        when (itineraryState) {
            is UiState.Success -> itineraryState.data
            else -> emptyList()
        }
    }
    val visitedCount = itineraryItems.count { it.visited }

    val filteredDestinations = remember(destinations, selectedCategory, searchQuery) {
        destinations.filter { spot ->
            val matchesCategory = selectedCategory == TourismCategory.ALL || spot.category == selectedCategory
            val q = searchQuery.trim()
            val matchesQuery = q.isEmpty() ||
                spot.name.contains(q, ignoreCase = true) ||
                spot.barangay.contains(q, ignoreCase = true) ||
                spot.tagline.contains(q, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 840.dp),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Mabuhay, ${profile.fullName.substringBefore(" ")}!",
                                style = MaterialTheme.typography.headlineLarge,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "San Juan, Batangas • ${profile.travelerStyle}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatSummaryCard(
                            icon = Icons.Rounded.Bookmark,
                            count = savedSpotIds.size.toString(),
                            label = "Saved Spots",
                            modifier = Modifier.weight(1f)
                        )
                        StatSummaryCard(
                            icon = Icons.Rounded.Map,
                            count = itineraryItems.size.toString(),
                            label = "Planned Stops",
                            modifier = Modifier.weight(1f)
                        )
                        StatSummaryCard(
                            icon = Icons.Rounded.CheckCircle,
                            count = visitedCount.toString(),
                            label = "Visited",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("hero_ai_banner_card"),
                    shape = MaterialTheme.shapes.extraLarge,
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(190.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_laiya_hero),
                            contentDescription = "Laiya San Juan Batangas coastal showcase",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            Color(0xD9082628),
                                            Color(0x88082628),
                                            Color(0x22082628)
                                        )
                                    )
                                )
                        )
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = Color.White.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "SMART TOURISM CONCIERGE",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Plan San Juan with Gemini AI",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = Color.White
                                )
                                Text(
                                    text = "Build a Laiya, Mt. Daguldol & heritage pottery route in seconds.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = onNavigateToAiPlanner,
                                    modifier = Modifier.testTag("ask_ai_button"),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.secondary,
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.AutoAwesome,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Open AI Tour Planner")
                                }
                            }
                        }
                    }
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        placeholder = { Text("Search Laiya, Daguldol, Pottery, Lomi...") },
                        leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { onSearchQueryChange("") }) {
                                    Icon(Icons.Rounded.Close, contentDescription = "Clear search")
                                }
                            }
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dashboard_search_input"),
                        shape = MaterialTheme.shapes.large
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(TourismCategory.entries) { category ->
                            FilterChip(
                                selected = selectedCategory == category,
                                onClick = { onSelectCategory(category) },
                                label = { Text(category.label) },
                                modifier = Modifier.testTag("category_chip_${category.name.lowercase()}")
                            )
                        }
                    }
                }
            }

            if (filteredDestinations.isEmpty()) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.large,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ) {
                        Column(
                            modifier = Modifier.padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No matching spots in San Juan",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Try clearing your filter or search for Laiya, Hugom, or Poblacion.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(filteredDestinations, key = { it.id }) { spot ->
                    val isSaved = spot.id in savedSpotIds
                    DestinationCard(
                        destination = spot,
                        isSaved = isSaved,
                        onClick = { activeDestination = spot },
                        onToggleSave = { onToggleSave(spot, isSaved) }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }
        }
    }

    activeDestination?.let { destination ->
        DestinationDetailSheet(
            destination = destination,
            isSaved = destination.id in savedSpotIds,
            onDismiss = { activeDestination = null },
            onToggleSave = { onToggleSave(destination, destination.id in savedSpotIds) },
            onAddToItinerary = { day, slot ->
                onAddToItinerary(destination, day, slot)
            }
        )
    }
}

@Composable
private fun StatSummaryCard(
    icon: ImageVector,
    count: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = count,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun DestinationCard(
    destination: SanJuanDestination,
    isSaved: Boolean,
    onClick: () -> Unit,
    onToggleSave: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("destination_card_${destination.id}"),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(155.dp)
            ) {
                Image(
                    painter = painterResource(id = destination.imageRes),
                    contentDescription = destination.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Surface(
                    modifier = Modifier
                        .padding(12.dp)
                        .align(Alignment.TopStart),
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
                ) {
                    Text(
                        text = destination.category.label,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
                Surface(
                    modifier = Modifier
                        .padding(10.dp)
                        .align(Alignment.TopEnd)
                        .size(48.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onToggleSave)
                        .testTag("save_spot_button_${destination.id}"),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isSaved) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                            contentDescription = if (isSaved) "Remove ${destination.name} bookmark" else "Save ${destination.name}",
                            tint = if (isSaved) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = destination.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = destination.barangay,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = destination.tagline,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
