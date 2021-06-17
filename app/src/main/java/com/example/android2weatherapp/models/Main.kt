package com.example.android2weatherapp.models

import java.io.Serializable

class Main (
        val temp: Double? = null,
        val feels_like: Double? = null,
        val humidity: Int? = null,
        val pressure: Int? = null,
        val temp_max: Double? = null,
        val temp_min: Double? = null

//"temp":294.24,"feels_like":293.45,"temp_min":294.24,"temp_max":294.24,"pressure":1020,"humidity":40,"sea_level":1020,"grnd_level":996
        ) : Serializable {
        override fun toString(): String {
                return "Main(temp=$temp, feelsLike=$feels_like, humidity=$humidity, pressure=$pressure, tempMax=$temp_max, tempMin=$temp_min)"
        }
}