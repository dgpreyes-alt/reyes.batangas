package com.example.core.data

import android.content.Context
import com.example.R
import com.example.core.common.OperationType
import com.example.core.common.handleFirestoreError
import com.example.core.model.SanJuanDestination
import com.example.core.model.SavedSpot
import com.example.core.model.TravelerProfile
import com.example.core.model.TripItineraryItem
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class TourismFirestoreRepository(
    private val db: FirebaseFirestore
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    fun observeTravelerProfile(userId: String): Flow<TravelerProfile?> = flow {
        val path = "users/$userId"
        emitAll(
            db.collection("users").document(userId)
                .snapshots()
                .map { snapshot ->
                    if (snapshot.exists()) {
                        snapshot.toObject(
                            TravelerProfile::class.java,
                            DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                        )
                    } else {
                        null
                    }
                }
                .catch { error ->
                    logStreamErrorIfActive(error, OperationType.GET, path, userId)
                    throw error
                }
        )
    }

    suspend fun createTravelerProfile(
        fullName: String,
        email: String,
        homeCity: String,
        travelerStyle: String
    ): Result<Unit> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
        val payload = mapOf(
            "userId" to uid,
            "fullName" to fullName.trim(),
            "email" to email.trim(),
            "homeCity" to homeCity.trim(),
            "travelerStyle" to travelerStyle,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun updateTravelerProfile(
        fullName: String,
        homeCity: String,
        travelerStyle: String
    ): Result<Unit> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
        val updates = mapOf(
            "fullName" to fullName.trim(),
            "homeCity" to homeCity.trim(),
            "travelerStyle" to travelerStyle,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.update(updates).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    fun observeSavedSpots(userId: String): Flow<List<SavedSpot>> = flow {
        val path = "users/$userId/savedSpots"
        emitAll(
            db.collection("users").document(userId).collection("savedSpots")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        SavedSpot::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
                }
                .catch { error ->
                    logStreamErrorIfActive(error, OperationType.LIST, path, userId)
                    throw error
                }
        )
    }

    suspend fun toggleSavedSpot(
        destination: SanJuanDestination,
        currentlySaved: Boolean
    ): Result<Unit> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
            .collection("savedSpots").document(destination.id)
        try {
            if (currentlySaved) {
                docRef.delete().await()
            } else {
                val payload = mapOf(
                    "userId" to uid,
                    "spotId" to destination.id,
                    "spotName" to destination.name,
                    "category" to destination.category.label,
                    "barangay" to destination.barangay,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(payload).await()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(
                e,
                if (currentlySaved) OperationType.DELETE else OperationType.CREATE,
                docRef.path
            )
            throw e
        }
    }

    fun observeItinerary(userId: String): Flow<List<TripItineraryItem>> = flow {
        val path = "users/$userId/itineraries"
        emitAll(
            db.collection("users").document(userId).collection("itineraries")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        TripItineraryItem::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedWith(
                        compareBy<TripItineraryItem> { it.dayNumber }
                            .thenBy { timeSlotWeight(it.timeSlot) }
                            .thenBy { it.createdAt ?: Timestamp(0, 0) }
                    )
                }
                .catch { error ->
                    logStreamErrorIfActive(error, OperationType.LIST, path, userId)
                    throw error
                }
        )
    }

    suspend fun addItineraryItem(
        title: String,
        location: String,
        dayNumber: Int,
        timeSlot: String,
        notes: String,
        source: String
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
            .collection("itineraries").document()
        val payload = mapOf(
            "userId" to uid,
            "title" to title.trim().take(120),
            "location" to location.trim().take(120),
            "dayNumber" to dayNumber.coerceIn(1, 14),
            "timeSlot" to timeSlot,
            "notes" to notes.trim().take(400),
            "visited" to false,
            "source" to source,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun addItineraryBatch(items: List<TripItineraryItem>): Result<Unit> = runCatching {
        if (items.isEmpty()) return@runCatching
        val uid = requireUserId()
        val colRef = db.collection("users").document(uid).collection("itineraries")
        val batch = db.batch()
        items.forEach { item ->
            val docRef = colRef.document()
            val payload = mapOf(
                "userId" to uid,
                "title" to item.title.trim().take(120),
                "location" to item.location.trim().take(120),
                "dayNumber" to item.dayNumber.coerceIn(1, 14),
                "timeSlot" to item.timeSlot,
                "notes" to item.notes.trim().take(400),
                "visited" to false,
                "source" to item.source,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            batch.set(docRef, payload)
        }
        try {
            batch.commit().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, colRef.path)
            throw e
        }
    }

    suspend fun toggleItineraryVisited(itemId: String, visited: Boolean): Result<Unit> = runCatching {
        val uid = requireUserId()
        require(itemId.isNotBlank()) { "Itinerary item ID must not be blank" }
        val docRef = db.collection("users").document(uid)
            .collection("itineraries").document(itemId)
        val updates = mapOf(
            "visited" to visited,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.update(updates).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun deleteItineraryItem(itemId: String): Result<Unit> = runCatching {
        val uid = requireUserId()
        require(itemId.isNotBlank()) { "Itinerary item ID must not be blank" }
        val docRef = db.collection("users").document(uid)
            .collection("itineraries").document(itemId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    private fun logStreamErrorIfActive(
        error: Throwable,
        operationType: OperationType,
        path: String,
        userId: String
    ) {
        val firestoreError = generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
        if (firestoreError != null && auth.currentUser?.uid == userId) {
            handleFirestoreError(firestoreError, operationType, path)
        } else if (error !is CancellationException && auth.currentUser?.uid == userId) {
            handleFirestoreError(error, operationType, path)
        }
    }

    private fun timeSlotWeight(timeSlot: String): Int = when (timeSlot) {
        "Morning" -> 0
        "Afternoon" -> 1
        "Sunset" -> 2
        "Evening" -> 3
        else -> 4
    }
}
