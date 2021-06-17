package com.example.android2weatherapp.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.android2weatherapp.ImageNicer
import com.example.android2weatherapp.R
import com.example.android2weatherapp.adapters.DailyForecastAdapter
import com.example.android2weatherapp.adapters.HourlyForecastAdapter
import com.example.android2weatherapp.models.DailyForecastModel
import com.example.android2weatherapp.models.HourlyForecastModel
import com.example.android2weatherapp.models.WeatherResponse
import com.example.android2weatherapp.network.WeatherService
import com.example.android2weatherapp.utils.Constants
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.koushikdutta.async.future.FutureCallback
import com.koushikdutta.ion.Ion
import kotlinx.android.synthetic.main.activity_main.*
import retrofit.*
import java.io.IOException
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    var newLocation: String = ""
    var sunrise = ""
    var sunset = ""
    var wind = ""
    var humidity = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (!isLocationEnabled()) {
            Toast.makeText(
                    this,
                    "Your location provider is turned OFF. Please turn it on",
                    Toast.LENGTH_SHORT
            ).show()

            //redirect to the settings from where you need to turn on the location provider
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
        } else {
//            Toast.makeText(
//                this,
//                "Your location provider is already ON",
//                Toast.LENGTH_SHORT
//            ).show()
            Dexter.withActivity(this)
                    .withPermissions(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION,

                            )
                    .withListener(object : MultiplePermissionsListener {
                        override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                            if (report!!.areAllPermissionsGranted()) {
                                requestLocationData()
                            }

                            if (report.isAnyPermissionPermanentlyDenied) {
                                Toast.makeText(
                                        this@MainActivity,
                                        "You have denied location permission, Please allow it is mandatory",
                                        Toast.LENGTH_LONG
                                ).show()
                            }
                        }

                        override fun onPermissionRationaleShouldBeShown(
                                permissions: MutableList<PermissionRequest>?,
                                token: PermissionToken?
                        ) {
                            showRationalDialogForPermissions()
                        }
                    }).onSameThread()
                    .check()
        }



//        btnSearchLocation.setOnClickListener(object : View.OnClickListener {
//            override fun onClick(v: View?) {
//                etSearchLocation.setOnEditorActionListener(object : TextView.OnEditorActionListener {
//                    override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
//                        newLocation = etSearchLocation.text.toString().trim()
//                        return false
//                    }
//                })
//            }
//        })

        btnSearchLocation.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                newLocation = etSearchLocation.text.toString().trim()

                if (Geocoder.isPresent()) {
                    try {
                        val geocoder = Geocoder(this@MainActivity)
                        val address: List<Address> = geocoder.getFromLocationName(newLocation, 5)
                        val latNlon: MutableList<LatLng> = ArrayList(address.size)
                        for (a in address) {
                            if (a.hasLatitude() && a.hasLongitude()) {
                                latNlon.add(LatLng(a.getLatitude(), a.getLongitude()))
                                getLocationWeatherDetails(a.latitude, a.longitude)
//                                loadDailyForecast(a.latitude, a.longitude)
                            }
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                        Log.i("IOException", "$e")
                        Toast.makeText(this@MainActivity, "$e", Toast.LENGTH_LONG).show()
                    }
                }
            }
        })

        vMenu.setOnClickListener (object : View.OnClickListener {
            override fun onClick(v: View?) {
                var bottomSheetDialog = BottomSheetDialog(
                        this@MainActivity, R.style.BottomSheetDialogTheme
                )
                val bottomSheetView: View = LayoutInflater.from(
                        applicationContext
                ).inflate(
                        R.layout.layout_bottom_sheet,
                        findViewById(R.id.bottomSheetContainer) as LinearLayout?
                )

                bottomSheetView.findViewById<TextView>(R.id.tvSunrise).text = sunrise
                bottomSheetView.findViewById<TextView>(R.id.tvSunset).text = sunset
                bottomSheetView.findViewById<TextView>(R.id.tvWind).text = wind
                bottomSheetView.findViewById<TextView>(R.id.tvHumidity).text = humidity

                bottomSheetDialog.setContentView(bottomSheetView)
                bottomSheetDialog.show()
            }
        })

        ////////////////////////////////////////////////////////////////////////////////////////////
    }


    private fun showRationalDialogForPermissions() {
        AlertDialog.Builder(this)
                .setMessage("It looks like you have turned off permissions required for this feature. It can be enabled under Application S")
                .setPositiveButton(
                        "GO TO SETTINGS"
                ) { _, _->
                    try {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        val uri = Uri.fromParts("package", packageName, null)
                        intent.data = uri
                        startActivity(intent)
                    } catch (e: ActivityNotFoundException) {
                        e.printStackTrace()
                    }
                }
                .setNegativeButton("Cancel") { dialog,
                                               _ ->
                    dialog.dismiss()
                }.show()
    }

    //6666 checks whether the GPS is ON or OFF

    private fun isLocationEnabled() : Boolean {
        //provides access to the system location services
        val locationManager : LocationManager = getSystemService(
                Context.LOCATION_SERVICE
        )  as LocationManager

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.
        isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    @SuppressLint
    private fun requestLocationData() {
        val mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                )
                        != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        mFusedLocationClient.requestLocationUpdates(
                mLocationRequest, mLocationCallBack,
                Looper.myLooper()
        )
    }

    private val mLocationCallBack = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            val mLastLocation: Location = locationResult!!.lastLocation
            val latitude = mLastLocation.latitude
            Log.i("Current Latitude", "$latitude")

            val longitude = mLastLocation.longitude
            Log.i("Current Longitude", "$longitude")

            //7777
            //call the api calling function here
            getLocationWeatherDetails(latitude, longitude)
