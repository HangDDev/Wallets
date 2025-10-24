package com.example.financemanager.ui.utils

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp

// Alternative: More precise swipe detection with density-aware conversion
@Composable
fun Modifier.swipeBackGesture(
    onBack: () -> Unit,
    swipeThreshold: Dp = 100.dp
): Modifier {
    val density = LocalDensity.current
    val thresholdPx = remember(swipeThreshold, density) {
        with(density) { swipeThreshold.toPx() }
    }

    return pointerInput(thresholdPx, onBack) {
        detectHorizontalDragGestures { change, dragAmount ->
            // Swipe from left edge (within 50dp) with minimum threshold drag
            if (change.position.x < 50.dp.toPx() && dragAmount > thresholdPx) {
                onBack()
            }
        }
    }
}