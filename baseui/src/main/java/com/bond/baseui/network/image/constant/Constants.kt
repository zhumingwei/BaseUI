package com.bond.baseui.network.image.constant

import android.content.Context

object Constants {
    var density: Float = 0.toFloat()

    var Quality_THUMBNAIL = 200
    var Quality_MIDDLE = 480
    var Quality_HIGH = -1


    fun init(context: Context) {
        density = context.resources.displayMetrics.density
        Quality_THUMBNAIL = (50 * density).toInt()
        Quality_MIDDLE = (160 * density).toInt()
        Quality_HIGH = (500 * density).toInt()
    }
}
