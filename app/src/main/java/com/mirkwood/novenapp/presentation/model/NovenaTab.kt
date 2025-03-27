package com.mirkwood.novenapp.presentation.model

import com.mirkwood.novenapp.R

sealed class NovenaTab(var titleResId: Int) {
    object OracionTodosLosDias : NovenaTab(R.string.oracion_todos_los_dias)
    object OracionALaVirgen : NovenaTab(R.string.oracion_a_la_virgen)
    object OracionSanJose : NovenaTab(R.string.oracion_a_san_jose)
    object Consideracion : NovenaTab(R.string.consideracion)
    object OracionAJesus : NovenaTab(R.string.oracion_a_jesus)
    object Gozos : NovenaTab(R.string.gozos)
}
