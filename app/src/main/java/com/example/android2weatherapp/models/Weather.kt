package com.example.android2weatherapp.models

import java.io.Serializable

class Weather (
        val description: String? = null,
        val icon: String? = null,
        val id: Int? = null,
        val main: String? = null
        ) : Serializable {
        override fun toString(): String {
                return "Weather(description=$description, icon=$icon, id=$id, main=$main)"
        }
}