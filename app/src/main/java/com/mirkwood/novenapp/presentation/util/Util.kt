package com.mirkwood.novenapp.presentation.util

import java.time.LocalDate
import java.time.ZoneId

const val TARGET_MONTH = 12
const val TARGET_DAY = 25
const val LAST_DAY = 24
const val INITIAL_DAY = 16

object Util {
    private val currentDate = LocalDate.now()
    val currentYear = LocalDate.now().year

    // Verifica si ya pasó el 25 de diciembre de este año
    private val isAfterChristmas = currentDate.monthValue == TARGET_MONTH && currentDate.dayOfMonth > TARGET_DAY

    // Si ya pasó Navidad, usa el próximo año; si no, usa el año actual
    private val targetYear = if (isAfterChristmas) currentYear + 1 else currentYear
    // Define la fecha objetivo (Navidad)
    val targetDate = LocalDate.of(targetYear, TARGET_MONTH, TARGET_DAY) // Año, mes, día
        .atStartOfDay(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()

    // Calcula el tiempo restante en milisegundos
    fun calculateTimeRemaining(): Long {
        return targetDate - System.currentTimeMillis()
    }
}
