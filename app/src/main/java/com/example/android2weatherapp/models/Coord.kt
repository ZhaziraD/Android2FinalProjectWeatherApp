package com.example.android2weatherapp.models

import java.io.Serializable

class Coord(
        val lat: Double? = null,
        val lon: Double? = null
) : Serializable {
    override fun toString(): String {
        return "Coord(lat=$lat, lon=$lon)"
    }
}