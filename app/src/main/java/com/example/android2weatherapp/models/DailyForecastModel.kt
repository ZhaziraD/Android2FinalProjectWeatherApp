package com.example.android2weatherapp.models

class DailyForecastModel(
        val date: Long,
        val timeZone: String,
        val temp: Double,
        val icon: String?,
        val tempMax: Double,
        val tempMin: Double) {
    override fun toString(): String {
        return "DailyForecastModel(date=$date, timeZone='$timeZone', temp=$temp, icon=$icon, tempMax=$tempMax, tempMin=$tempMin)"
    }
}
