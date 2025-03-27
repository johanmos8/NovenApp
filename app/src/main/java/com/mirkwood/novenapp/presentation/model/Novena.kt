package com.mirkwood.novenapp.presentation.model

data class Novena(
    val general: General,
    val dias: List<Dia>,
    val gozos: List<Gozo>
)

data class Dia(
    val dia: Int,
    val titulo: String,
    val oracion_inicial: String,
    val reflexion: String
)
data class General(
    val oracion_todos_los_dias: String,
    val oracion_virgen_maria: String,
    val oracion_san_jose: String,
    val oracion_niño_jesus: String
)
data class Gozo(
    val id: Int,
    val texto: String
)
