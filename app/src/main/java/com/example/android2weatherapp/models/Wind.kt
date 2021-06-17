package com.example.android2weatherapp.models

import java.io.Serializable

class Wind (
        val deg: Int? = null,
        val gust: Double? = null,
        val speed: Double? = null
        ) : Serializable {
        override fun toString(): String {
                return "Wind(deg=$deg, gust=$gust, speed=$speed)"
        }
}