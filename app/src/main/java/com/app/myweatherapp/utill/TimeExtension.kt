package com.app.myweatherapp.utill

import java.text.SimpleDateFormat
import java.util.Locale

fun timeFormatter(inputDateTime: String): String {
    val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    val postDateTime = inputFormat.parse(inputDateTime) ?: return ""

    val currentTime = System.currentTimeMillis()
    val postTime = postDateTime.time
    val oneHourInMillis = 60 * 60 * 1000
    val timeDifference = currentTime - postTime
    val isWithinOneHour = timeDifference in 0..oneHourInMillis

    val outputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val time = outputFormat.format(postDateTime)
    val hour = time.substring(0, 2).toInt()
    val isPM = hour >= 12

    val formattedTime = if (isPM) {
        if (hour > 12) "${hour - 12}.00 PM" else "12.00 PM"
    } else {
        if (hour == 0) "12.00 AM" else "$hour.00 AM"
    }

    return if (isWithinOneHour) "NOW" else formattedTime
}