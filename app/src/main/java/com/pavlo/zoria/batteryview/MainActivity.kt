package com.pavlo.zoria.batteryview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.lifecycle.lifecycleScope
import com.pavlo.zoria.batteryview.battery.BatteryLevelIndicatorView
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val batteryLevelIndicator: BatteryLevelIndicatorView =
            findViewById(R.id.batteryLevelIndicatorView)

        findViewById<Button>(R.id.fillUp).setOnClickListener {
            with(lifecycleScope) {
                launch {
                    while (batteryLevelIndicator.percent != 100f) {
                        delay(25)
                        batteryLevelIndicator.percent += 1
                    }
                }
            }
        }
        findViewById<Button>(R.id.freeUp).setOnClickListener {
            with(lifecycleScope) {
                launch {
                    while (batteryLevelIndicator.percent != 0f) {
                        delay(25)
                        batteryLevelIndicator.percent -= 1
                    }
                }
            }
        }
    }
}