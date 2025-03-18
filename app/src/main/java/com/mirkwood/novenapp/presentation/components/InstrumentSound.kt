package com.mirkwood.novenapp.presentation.components

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.MediaPlayer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.mirkwood.novenapp.presentation.model.MainModule

@Composable
internal fun InstrumentSound(
    instrument: MainModule.Instrument,
) {
    val context = LocalContext.current
    val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    var sound = remember {
        MediaPlayer.create(
            context,
            instrument.soundRes
        )
    }  // Agregar un sonido en res/raw/
    var lastAcceleration by remember { mutableStateOf(0f) }
    DisposableEffect(Unit) {

        sound.playbackParams =
            sound.playbackParams.setSpeed(1.5f) // 🔥 Ajusta la velocidad más realista
        val sensorListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event?.let {
                    val acceleration = event.values[0] + event.values[1] + event.values[2]
                    if (Math.abs(acceleration - lastAcceleration) > 2) {  // Sensibilidad ajustable
                        sound.start()
                    }
                    lastAcceleration = acceleration
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorManager.registerListener(sensorListener, sensor, SensorManager.SENSOR_DELAY_UI)

        onDispose {
            sensorManager.unregisterListener(sensorListener)
            sound.release()
        }
    }


}
