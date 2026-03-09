package com.mapp.flutter.sdk

import android.os.Handler
import android.os.Looper
import android.util.Log
import io.flutter.plugin.common.MethodChannel
import java.util.concurrent.CopyOnWriteArrayList

object EventEmitter {
    private val pendingEvents = CopyOnWriteArrayList<Event>()
    private val mainHandler = Handler(Looper.getMainLooper())

    @Volatile
    private var channel: MethodChannel? = null

    fun attachChannel(methodChannel: MethodChannel): EventEmitter {
        channel = methodChannel
        sendPendingEvents()
        return this
    }

    @Synchronized
    fun sendEvent(event: Event) {
        pendingEvents.add(event)
        sendPendingEvents()
    }

    private fun sendPendingEvents() {
        val events = pendingEvents.toList()
        events.forEach { event ->
            mainHandler.post { emit(event) }
        }
    }

    private fun emit(event: Event): Boolean {
        return try {
            val currentChannel = channel
            if (currentChannel != null) {
                if (event.body.isNotEmpty()) {
                    currentChannel.invokeMethod(event.name, event.body)
                }
                pendingEvents.remove(event)
                true
            } else {
                Log.w("EventEmitter", "CHANNEL IS NULL!!!")
                false
            }
        } catch (_: Exception) {
            false
        }
    }

    @Synchronized
    fun detachChannel() {
        channel = null
    }

    fun getChannel(): MethodChannel? = channel
}
