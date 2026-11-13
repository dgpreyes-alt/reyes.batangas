package com.example.feature.dashboard

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.common.UiState
import com.example.core.data.TourismFirestoreRepository
import com.example.core.model.SanJuanCatalog
import com.example.core.model.SanJuanDestination
import com.example.core.model.SavedSpot
import com.example.core.model.TourismCategory
import com.example.core.model.TravelerProfile
import com.example.core.model.TripItineraryItem
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ActionStatus(
    val isBusy: Boolean = false,
    val message: String? = null,
    val error: String? = null
)

class DashboardViewModel(
    private val repository: TourismFirestoreRepository,
    private val currentUserId: String
) : ViewModel() {

    val profileState: StateFlow<UiState<TravelerProfile?>> =
        repository.observeTravelerProfile(currentUserId)
            .map<TravelerProfile?, UiState<TravelerProfile?>> { UiState.Success(it) }
            .catch { error ->
                val firestoreError = unwrapFirestoreError(error)
                if (firestoreError == null && error is CancellationException) throw error
                val root = firestoreError ?: error
                Log.w(TAG, "Error observing traveler profile", root)
                emit(UiState.Error(root.message ?: "Failed to load traveler profile"))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = UiState.Loading
            )

    val savedSpotsState: StateFlow<UiState<List<SavedSpot>>> =
        repository.observeSavedSpots(currentUserId)
            .map<List<SavedSpot>, UiState<List<SavedSpot>>> { UiState.Success(it) }
            .catch { error ->
                val firestoreError = unwrapFirestoreError(error)
                if (firestoreError == null && error is CancellationException) throw error
                val root = firestoreError ?: error
                Log.w(TAG, "Error observing saved spots", root)
                emit(UiState.Error(root.message ?: "Failed to observe saved spots"))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = UiState.Loading
            )

    val itineraryState: StateFlow<UiState<List<TripItineraryItem>>> =
        repository.observeItinerary(currentUserId)
            .map<List<TripItineraryItem>, UiState<List<TripItineraryItem>>> { UiState.Success(it) }
            .catch { error ->
                val firestoreError = unwrapFirestoreError(error)
                if (firestoreError == null && error is CancellationException) throw error
                val root = firestoreError ?: error
                Log.w(TAG, "Error observing itinerary", root)
                emit(UiState.Error(root.message ?: "Failed to observe itinerary"))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = UiState.Loading
            )

    private val _selectedCategory = MutableStateFlow(TourismCategory.ALL)
    val selectedCategory: StateFlow<TourismCategory> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _actionStatus = MutableStateFlow(ActionStatus())
    val actionStatus: StateFlow<ActionStatus> = _actionStatus.asStateFlow()

    val catalog: List<SanJuanDestination> = SanJuanCatalog.destinations

    fun selectCategory(category: TourismCategory) {
        _selectedCategory.value = category
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun createProfile(
        fullName: String,
        email: String,
        homeCity: String,
        travelerStyle: String
    ) {
        viewModelScope.launch {
            _actionStatus.value = ActionStatus(isBusy = true)
            repository.createTravelerProfile(fullName, email, homeCity, travelerStyle)
                .onSuccess {
                    _actionStatus.value = ActionStatus(isBusy = false, message = "Welcome to San Juan, Batangas!")
                }
                .onFailure { e ->
                    _actionStatus.value = ActionStatus(isBusy = false, error = e.localizedMessage ?: "Registration failed")
                }
        }
    }

    fun updateProfile(fullName: String, homeCity: String, travelerStyle: String) {
        viewModelScope.launch {
            _actionStatus.value = ActionStatus(isBusy = true)
            repository.updateTravelerProfile(fullName, homeCity, travelerStyle)
                .onSuccess {
                    _actionStatus.value = ActionStatus(isBusy = false, message = "Traveler profile updated.")
                }
                .onFailure { e ->
                    _actionStatus.value = ActionStatus(isBusy = false, error = e.localizedMessage ?: "Failed to update profile")
                }
        }
    }

    fun toggleSavedSpot(destination: SanJuanDestination, currentlySaved: Boolean) {
        viewModelScope.launch {
            repository.toggleSavedSpot(destination, currentlySaved)
                .onSuccess {
                    val msg = if (currentlySaved) "Removed ${destination.name} from saved spots" else "Saved ${destination.name}"
                    _actionStatus.update { it.copy(message = msg, error = null) }
                }
                .onFailure { e ->
                    _actionStatus.update { it.copy(error = e.localizedMessage ?: "Unable to update saved spot") }
                }
        }
    }

    fun addDestinationToItinerary(
        destination: SanJuanDestination,
        dayNumber: Int,
        timeSlot: String
    ) {
        viewModelScope.launch {
            _actionStatus.update { it.copy(isBusy = true, error = null) }
            repository.addItineraryItem(
                title = destination.name,
                location = destination.barangay,
                dayNumber = dayNumber,
                timeSlot = timeSlot,
                notes = destination.travelTip,
                source = "catalog"
            ).onSuccess {
                _actionStatus.value = ActionStatus(
                    isBusy = false,
                    message = "Added ${destination.name} to Day $dayNumber ($timeSlot)"
                )
            }.onFailure { e ->
                _actionStatus.value = ActionStatus(
                    isBusy = false,
                    error = e.localizedMessage ?: "Could not add stop"
                )
            }
        }
    }

    fun addCustomItineraryItem(
        title: String,
        location: String,
        dayNumber: Int,
        timeSlot: String,
        notes: String
    ) {
        viewModelScope.launch {
            _actionStatus.update { it.copy(isBusy = true, error = null) }
            repository.addItineraryItem(
                title = title,
                location = location,
                dayNumber = dayNumber,
                timeSlot = timeSlot,
                notes = notes,
                source = "manual"
            ).onSuccess {
                _actionStatus.value = ActionStatus(isBusy = false, message = "Added \"$title\" to itinerary.")
            }.onFailure { e ->
                _actionStatus.value = ActionStatus(isBusy = false, error = e.localizedMessage ?: "Failed to add stop")
            }
        }
    }

    fun addAiItineraryBatch(items: List<TripItineraryItem>, onSaved: () -> Unit) {
        viewModelScope.launch {
            _actionStatus.update { it.copy(isBusy = true, error = null) }
            repository.addItineraryBatch(items)
                .onSuccess {
                    _actionStatus.value = ActionStatus(
                        isBusy = false,
                        message = "Saved ${items.size} AI-planned stops to your itinerary!"
                    )
                    onSaved()
                }
                .onFailure { e ->
                    _actionStatus.value = ActionStatus(
                        isBusy = false,
                        error = e.localizedMessage ?: "Failed to save AI itinerary"
                    )
                }
        }
    }

    fun toggleVisited(itemId: String, visited: Boolean) {
        viewModelScope.launch {
            repository.toggleItineraryVisited(itemId, visited)
                .onFailure { e ->
                    _actionStatus.update { it.copy(error = e.localizedMessage ?: "Failed to update status") }
                }
        }
    }

    fun deleteItineraryItem(itemId: String) {
        viewModelScope.launch {
            repository.deleteItineraryItem(itemId)
                .onSuccess {
                    _actionStatus.update { it.copy(message = "Stop removed from itinerary") }
                }
                .onFailure { e ->
                    _actionStatus.update { it.copy(error = e.localizedMessage ?: "Failed to delete stop") }
                }
        }
    }

    fun clearStatus() {
        _actionStatus.update { it.copy(message = null, error = null) }
    }

    private fun unwrapFirestoreError(error: Throwable): FirebaseFirestoreException? {
        return generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
    }

    private companion object {
        const val TAG = "DashboardVM"
        const val STOP_TIMEOUT_MILLIS = 5000L
    }
}
