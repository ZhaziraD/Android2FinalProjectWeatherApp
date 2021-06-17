package com.example.android2weatherapp

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.math.BigDecimal

object ImageNicer {

    fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        // Raw height and width of image
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {

            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }

    fun decodeSampledBitmapFromResource(
            res: Resources,
            resId: Int,
            reqWidth: Int,
            reqHeight: Int
    ): Bitmap {
        // First decode with inJustDecodeBounds=true to check dimensions
        return BitmapFactory.Options().run {
            inJustDecodeBounds = true
            BitmapFactory.decodeResource(res, resId, this)

            // Calculate inSampleSize
            inSampleSize = calculateInSampleSize(this, reqWidth, reqHeight)

            // Decode bitmap with inSampleSize set
            inJustDecodeBounds = false

            BitmapFactory.decodeResource(res, resId, this)
        }
    }

    fun getIconImage(icon: String) : Int {
        return when(icon) {
            "01d" -> R.drawable.d01
            "01n" -> R.drawable.n01
            "02d" -> R.drawable.d02
            "02n" -> R.drawable.n02
            "03d" -> R.drawable.n03
            "03n" -> R.drawable.n03
            "04d" -> R.drawable.n04
            "04n" -> R.drawable.n04
            "09d" -> R.drawable.n09
            "09n" -> R.drawable.n09
            "10d" -> R.drawable.d10
            "10n" -> R.drawable.n10
            "11d" -> R.drawable.d11
            "11n" -> R.drawable.n11
            "13d" -> R.drawable.d13
            "13n" -> R.drawable.n13
            "50d" -> R.drawable.d50
            "50n" -> R.drawable.n50
            else -> R.drawable.n50
        }
    }
}