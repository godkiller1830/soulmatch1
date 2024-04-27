package com.example.matrimony

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class ProfileViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun addUserProfile(name: String, age: String, bio: String, interests: String) {
        val ageNumber = age.toIntOrNull() ?: return // Validate age input
        val interestsList = interests.split(",").map { it.trim() }
        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser != null) {
            val userProfile = UserProfile(
                uid = currentUser.uid, // Use the authenticated user's UID
                name = name,
                age = ageNumber,
                bio = bio,
                interests = interestsList
            )

            _uiState.value = ProfileUiState(isLoading = true)
            db.collection("userProfiles").document(userProfile.uid)
                .set(userProfile)
                .addOnSuccessListener {
                    _uiState.value = ProfileUiState(isComplete = true)
                }
                .addOnFailureListener { e ->
                    _uiState.value = ProfileUiState(errorMessage = e.localizedMessage ?: "An error occurred")
                }
        } else {
            _uiState.value = ProfileUiState(errorMessage = "User not logged in")
        }
    }


    data class ProfileUiState(
        val isLoading: Boolean = false,
        val isComplete: Boolean = false,
        val errorMessage: String? = null
    )
}
