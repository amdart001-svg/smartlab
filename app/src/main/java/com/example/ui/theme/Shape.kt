package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// ============================================================================
// SMARTLAB Clean Minimalist Medical Shapes
// ============================================================================

val SmartLabShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),  // Status badges & chips
    small = RoundedCornerShape(10.dp),      // Buttons & input fields
    medium = RoundedCornerShape(14.dp),     // Data cards & list items
    large = RoundedCornerShape(20.dp),      // Modals, dialogs & dashboard hero cards
    extraLarge = RoundedCornerShape(28.dp)  // Bottom sheets & main containers
)
