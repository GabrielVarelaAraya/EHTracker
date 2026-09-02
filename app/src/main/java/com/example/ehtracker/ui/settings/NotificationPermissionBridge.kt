package com.example.ehtracker.ui.settings

object NotificationPermissionBridge {
    var onResult: ((Boolean) -> Unit)? = null

    fun deliverResult(granted: Boolean) {
        onResult?.invoke(granted)
        onResult = null
    }
}