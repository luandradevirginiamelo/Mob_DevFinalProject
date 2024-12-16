package com.example.finalproject_mobdev.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.location.Geocoder
import java.util.Locale

// Função para abrir o Google Maps usando latitude e longitude
fun openGoogleMaps(context: Context, latitude: Double, longitude: Double) {
    val gmmIntentUri = Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude")
    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
    mapIntent.setPackage("com.google.android.apps.maps") // Abre diretamente no Google Maps
    context.startActivity(mapIntent)
}

// Função para obter endereço a partir de latitude e longitude
fun getAddressFromLocation(context: Context, latitude: Double, longitude: Double): Pair<String, String>? {
    return try {
        val geocoder = Geocoder(context, Locale.getDefault())
        val addresses = geocoder.getFromLocation(latitude, longitude, 1)
        if (!addresses.isNullOrEmpty()) {
            val address = addresses[0]
            val street = address.thoroughfare ?: "Unknown Street"
            val city = address.locality ?: "Unknown City"
            Pair(street, city)
        } else null
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
