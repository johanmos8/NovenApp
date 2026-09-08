package com.mirkwood.novenapp.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// "Novena Contemplative MD3" rounded scale (sm/DEFAULT/md/lg/xl), mapped onto Material3's
// 5-tier shape scale in ascending order. The spec's pill ("full") radius has no scale
// slot of its own - components that need it apply CircleShape/a 50% corner shape directly.
val Shapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(16.dp),
    medium = RoundedCornerShape(24.dp),
    large = RoundedCornerShape(32.dp),
    extraLarge = RoundedCornerShape(48.dp)
)
