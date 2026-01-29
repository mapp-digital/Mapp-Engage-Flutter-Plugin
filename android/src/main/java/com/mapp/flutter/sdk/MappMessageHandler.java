package com.mapp.flutter.sdk;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.appoxee.Appoxee;
import com.appoxee.push.fcm.MappMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Helper class for identifying and handling Mapp SDK push messages.
 * 
 * This class allows client applications to use their own FirebaseMessagingService
 * while delegating Mapp messages to the underlying Mapp SDK.
 * 
 * Usage example:
 * <pre>
 * public class MyFirebaseMessagingService extends FirebaseMessagingService {
 *     {@literal @}Override
 *     public void onMessageReceived(RemoteMessage message) {
 *         if (MappMessageHandler.canHandle(message)) {
 *             MappMessageHandler.handle(message, this);
 *         } else {
 *             // Handle non-Mapp messages
 *         }
 *     }
 *     
 *     {@literal @}Override
 *     public void onNewToken(String token) {
 *         super.onNewToken(token);
 *         MappMessageHandler.onNewToken(token, this);
 *     }
 * }
 * </pre>
 */
public class MappMessageHandler {
    
    /**
     * Checks if a RemoteMessage is from Mapp SDK.
     * 
     * Mapp messages are identified by the presence of a key "p" in the data payload.
     * 
     * @param remoteMessage The RemoteMessage to check
     * @return true if the message is from Mapp SDK (has key "p"), false otherwise
     */
    public static boolean canHandle(@NonNull RemoteMessage remoteMessage) {
        if (remoteMessage == null) {
            return false;
        }
        
        Map<String, String> data = remoteMessage.getData();
        if (data == null || data.isEmpty()) {
            return false;
        }
        
        // Check if there is a key "p" in the data payload
        return data.containsKey("p");
    }
    
    /**
     * Handles a Mapp SDK push message by delegating to the underlying Mapp SDK.
     * 
     * This method ensures the SDK is initialized and then processes the message
     * through MappMessagingService using a helper service instance.
     * 
     * @param remoteMessage The RemoteMessage to handle
     * @param context The application context (should be from FirebaseMessagingService)
     */
    public static void handle(@Nullable RemoteMessage remoteMessage, @Nullable Context context) {
        if (remoteMessage == null || context == null) {
            return;
        }
        
        // Ensure SDK is initialized
        provideSdkInitialization(context);
        
        // Create a helper service instance that can process the message
        // We use a wrapper that properly extends MappMessagingService
        try {
            HelperMessagingService helperService = new HelperMessagingService(context);
            helperService.onCreate();
            helperService.onMessageReceived(remoteMessage);
        } catch (Exception e) {
            // Log error but don't crash - message might still be processed by Mapp SDK
            android.util.Log.e("MappMessageHandler", "Failed to handle Mapp message", e);
        }
    }
    
    /**
     * Internal helper class that extends MappMessagingService to handle messages
     * without being registered in the manifest.
     * This is a minimal wrapper that allows us to delegate to Mapp SDK's message handling.
     */
    private static class HelperMessagingService extends MappMessagingService {
        private final Application appContext;
        
        HelperMessagingService(Context context) {
            this.appContext = (Application) context.getApplicationContext();
        }
        
        @Override
        public android.content.Context getApplicationContext() {
            return appContext;
        }
        
        @Override
        public void onCreate() {
            // Attach the application context using reflection
            try {
                // On modern Android versions the base context field lives on ContextWrapper
                java.lang.reflect.Field contextField = android.content.ContextWrapper.class.getDeclaredField("mBase");
                contextField.setAccessible(true);
                contextField.set(this, appContext);
            } catch (Exception e) {
                // If reflection fails, try alternative approach
                android.util.Log.w("MappMessageHandler", "Could not set service context via reflection", e);
            }
            super.onCreate();
            // Ensure Appoxee is engaged
            if (!Appoxee.instance().isReady()) {
                Appoxee.engage(appContext);
            }
        }
    }
    
    /**
     * Handles FCM token refresh for Mapp SDK.
     * 
     * This method should be called from the client's FirebaseMessagingService.onNewToken()
     * to ensure Mapp SDK receives token updates.
     * 
     * @param token The new FCM token
     * @param context The application context
     */
    public static void onNewToken(@NonNull String token, @NonNull Context context) {
        if (token == null || context == null) {
            return;
        }
        
        // Ensure SDK is initialized
        provideSdkInitialization(context);
        
        // Try to use helper service for token update
        try {
            HelperMessagingService helperService = new HelperMessagingService(context);
            helperService.onCreate();
            helperService.onNewToken(token);
        } catch (Exception e) {
            // Fallback: set token directly via Appoxee if available
            try {
                Appoxee.instance().setToken(token);
            } catch (Exception ex) {
                android.util.Log.e("MappMessageHandler", "Failed to update Mapp token", ex);
            }
        }
    }
    
    /**
     * Ensures the Mapp SDK is initialized before processing messages.
     * 
     * This method waits for the SDK to be ready (up to 4 seconds) before proceeding.
     * 
     * @param context The application context
     */
    private static void provideSdkInitialization(@NonNull Context context) {
        if (context == null) {
            return;
        }
        
        // Ensure Appoxee is engaged
        try {
            if (!Appoxee.instance().isReady()) {
                Application app = (Application) context.getApplicationContext();
                Appoxee.engage(app);
            }
        } catch (Exception e) {
            // If engage fails, try to continue anyway
            android.util.Log.w("MappMessageHandler", "Failed to engage Appoxee", e);
        }
        
        // Wait for SDK to be ready (similar to MappFlutterMessagingService)
        int retries = 0;
        try {
            if (!Appoxee.instance().isReady()) {
                while (retries++ < 20 && !Appoxee.instance().isReady()) {
                    try {
                        TimeUnit.MILLISECONDS.sleep(200);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        } catch (Exception e) {
            // If SDK is not available, log but continue
            android.util.Log.w("MappMessageHandler", "SDK initialization check failed", e);
        }
    }
}
