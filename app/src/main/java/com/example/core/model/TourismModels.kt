package com.example.core.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

object TravelerStyles {
    val all = listOf(
        "Beach & Marine",
        "Eco-Hiker",
        "Heritage & Crafts",
        "Foodie & Culture"
    )
}

object ItineraryTimeSlots {
    val all = listOf(
        "Morning",
        "Afternoon",
        "Sunset",
        "Evening"
    )
}

@IgnoreExtraProperties
data class TravelerProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val fullName: String = "",
    val email: String = "",
    val homeCity: String = "",
    val travelerStyle: String = "Beach & Marine",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class SavedSpot(
    @DocumentId val id: String = "",
    val userId: String = "",
    val spotId: String = "",
    val spotName: String = "",
    val category: String = "",
    val barangay: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class TripItineraryItem(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val location: String = "",
    val dayNumber: Int = 1,
    val timeSlot: String = "Morning",
    val notes: String = "",
    val visited: Boolean = false,
    val source: String = "manual",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
