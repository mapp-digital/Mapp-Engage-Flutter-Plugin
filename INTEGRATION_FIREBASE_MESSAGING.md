# Integrating Mapp Flutter Plugin for Multi-Provider Push Support

This guide explains how to integrate the Mapp Flutter plugin when you need to handle push messages from multiple providers.

## Overview

When you need to handle push messages from multiple providers, you have two options:

1. **Default (Mapp only)**: Works out of the box - no additional setup required. The plugin's built-in `MappFlutterMessagingService` handles all Mapp messages automatically in all app states.
2. **Multiple providers (Native approach)**: Create a simple native `FirebaseMessagingService` that delegates Mapp messages to the Mapp SDK while handling other providers

This guide covers **option 2** - the recommended native approach for multi-provider support.

## Why Native Approach?

The native approach is **simpler and more reliable** because:
- ✅ Works in **all app states** (foreground, background, quit) without special setup
- ✅ No Dart background handlers or plugin registration needed
- ✅ No additional Flutter plugin dependencies required
- ✅ Full Mapp SDK features (rich notifications, deep links) work everywhere

## Setup Instructions

### Step 1: Create Unified FirebaseMessagingService

Create a new file in your app: `android/app/src/main/java/com/yourcompany/yourapp/UnifiedFirebaseMessagingService.java`

```java
package com.yourcompany.yourapp;

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
        MappMessageHandler.onNewToken(token, this);
        
        // TODO: Forward token to your other push providers / backend if needed
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        // Check if this is a Mapp message (has data["p"])
        if (MappMessageHandler.canHandle(remoteMessage)) {
            // Delegate to Mapp SDK - it will handle notification display
            // and all Mapp-specific processing (works in all app states)
            MappMessageHandler.handle(remoteMessage, this);
            return;
        }

        // Non-Mapp message - handle with your own logic
        // For example, show a notification using NotificationManager
        android.util.Log.d("UnifiedFCM", "Non-Mapp message: " + remoteMessage.getData());
        
        // TODO: Handle non-Mapp messages here
        // You can use NotificationManager, flutter_local_notifications, etc.
    }
}
```

### Step 2: Android Manifest

Update your `android/app/src/main/AndroidManifest.xml`:

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <application>
        <!-- Disable Mapp plugin's default FirebaseMessagingService -->
        <service
            android:name="com.mapp.flutter.sdk.MappFlutterMessagingService"
            tools:node="remove" />
        
        <!-- Your unified FirebaseMessagingService -->
        <service
            android:name=".UnifiedFirebaseMessagingService"
            android:exported="false">
            <intent-filter>
                <action android:name="com.google.firebase.MESSAGING_EVENT" />
            </intent-filter>
        </service>
    </application>
</manifest>
```

### Step 3: Flutter Code

In your Dart code, just initialize the Mapp SDK and set up callbacks:

```dart
import 'package:mapp_sdk/mapp_sdk.dart';

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();
  
  // Initialize Mapp SDK
  await MappSdk.engage(
    'your_sdk_key',
    'your_google_project_id',
    SERVER.L3, // or your server
    'your_app_id',
    'your_tenant_id',
  );
  
  // Set up Mapp SDK callbacks
  MappSdk.handledRemoteNotification = (dynamic arguments) {
    debugPrint('Mapp notification received: $arguments');
    // Handle Mapp notification in your app
  };

  MappSdk.handledPushOpen = (dynamic arguments) {
    debugPrint('Mapp notification opened: $arguments');
    // Handle Mapp notification tap
  };

  runApp(const MyApp());
}
```

**That's it!** All push handling happens natively - no additional Flutter plugins needed.

## How It Works

### Message Flow

1. **Push message arrives** → handled by your `UnifiedFirebaseMessagingService`
2. **Check for Mapp message** → `MappMessageHandler.canHandle()` checks for `data["p"]`
3. **If Mapp message**:
   - `MappMessageHandler.handle()` delegates to the native Mapp SDK
   - Mapp SDK processes and displays the notification (works in all app states)
   - Flutter callbacks (`MappSdk.handledRemoteNotification`, `MappSdk.handledPushOpen`, etc.) are triggered when app is open
4. **If non-Mapp message**:
   - Handle entirely with your own logic in `onMessageReceived()`

### Message Identification

Mapp messages are identified by the presence of key `"p"` in the FCM message `data` payload:

```json
{
  "data": {
    "p": "mapp_payload_value",
    "other_key": "value"
  }
}
```

Any FCM message without `"p"` in `data` is treated as a regular (non-Mapp) message.

## Important Notes

1. **Mapp messages are handled automatically**: When you call `MappMessageHandler.handle()`, the native Mapp SDK handles notification display and processing in **all app states** (foreground, background, quit). Your Flutter app receives callbacks via `MappSdk.handledRemoteNotification`, `MappSdk.handledPushOpen`, etc. when the app is open.

2. **Non-Mapp messages require your handling**: You need to implement notification display for non-Mapp messages in `UnifiedFirebaseMessagingService.onMessageReceived()`. You can use `NotificationManager` directly, or call into Flutter via method channels if you prefer.

3. **No Dart background handlers needed**: Since everything happens natively, you don't need any additional Flutter plugins or Dart background handlers.

4. **Token forwarding**: Make sure to forward FCM tokens to all your push providers (Mapp via `MappMessageHandler.onNewToken()`, and your other providers in the same method).

## Example: Handling Non-Mapp Messages

If you want to show notifications for non-Mapp messages, you can do it directly in the native service:

```java
@Override
public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
    if (MappMessageHandler.canHandle(remoteMessage)) {
        MappMessageHandler.handle(remoteMessage, this);
        return;
    }

    // Non-Mapp message - show notification
    NotificationManager notificationManager = 
        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    
    NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "default_channel")
        .setSmallIcon(R.drawable.ic_notification)
        .setContentTitle(remoteMessage.getNotification().getTitle())
        .setContentText(remoteMessage.getNotification().getBody())
        .setAutoCancel(true);
    
    notificationManager.notify(0, builder.build());
}
```

Or you can forward the message to Flutter via method channels if you prefer Dart-side handling.

## Troubleshooting

- **Mapp messages not working**: Ensure `MappSdk.engage()` is called before messages arrive
- **Messages not being handled**: Verify you've removed the plugin's service from your manifest and registered your `UnifiedFirebaseMessagingService`
- **Token not updating**: Ensure `MappMessageHandler.onNewToken()` is called in your service's `onNewToken()` method
- **Notifications not showing**: For non-Mapp messages, make sure you're creating and showing notifications in `onMessageReceived()`

## See Also

- **Example**: `example_multi_provider/` - Complete working example with `UnifiedFirebaseMessagingService`
- **Main README**: `README.md` - General plugin usage and API reference
