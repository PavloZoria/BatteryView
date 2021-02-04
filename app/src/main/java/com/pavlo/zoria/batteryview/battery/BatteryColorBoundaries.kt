package com.pavlo.zoria.batteryview.battery

data class BatteryColorBoundaries(
    val boundaries: MutableList<BatteryColorBoundary>
) {
    constructor(vararg boundary: BatteryColorBoundary) : this(
        boundary.sortedBy { it.topBound }.toMutableList())
}