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
    isPlaying: Boolean = false
) {
    val context = LocalContext.current

    var sound = remember {
        MediaPlayer.create(
            context,
            instrument.soundRes
        )
    }
    when (instrument) {
        MainModule.Instrument.Maraca,
        MainModule.Instrument.Tambourine -> {
            PlayInstrument(sound, context)
        }

        MainModule.Instrument.Music -> {
            if (isPlaying) {
                sound.start()
            } else {
                sound.pause()
            }
        }
    }
    DisposableEffect(Unit) {
        onDispose {
            sound.release() // Liberar recursos cuando el Composable se destruye
        }

    }


}

@Composable
fun PlayInstrument(sound: MediaPlayer, context: Context) {
    val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    // Agregar un sonido en res/raw/
    var lastAcceleration by remember { mutableStateOf(0f) }
    DisposableEffect(Unit) {

        val sensorListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event?.let {
                    val acceleration = event.values[0] + event.values[1] + event.values[2]
                    if (Math.abs(acceleration - lastAcceleration) > 8) {  // Sensibilidad ajustable
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
        }
    }
}
