package com.example.android2weatherapp.models

import java.io.Serializable

data class Clouds (
        val all: Int
        )  : Serializable {
        override fun toString(): String {
                return "Clouds(all=$all)"
        }
}