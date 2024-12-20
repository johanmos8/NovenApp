package com.mirkwood.novenapp.presentation.model

sealed class NovenaTab(var title: String) {
    object OracionTodosLosDias : NovenaTab("Oración para todos los días")
    object OracionALaVirgen : NovenaTab("Oración a la Virgen")
    object OracionSanJose : NovenaTab("Oración a San José")
    object Consideracion : NovenaTab("Consideración")
    object Gozos : NovenaTab("Gozos")
}
