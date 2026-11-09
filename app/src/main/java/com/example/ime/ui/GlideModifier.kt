package com.example.ime.ui

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import com.example.ime.engine.Pt

/**
 * Watches touches without stealing taps. Once a finger has moved clearly further than a tap
 * (and started where [canStart] allows) the gesture is consumed so the keys underneath do not
 * also fire, and the whole path is handed to [onGlide] when the finger lifts.
 */
fun Modifier.glideTyping(
    enabled: Boolean,
    canStart: (Offset) -> Boolean,
    onGlide: (List<Pt>) -> Unit
): Modifier = pointerInput(enabled) {
    if (!enabled) return@pointerInput
    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
        if (!canStart(down.position)) return@awaitEachGesture
        val path = mutableListOf(Pt(down.position.x, down.position.y))
        val threshold = viewConfiguration.touchSlop * 4f
        var gliding = false
        while (true) {
            val event = awaitPointerEvent(PointerEventPass.Initial)
            val change = event.changes.firstOrNull { it.id == down.id } ?: break
            path += Pt(change.position.x, change.position.y)
            if (!gliding && (change.position - down.position).getDistance() > threshold) gliding = true
            if (gliding) change.consume()
            if (!change.pressed) break
        }
        if (gliding) onGlide(path)
    }
}
