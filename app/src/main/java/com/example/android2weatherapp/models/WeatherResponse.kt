package com.example.android2weatherapp.models

import java.io.Serializable

class WeatherResponse(
    val coord: Coord,
    val weather: List<Weather>,
    val base: String,
    val main: Main,
    val visibility: Int,
    val wind: Wind,
    val clouds: Clouds,
    val dt: Int,
    val sys: Sys,
    val id: Int,
    val name: String,
    val cod: Int,
    val timezone: String
) : Serializable {
    override fun toString(): String {
        return "WeatherResponse(coord=$coord, weather=$weather, base='$base', main=$main, visibility=$visibility, wind=$wind, clouds=$clouds, dt=$dt, sys=$sys, id=$id, name='$name', cod=$cod, timezone='$timezone')"
    }
}