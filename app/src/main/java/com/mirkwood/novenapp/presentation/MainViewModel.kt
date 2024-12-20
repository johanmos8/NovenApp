package com.mirkwood.novenapp.presentation

import android.content.Context
import androidx.lifecycle.LiveData
import java.time.LocalDate
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.mirkwood.novenapp.presentation.model.Novena

class MainViewModel : ViewModel() {



    fun getContent(context: Context): Novena {
        val json = loadJSONFromAsset(context, "content.json")
        return Gson().fromJson(json, Novena::class.java)
    }

    private fun loadJSONFromAsset(context: Context, fileName: String): String? {
        return try {
            val inputStream = context.assets.open(fileName)
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            String(buffer, Charsets.UTF_8)
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }

    fun getNovenaDay(): Int? {
        val today = LocalDate.now()
        val start = LocalDate.of(today.year, 12, 16)
        val end = LocalDate.of(today.year, 12, 24)

        return if (today in start..end) {
            // Calculamos el día basado en el rango
            (today.dayOfMonth - start.dayOfMonth + 1)
        } else {
            null // Retornamos null si la fecha no está en el rango
        }
    }


}