//            loadDailyForecast(latitude, longitude)
        }
    }

    //55555 api call using Retrofit Network Library
    //weather details
//    private fun getLocationWeatherDetails() {
//        if (Constants.isNetworkAvailable(this)) {
//            Toast.makeText(this, "You have connected to the Internet. Now you can make an api call", Toast.LENGTH_SHORT).show()
//        } else {
//            Toast.makeText(this, "No Internet connection available", Toast.LENGTH_SHORT).show()
//        }
//
//    }

    private fun getLocationWeatherDetails(latitude: Double, longitude: Double) {
        if(Constants.isNetworkAvailable(this)) {
            val retrofit: Retrofit = Retrofit.Builder()
                    .baseUrl(Constants.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()

            val service: WeatherService = retrofit.create<WeatherService>(WeatherService::class.java)

            val listCall: Call<WeatherResponse> = service.getWeather(
                    latitude, longitude, Constants.METRIC_UNIT, Constants.APP_ID
            )

            listCall.enqueue(object : Callback<WeatherResponse> {
                @SuppressLint("SetTextI18n")
                override fun onResponse(
                        response: Response<WeatherResponse>,
                        retrofit: Retrofit
                ) {
                    if (response.isSuccess) {
                        val weatherList: WeatherResponse = response.body()

                        Log.i("Response Result", "$weatherList")

                        tvCity.text = weatherList.name
                        ivIcon.setImageResource(ImageNicer.getIconImage(weatherList.weather[0].icon.toString()))
                        ivWeather.setImageBitmap(
                                ImageNicer.decodeSampledBitmapFromResource(
                                        this@MainActivity.resources,
                                        getBackgroundImage(weatherList.weather[0].icon.toString()), 300, 300
                                )
                        )
                        tvTemperature.text = weatherList.main.temp.toString() + "º"
                        tvTempMin.text = weatherList.main.temp_min.toString() + "º"
                        tvTempMax.text = weatherList.main.temp_max.toString() + "º"
                        tvTempFeelsLike.text = weatherList.main.feels_like.toString() + "º"

                        var dateFormat = SimpleDateFormat("EEE, d MMM  HH:mm")
                        var localDate = Date(Date().time + weatherList.timezone.toLong()*1000)

                        tvDateTime.text = dateFormat.format(localDate)
                        tvId.text = getIdText(weatherList.weather[0].id)

                        var dateFormat2 = SimpleDateFormat("HH:mm") //*MM-dd-yyyy HH:mm
                        var localDateSunrise = Date(weatherList.sys.sunrise.toLong()*1000)
                        var localDateSunset = Date(weatherList.sys.sunset.toLong()*1000)
                        dateFormat2.timeZone = TimeZone.getTimeZone(weatherList.timezone)
                        sunrise = dateFormat2.format(localDateSunrise).toString()
                        sunset = dateFormat2.format(localDateSunset).toString()

                        wind = weatherList.wind.speed.toString() + "m/s"
                        humidity = weatherList.main.humidity.toString() + "%"


                        loadDailyForecast(weatherList.coord.lon!!, weatherList.coord.lat!!)
                        loadHourlyForecast(weatherList.coord.lon!!, weatherList.coord.lat!!)

                    } else {
                        val sc = response.code()
                        when (sc) {
                            400 -> {
                                Log.e("Error 400", "Bad Request")
                            }
                            404 -> {
                                Log.e("Error 404", "Not Found")
                            }
                            else -> {
                                Log.e("Error", "Generic Error")
                            }
                        }
                    }
                }

                override fun onFailure(t: Throwable?) {
                    Log.e("Error (onFailure)", t!!.message.toString())
                }
            })
        } else {
            Toast.makeText(
                    this@MainActivity,
                    "No internet connection available",
                    Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun loadDailyForecast(lon: Double, lat: Double) {
        var apiUrl = "https://api.openweathermap.org/data/2.5/onecall?lat=" + lat + "&lon=" + lon + "&exclude=hourly,%20minutely,%20current&units=metric&appid=" + Constants.APP_ID
        Ion.with(this)
                .load(apiUrl)
                .asJsonObject()
                .setCallback(object : FutureCallback<JsonObject> {
                    override fun onCompleted(e: Exception?, result: JsonObject?) {
                        if (e != null) {
                            e.printStackTrace()
                            Toast.makeText(this@MainActivity, "Server Error: $e", Toast.LENGTH_SHORT).show()
                        } else {
                            Log.d("result_response", result.toString())
                            var weatherList: ArrayList<DailyForecastModel> = ArrayList()
                            var timeZone: String = result!!.get("timezone").asString
                            var daily: JsonArray = result!!.get("daily").asJsonArray

                            for (i in 1..daily.size()-1) {
                                var date = daily.get(i).asJsonObject.get("dt").asLong
                                var temp = daily.get(i).asJsonObject.get("temp").asJsonObject.get("day").asDouble
                                var tempMax = daily.get(i).asJsonObject.get("temp").asJsonObject.get("max").asDouble
                                var tempMin = daily.get(i).asJsonObject.get("temp").asJsonObject.get("min").asDouble
                                var icon = daily.get(i).asJsonObject.get("weather").asJsonArray.get(0).asJsonObject.get("icon").asString

                                weatherList.add(DailyForecastModel(date, timeZone, temp, icon, tempMax, tempMin))
                            }

                            var dailyWeatherAdapter = DailyForecastAdapter(weatherList, this@MainActivity)
                            rvDailyForecast.layoutManager = LinearLayoutManager(this@MainActivity)
                            rvDailyForecast.adapter = dailyWeatherAdapter
                        }
                    }
                })
    }

    private fun loadHourlyForecast(lon: Double, lat: Double) {
        var apiUrl = "https://api.openweathermap.org/data/2.5/onecall?lat=" + lat + "&lon=" + lon + "&exclude=daily,minutely,current,alerts&units=metric&appid=5c6e0fbe93a07632b752699e3b368056"
        Ion.with(this)
                .load(apiUrl)
                .asJsonObject()
                .setCallback(object : FutureCallback<JsonObject> {
                    override fun onCompleted(e: Exception?, result: JsonObject?) {
                        if (e != null) {
                            e.printStackTrace()
                            Toast.makeText(this@MainActivity, "Server Error: $e", Toast.LENGTH_SHORT).show()
                        } else {
                            Log.d("result_response", result.toString())
                            var weatherList: ArrayList<HourlyForecastModel> = ArrayList()
                            var timeZone: String = result!!.get("timezone_offset").asString
                            var hourly: JsonArray = result.get("hourly").asJsonArray

                            for (i in 1..hourly.size()-1) {
                                var date = hourly.get(i).asJsonObject.get("dt").asLong
                                var temp = hourly.get(i).asJsonObject.get("temp").asDouble//asJsonObject.get("dt").asDouble
                                var icon = hourly.get(i).asJsonObject.get("weather").asJsonArray.get(0).asJsonObject.get("icon").asString

                                weatherList.add(HourlyForecastModel(date, timeZone, temp, icon))
                            }

                            var hourlyForecastAdapter = HourlyForecastAdapter(weatherList, this@MainActivity)
                            rvHourlyForecast.layoutManager = GridLayoutManager(this@MainActivity, 2, GridLayoutManager.HORIZONTAL, false) //LinearLayoutManager(this@MainActivity)
                            rvHourlyForecast.adapter = hourlyForecastAdapter
                        }
                    }
                })
    }

    fun getIdText(id: Int?) : String {
        return when(id) {
            200 -> "Thunderstorm with light rain"
            201 -> "Thunderstorm with rain"
            202 -> "Thunderstorm with heavy rain"
            210 -> "Light thunderstorm"
            211 -> "Thunderstorm"
            212 -> "Heavy thunderstorm"
            221 -> "Ragged thunderstorm"
            230 -> "Thunderstorm with light drizzle"
            231 -> "Thunderstorm with drizzle"
            232 -> "Thunderstorm with heavy drizzle"

            300 -> "Light intensity drizzle"
            301 -> "Drizzle"
            302 -> "Heavy intensity drizzle"
            310 -> "Light intensity drizzle rain"
            311 -> "Drizzle rain"
            312 -> "Heavy intensity drizzle rain"
            313 -> "Shower rain and drizzle"
            314 -> "Heavy shower rain and drizzle"
            321 -> "Shower drizzle"

            500 -> "Light rain"
            501 -> "Moderate rain"
            502 -> "Heavy intensity rain"
            503 -> "Very heavy rain"
            504 -> "Extreme rain"
            511 -> "Freezing rain"
            520 -> "Light intensity shower rain"
            521 -> "Shower rain"
            522 -> "Heavy intensity shower rain"
            531 -> "Ragged shower rain"

            600 -> "Light snow"
            601 -> "Snow"
            602 -> "Heavy snow"
            611 -> "Sleet"
            612 -> "Light shower sleet"
            613 -> "Shower sleet"
            615 -> "Light rain and snow"
            616 -> "Rain and snow"
            620 -> "Light shower snow"
            621 -> "Shower snow"
            622 -> "Heavy shower snow"

            701 -> "Mist"
            711 -> "Smoke"
            721 -> "Haze"
            731 -> "Sand / Dust whirls"
            741 -> "Fog"
            751 -> "Sand"
            761 -> "Dust"
            762 -> "Volcanic ash"
            771 -> "Squalls"
            781 -> "Tornado"

            800 -> "Few clouds: 11-25%"
            801 -> "Scattered clouds: 25-50%"
            802 -> "Broken clouds: 51-84%"
            803 -> "Overcast clouds: 85-100%"
            else -> ""
        }
    }

    fun getBackgroundImage(icon: String) : Int {
        return when(icon) {
            "01d" -> R.drawable.id01
            "01n" -> R.drawable.in01
            "02d" -> R.drawable.id02
            "02n" -> R.drawable.in02
            "03d" -> R.drawable.id03
            "03n" -> R.drawable.in03
            "04d" -> R.drawable.id04
            "04n" -> R.drawable.in04
            "09d" -> R.drawable.id09
            "09n" -> R.drawable.in09
            "10d" -> R.drawable.id10
            "10n" -> R.drawable.in10
            "11d" -> R.drawable.id11
            "11n" -> R.drawable.in11
            "13d" -> R.drawable.id13
            "13n" -> R.drawable.in13
            "50d" -> R.drawable.id50
            "50n" -> R.drawable.in50
            else -> R.color.primary_4
        }
    }
}