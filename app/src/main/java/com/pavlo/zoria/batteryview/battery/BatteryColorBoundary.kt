package com.pavlo.zoria.batteryview.battery

import androidx.annotation.ColorInt

data class BatteryColorBoundary(
    var topBound: Int,
    @ColorInt var color: Int
)