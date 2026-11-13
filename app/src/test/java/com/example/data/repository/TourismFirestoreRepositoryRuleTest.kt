package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.core.data.TourismFirestoreRepository
import com.example.core.model.SanJuanCatalog
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class TourismFirestoreRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun createAndObserveTravelerProfile_authenticatedOwner_succeeds() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repository = TourismFirestoreRepository(firestore)

        val createResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.createTravelerProfile(
                fullName = "Juan Dela Cruz",
                email = ALICE_EMAIL,
                homeCity = "San Juan, Batangas",
                travelerStyle = "Beach & Marine"
            )
        }
        assertTrue("Profile create should succeed: ${createResult.exceptionOrNull()}", createResult.isSuccess)

        val profile = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeTravelerProfile(uid).first { it != null }
        }
        assertNotNull(profile)
        assertEquals("Juan Dela Cruz", profile?.fullName)
        assertEquals("Beach & Marine", profile?.travelerStyle)
    }

    @Test
    fun addAndToggleItineraryItem_authenticatedOwner_succeeds() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repository = TourismFirestoreRepository(firestore)

        val addResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.addItineraryItem(
                title = "Laiya Sunrise Kayak",
                location = "Brgy. Laiya Aplaya, San Juan",
                dayNumber = 1,
                timeSlot = "Morning",
                notes = "Bring waterproof pouch",
                source = "manual"
            )
        }
        assertTrue("Add itinerary item should succeed", addResult.isSuccess)
        val itemId = addResult.getOrThrow()

        val items = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeItinerary(uid).first { list -> list.any { it.id == itemId } }
        }
        assertTrue(items.any { it.id == itemId && !it.visited })

        val toggleResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.toggleItineraryVisited(itemId, visited = true)
        }
        assertTrue("Toggle visited should succeed", toggleResult.isSuccess)

        val updated = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeItinerary(uid).first { list -> list.any { it.id == itemId && it.visited } }
        }
        assertTrue(updated.any { it.id == itemId && it.visited })
    }

    @Test
    fun toggleSavedSpot_authenticatedOwner_bookmarksAndRemoves() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repository = TourismFirestoreRepository(firestore)
        val destination = SanJuanCatalog.destinations.first()

        val saveResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.toggleSavedSpot(destination, currentlySaved = false)
        }
        assertTrue("Bookmark should succeed", saveResult.isSuccess)

        val saved = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeSavedSpots(uid).first { list -> list.any { it.spotId == destination.id } }
        }
        assertTrue(saved.any { it.spotId == destination.id })
    }

    @Test
    fun observeItinerary_crossUserAccess_failsWithPermissionDenied() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        signInTestUser(BOB_EMAIL)
        val bobRepo = TourismFirestoreRepository(firestore)

        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                bobRepo.observeItinerary(aliceUid).first()
            }
            fail("Expected PERMISSION_DENIED when Bob reads Alice's itinerary")
        } catch (e: Exception) {
            val firestoreException = checkNotNull(e.unwrapFirestoreException()) {
                "Expected FirebaseFirestoreException, got $e"
            }
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreException.code)
        }
    }

    @Test
    fun observeItinerary_unauthenticatedUser_failsWithPermissionDenied() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        auth.signOut()
        val repository = TourismFirestoreRepository(firestore)

        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                repository.observeItinerary(aliceUid).first()
            }
            fail("Expected PERMISSION_DENIED for unauthenticated user")
        } catch (e: Exception) {
            val firestoreException = checkNotNull(e.unwrapFirestoreException()) {
                "Expected FirebaseFirestoreException, got $e"
            }
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreException.code)
        }
    }

    private companion object {
        const val ALICE_EMAIL = "alice.sanjuan@test.com"
        const val BOB_EMAIL = "bob.laiya@test.com"
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 3000L
    }
}
