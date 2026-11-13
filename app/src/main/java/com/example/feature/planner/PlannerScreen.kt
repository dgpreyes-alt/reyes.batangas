package com.example.feature.planner

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.BookmarkAdd
import androidx.compose.material.icons.rounded.Checklist
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.core.common.UiState
import com.example.core.data.GeneratedItineraryPlan
import com.example.core.model.ItineraryTimeSlots
import com.example.core.model.TravelerProfile
import com.example.core.model.TripItineraryItem

private enum class PlannerTab(val label: String) {
    ITINERARY("My Itinerary"),
    AI_GUIDE("Gemini AI Guide")
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PlannerScreen(
    profile: TravelerProfile,
    itineraryState: UiState<List<TripItineraryItem>>,
    conciergeState: ConciergeState,
    generatedPlanState: UiState<GeneratedItineraryPlan>?,
    onToggleVisited: (String, Boolean) -> Unit,
    onDeleteItem: (String) -> Unit,
    onAddCustomStop: (title: String, location: String, dayNumber: Int, timeSlot: String, notes: String) -> Unit,
    onAskConcierge: (String) -> Unit,
    onGenerateItinerary: (days: Int, style: String, groupContext: String) -> Unit,
    onSaveAiBatch: (List<TripItineraryItem>, onSaved: () -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(PlannerTab.ITINERARY) }
    var showAddDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 840.dp)
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            Text(
                text = "San Juan Trip & AI Guide",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(10.dp))

            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                PlannerTab.entries.forEachIndexed { index, tab ->
                    SegmentedButton(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        shape = SegmentedButtonDefaults.itemShape(index, PlannerTab.entries.size),
                        icon = {
                            Icon(
                                imageVector = if (tab == PlannerTab.ITINERARY) {
                                    Icons.Rounded.Checklist
                                } else {
                                    Icons.Rounded.AutoAwesome
                                },
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        modifier = Modifier.testTag("planner_tab_${tab.name.lowercase()}")
                    ) {
                        Text(tab.label)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            when (selectedTab) {
                PlannerTab.ITINERARY -> ItineraryTabContent(
                    itineraryState = itineraryState,
                    onToggleVisited = onToggleVisited,
                    onDeleteItem = onDeleteItem,
                    onSwitchToAi = { selectedTab = PlannerTab.AI_GUIDE }
                )

                PlannerTab.AI_GUIDE -> AiGuideTabContent(
                    profile = profile,
                    conciergeState = conciergeState,
                    generatedPlanState = generatedPlanState,
                    onAskConcierge = onAskConcierge,
                    onGenerateItinerary = onGenerateItinerary,
                    onSaveAiBatch = { stops ->
                        onSaveAiBatch(stops) { selectedTab = PlannerTab.ITINERARY }
                    }
                )
            }
        }

        if (selectedTab == PlannerTab.ITINERARY) {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp)
                    .testTag("add_custom_stop_button"),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Add custom San Juan stop")
            }
        }
    }

