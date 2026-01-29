package com.mapp.flutter.multi_provider;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.mapp.flutter.sdk.MappMessageHandler;

/**
 * Unified FirebaseMessagingService that handles both Mapp and non-Mapp push messages.
 * 
 * This service:
 * - Delegates Mapp messages (identified by data["p"]) to Mapp SDK for full native handling
 * - Allows you to handle non-Mapp messages with your own logic
 * - Works in all app states (foreground, background, quit) because it's native
 */
public class UnifiedFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        
        // Forward token to Mapp SDK
        MappMessageHandler.onNewToken(token, getApplicationContext());
        
        // TODO: Forward token to your other push providers / backend if needed
        android.util.Log.d("UnifiedFCM", "FCM token: " + token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        // Check if this is a Mapp message (has data["p"])
        if (MappMessageHandler.canHandle(remoteMessage)) {
            // Delegate to Mapp SDK - it will handle notification display
            // and all Mapp-specific processing (works in all app states)
            MappMessageHandler.handle(remoteMessage, getApplicationContext());
            return;
        }

        // Non-Mapp message - handle with your own logic
        // For example, show a notification using NotificationManager
        android.util.Log.d("UnifiedFCM", "Non-Mapp message: " + remoteMessage.getData());
        
        // TODO: Handle non-Mapp messages here
        // You can use NotificationManager, flutter_local_notifications, etc.
    }
}
