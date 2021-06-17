package com.example.android2weatherapp.models

import java.io.Serializable

class Sys (
        val sunrise: Int,
        val sunset: Int,
        val country: String?
        ) : Serializable {
        override fun toString(): String {
                return "Sys(sunrise=$sunrise, sunset=$sunset, country=$country)"
        }
}