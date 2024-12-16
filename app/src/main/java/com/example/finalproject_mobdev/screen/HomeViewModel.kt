package com.example.finalproject_mobdev.screen

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class HomeViewModel : ViewModel() {
    private val _locationText = MutableStateFlow("No location selected")
    val locationText = _locationText.asStateFlow()

    private val _messageText = MutableStateFlow("")
    val messageText = _messageText.asStateFlow()

    private val _pubsList = MutableStateFlow(listOf<Pair<String, String>>())
    val pubsList = _pubsList.asStateFlow()

    fun updateLocation(location: String, message: String) {
        _locationText.value = location
        _messageText.value = message
    }

    fun updatePubs(pubs: List<Pair<String, String>>) {
        _pubsList.value = pubs
    }
}
