package com.mapp.flutter.multi_provider

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.mapp.flutter.sdk.MappMessageHandler

class UnifiedFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        // Forward token to Mapp SDK
        MappMessageHandler.onNewToken(token, applicationContext)

        // TODO: Forward token to your other push providers / backend if needed
        Log.d("UnifiedFCM", "FCM token: $token")
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Check if this is a Mapp message (has data["p"])
        if (MappMessageHandler.canHandle(remoteMessage)) {
            // Delegate to Mapp SDK - it will handle notification display
            // and all Mapp-specific processing (works in all app states)
            MappMessageHandler.handle(remoteMessage, applicationContext)
            return
        }

        // Non-Mapp message - handle with your own logic
        // For example, show a notification using NotificationManager
        Log.d("UnifiedFCM", "Non-Mapp message: ${remoteMessage.data}")

        // TODO: Handle non-Mapp messages here
        // You can use NotificationManager, flutter_local_notifications, etc.
    }
}
