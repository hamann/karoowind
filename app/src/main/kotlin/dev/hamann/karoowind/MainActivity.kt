package dev.hamann.karoowind

import android.app.Activity
import android.os.Bundle

/**
 * Minimal launcher activity — placeholder for future settings UI.
 */
class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // TODO: Add settings UI (HR zone thresholds, manual fan speed, etc.)
        finish()
    }
}