    if (showAddDialog) {
        AddStopDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { title, location, day, slot, notes ->
                onAddCustomStop(title, location, day, slot, notes)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun ItineraryTabContent(
    itineraryState: UiState<List<TripItineraryItem>>,
    onToggleVisited: (String, Boolean) -> Unit,
    onDeleteItem: (String) -> Unit,
    onSwitchToAi: () -> Unit
) {
    when (itineraryState) {
        is UiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        is UiState.Error -> {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.errorContainer
            ) {
                Text(
                    text = itineraryState.message,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        is UiState.Success -> {
            val items = itineraryState.data
            if (items.isEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Checklist,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No San Juan stops planned yet",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Tap + to add a custom stop, bookmark places from Explore, or let Gemini AI build a route for you.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(onClick = onSwitchToAi) {
                            Icon(Icons.Rounded.AutoAwesome, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Generate with Gemini AI")
                        }
                    }
                }
            } else {
                val visitedCount = items.count { it.visited }
                val progress = visitedCount.toFloat() / items.size.toFloat()

                LazyColumn(
                    contentPadding = PaddingValues(bottom = 84.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium,
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Trip Progress",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = "$visitedCount of ${items.size} visited",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                LinearProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp),
                                    trackColor = MaterialTheme.colorScheme.surface
                                )
                            }
                        }
                    }

                    items(items, key = { it.id }) { stop ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("itinerary_item_${stop.id}"),
                            shape = MaterialTheme.shapes.medium,
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Checkbox(
                                    checked = stop.visited,
                                    onCheckedChange = { checked -> onToggleVisited(stop.id, checked) },
                                    modifier = Modifier.testTag("itinerary_checkbox_${stop.id}")
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Surface(
                                            shape = RoundedCornerShape(50),
                                            color = MaterialTheme.colorScheme.secondaryContainer
                                        ) {
                                            Text(
                                                text = "Day ${stop.dayNumber} • ${stop.timeSlot}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = stop.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Rounded.LocationOn,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = stop.location,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    if (stop.notes.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = stop.notes,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                IconButton(
                                    onClick = { onDeleteItem(stop.id) },
                                    modifier = Modifier.testTag("delete_itinerary_${stop.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.DeleteOutline,
                                        contentDescription = "Remove stop",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AiGuideTabContent(
    profile: TravelerProfile,
    conciergeState: ConciergeState,
    generatedPlanState: UiState<GeneratedItineraryPlan>?,
    onAskConcierge: (String) -> Unit,
    onGenerateItinerary: (days: Int, style: String, groupContext: String) -> Unit,
    onSaveAiBatch: (List<TripItineraryItem>) -> Unit
) {
    var tripDays by remember { mutableIntStateOf(2) }
    var groupNotes by remember { mutableStateOf("") }
    var questionText by remember { mutableStateOf("") }

    val quickPrompts = remember {
        listOf(
            "How to commute from Lipa City to Laiya Beach?",
            "Mt. Daguldol local guide fees & hike prep?",
            "Best Barako coffee, lomi & pottery stops?"
        )
    }

    LazyColumn(
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "AI San Juan Itinerary Generator",
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Tailored for your ${profile.travelerStyle} preference using Gemini 3.8 Flash.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        (1..3).forEach { days ->
                            FilterChip(
                                selected = tripDays == days,
                                onClick = { tripDays = days },
                                label = { Text("$days ${if (days == 1) "Day" else "Days"}") }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = groupNotes,
                        onValueChange = { groupNotes = it },
                        label = { Text("Trip notes (e.g., family beach + pottery + lomi)") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("ai_itinerary_notes_input"),
                        shape = MaterialTheme.shapes.medium
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            onGenerateItinerary(tripDays, profile.travelerStyle, groupNotes)
                        },
                        enabled = generatedPlanState !is UiState.Loading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("generate_itinerary_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (generatedPlanState is UiState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Crafting San Juan Route...")
                        } else {
                            Icon(Icons.Rounded.AutoAwesome, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Generate $tripDays-Day Itinerary")
                        }
                    }

                    when (generatedPlanState) {
                        is UiState.Error -> {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                shape = MaterialTheme.shapes.small,
                                color = MaterialTheme.colorScheme.errorContainer
                            ) {
                                Text(
                                    text = generatedPlanState.message,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }

                        is UiState.Success -> {
                            val plan = generatedPlanState.data
                            Spacer(modifier = Modifier.height(14.dp))
                            Surface(
                                shape = MaterialTheme.shapes.medium,
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = plan.tripSummary,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    plan.stops.forEach { stop ->
                                        Row(
                                            modifier = Modifier.padding(vertical = 4.dp),
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.Schedule,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(
                                                    text = "Day ${stop.dayNumber} (${stop.timeSlot}): ${stop.title}",
                                                    style = MaterialTheme.typography.titleSmall
                                                )
                                                Text(
                                                    text = "${stop.location} — ${stop.notes}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Button(
                                        onClick = { onSaveAiBatch(plan.stops) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("save_ai_itinerary_button"),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(Icons.Rounded.BookmarkAdd, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Save All ${plan.stops.size} Stops to My Itinerary")
                                    }
                                }
                            }
                        }

                        else -> Unit
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Ask BatangasConnect AI Concierge",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Real-time streaming local guidance on San Juan beaches, hikes, commutes, and cuisine.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        quickPrompts.forEach { sample ->
                            AssistChip(
                                onClick = {
                                    questionText = sample
                                    onAskConcierge(sample)
                                },
                                label = { Text(sample, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = questionText,
                            onValueChange = { questionText = it },
                            placeholder = { Text("Ask anything about San Juan, Batangas...") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("concierge_question_input"),
                            shape = MaterialTheme.shapes.medium,
                            maxLines = 3
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { onAskConcierge(questionText) },
                            enabled = questionText.isNotBlank() && conciergeState !is ConciergeState.Loading,
                            modifier = Modifier
                                .height(52.dp)
                                .testTag("concierge_send_button"),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.Send,
                                contentDescription = "Send question"
                            )
                        }
                    }

                    when (conciergeState) {
                        is ConciergeState.Idle -> Unit
                        is ConciergeState.Loading -> {
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("Consulting San Juan local knowledge...", style = MaterialTheme.typography.bodySmall)
                            }
                        }

                        is ConciergeState.Streaming -> {
                            Spacer(modifier = Modifier.height(12.dp))
                            Surface(
                                shape = MaterialTheme.shapes.medium,
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                            ) {
                                Text(
                                    text = conciergeState.text,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(14.dp)
                                )
                            }
                        }

                        is ConciergeState.Success -> {
                            Spacer(modifier = Modifier.height(12.dp))
                            Surface(
                                shape = MaterialTheme.shapes.medium,
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                            ) {
                                Text(
                                    text = conciergeState.text,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(14.dp)
                                )
                            }
                        }

                        is ConciergeState.Error -> {
                            Spacer(modifier = Modifier.height(12.dp))
                            Surface(
                                shape = MaterialTheme.shapes.small,
                                color = MaterialTheme.colorScheme.errorContainer
                            ) {
                                Text(
                                    text = conciergeState.message,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AddStopDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, location: String, dayNumber: Int, timeSlot: String, notes: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("Brgy. Laiya Aplaya, San Juan") }
    var dayNumber by remember { mutableIntStateOf(1) }
    var timeSlot by remember { mutableStateOf("Morning") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add San Juan Stop") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Activity or Spot") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Barangay / Landmark") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    (1..3).forEach { day ->
                        FilterChip(
                            selected = dayNumber == day,
                            onClick = { dayNumber = day },
                            label = { Text("Day $day") }
                        )
                    }
                }
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    ItineraryTimeSlots.all.forEach { slot ->
                        FilterChip(
                            selected = timeSlot == slot,
                            onClick = { timeSlot = slot },
                            label = { Text(slot) }
                        )
                    }
                }
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optional)") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(title.trim(), location.trim(), dayNumber, timeSlot, notes.trim()) },
                enabled = title.trim().length >= 2 && location.trim().length >= 2
            ) {
                Text("Save Stop")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
