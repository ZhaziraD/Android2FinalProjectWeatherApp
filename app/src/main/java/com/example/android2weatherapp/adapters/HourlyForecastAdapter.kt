package com.example.android2weatherapp.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.android2weatherapp.ImageNicer
import com.example.android2weatherapp.R
import com.example.android2weatherapp.models.HourlyForecastModel
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


class HourlyForecastAdapter : RecyclerView.Adapter<HourlyForecastAdapter.ViewHolder> {

    inner class ViewHolder : RecyclerView.ViewHolder {
        var tvDate: TextView
        var tvTemp: TextView
        var iconWeather: ImageView

        constructor(itemView: View) : super(itemView) {
            tvDate = itemView.findViewById(R.id.tvTime)
            tvTemp = itemView.findViewById(R.id.tvTemp)
            iconWeather = itemView.findViewById(R.id.ivWeatherDaily)
        }
    }

    private var weatherList: List<HourlyForecastModel>
    private var context: Context

    constructor(weatherList: List<HourlyForecastModel>, context: Context) : super() {
        this.weatherList = weatherList
        this.context = context
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var view = LayoutInflater.from(context).inflate(R.layout.layout_hourly_forecast, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var weather = weatherList[position]
        holder.tvTemp.text = weather.temp.toInt().toString() + "º"
        holder.iconWeather.setImageResource(ImageNicer.getIconImage(weather.icon))

        var date = Date(weather.date * 1000)
        var dateFormat: DateFormat = SimpleDateFormat("HH:mm", Locale.ENGLISH) //*MM-dd-yyyy HH:mm
        dateFormat.timeZone = TimeZone.getTimeZone(weather.timeZone)
        holder.tvDate.text = dateFormat.format(date)
    }

    override fun getItemCount(): Int {
        return weatherList.size
    }
}