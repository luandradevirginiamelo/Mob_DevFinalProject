package com.example.finalproject_mobdev.screen

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class HomeViewModel : ViewModel() {
    // I am using MutableStateFlow to hold the location text. This is where I store the current location of the user.
    // Initially, I set it to "No location selected" as the default value.
    private val _locationText = MutableStateFlow("No location selected")
    // I expose the location text as an immutable StateFlow so that other parts of the app can observe it but cannot modify it directly.
    val locationText = _locationText.asStateFlow()

    // I also use MutableStateFlow to manage message text, which I use to display any additional info or errors related to the location.
    private val _messageText = MutableStateFlow("")
    // Again, I expose this as an immutable StateFlow for observers.
    val messageText = _messageText.asStateFlow()

    // I use this MutableStateFlow to hold a list of pubs.
    // Each pub is represented as a Pair containing its ID and a string in the format "Name|Rating".
    private val _pubsList = MutableStateFlow(listOf<Pair<String, String>>())
    // I expose the pubs list as an immutable StateFlow for the UI to observe changes.
    val pubsList = _pubsList.asStateFlow()

    // This function is used when I want to update the current location and any associated message.
    fun updateLocation(location: String, message: String) {
        _locationText.value = location // I set the new location value here.
        _messageText.value = message   // I also set the new message value here.
    }

    // This function is for updating the list of pubs. I call it whenever I fetch new data from a database or server.
    fun updatePubs(pubs: List<Pair<String, String>>) {
        _pubsList.value = pubs // I set the new list of pubs here.
    }
}