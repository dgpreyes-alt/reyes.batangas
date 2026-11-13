package com.example.feature.planner

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.common.UiState
import com.example.core.data.BatangasAiRepository
import com.example.core.data.GeneratedItineraryPlan
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface ConciergeState {
    data object Idle : ConciergeState
    data object Loading : ConciergeState
    data class Streaming(val text: String) : ConciergeState
    data class Success(val text: String) : ConciergeState
    data class Error(val message: String) : ConciergeState
}

class AiPlannerViewModel(
    private val aiRepository: BatangasAiRepository
) : ViewModel() {

    private val _conciergeState = MutableStateFlow<ConciergeState>(ConciergeState.Idle)
    val conciergeState: StateFlow<ConciergeState> = _conciergeState.asStateFlow()

    private val _itineraryPlanState = MutableStateFlow<UiState<GeneratedItineraryPlan>?>(null)
    val itineraryPlanState: StateFlow<UiState<GeneratedItineraryPlan>?> = _itineraryPlanState.asStateFlow()

    fun askConcierge(question: String) {
        val trimmed = question.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            _conciergeState.value = ConciergeState.Loading
            var latestText = ""
            try {
                aiRepository.askConciergeStream(trimmed).collect { partial ->
                    latestText = partial
                    _conciergeState.value = ConciergeState.Streaming(partial)
                }
                _conciergeState.value = if (latestText.isNotBlank()) {
                    ConciergeState.Success(latestText)
                } else {
                    ConciergeState.Error("Gemini AI did not return a response.")
                }
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "AI concierge timed out", e)
                _conciergeState.value = ConciergeState.Error(
                    "AI concierge timed out after 60 seconds. Please check your connection and try again."
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "AI concierge failed", e)
                _conciergeState.value = ConciergeState.Error(
                    e.localizedMessage ?: "Failed to reach BatangasConnect AI."
                )
            }
        }
    }

    fun generateItinerary(days: Int, travelerStyle: String, groupContext: String) {
        viewModelScope.launch {
            _itineraryPlanState.value = UiState.Loading
            try {
                val plan = aiRepository.generateSanJuanItinerary(
                    days = days,
                    travelerStyle = travelerStyle,
                    groupContext = groupContext.ifBlank { "Balanced coastal & heritage San Juan tour" }
                )
                _itineraryPlanState.value = UiState.Success(plan)
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "AI itinerary generation timed out", e)
                _itineraryPlanState.value = UiState.Error(
                    "Itinerary generation timed out after 60 seconds. Please try again."
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "AI itinerary generation failed", e)
                _itineraryPlanState.value = UiState.Error(
                    e.localizedMessage ?: "Failed to generate itinerary."
                )
            }
        }
    }

    fun clearGeneratedPlan() {
        _itineraryPlanState.value = null
    }

    private companion object {
        const val TAG = "AiPlannerVM"
    }
}
