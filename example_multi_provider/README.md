# Mapp SDK Multi-Provider Example (Native Approach)

This example demonstrates how to integrate the Mapp SDK with other push providers using a **simple native FirebaseMessagingService**. This approach works in all app states (foreground, background, quit) with minimal setup.

## Use Case

Use this example when you need to:
- Handle push messages from multiple providers (not just Mapp)
- Have Mapp notifications work in **all app states** (including quit/background)
- Keep setup as simple as possible

## Setup Steps

### 1. Create Unified FirebaseMessagingService

Create `android/app/src/main/java/com/yourcompany/yourapp/UnifiedFirebaseMessagingService.java`:

```java
package com.yourcompany.yourapp;

import androidx.annotation.NonNull;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.mapp.flutter.sdk.MappMessageHandler;

public class UnifiedFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        // Forward token to Mapp SDK
        MappMessageHandler.onNewToken(token, this);
        // TODO: Forward to your other providers
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        // Check if Mapp message (has data["p"])
        if (MappMessageHandler.canHandle(remoteMessage)) {
            // Delegate to Mapp SDK - handles notification display in all states
            MappMessageHandler.handle(remoteMessage, this);
            return;
        }

        // Non-Mapp message - handle with your own logic
        // TODO: Show notification using NotificationManager, etc.
    }
}
```

### 2. Android Manifest

In `android/app/src/main/AndroidManifest.xml`:

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <application>
        <!-- Disable Mapp plugin's default service -->
        <service
            android:name="com.mapp.flutter.sdk.MappFlutterMessagingService"
            tools:node="remove" />
        
        <!-- Your unified service -->
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

### 3. Flutter Code

In your Dart code, just initialize Mapp SDK and set up callbacks:

```dart
import 'package:mapp_sdk/mapp_sdk.dart';

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();
  
  // Initialize Mapp SDK
  await MappSdk.engage(
    'your_sdk_key',
    'your_google_project_id',
    SERVER.L3,
    'your_app_id',
    'your_tenant_id',
  );
  
  // Set up callbacks
  MappSdk.handledRemoteNotification = (arguments) {
    print('Mapp notification received: $arguments');
  };
  
  MappSdk.handledPushOpen = (arguments) {
    print('Mapp notification opened: $arguments');
  };
  
  runApp(MyApp());
}
```

**That's it!** No `firebase_messaging` plugin needed. All push handling happens natively.

## How It Works

1. **All FCM messages** arrive at `UnifiedFirebaseMessagingService.onMessageReceived()`
2. **Check if Mapp message** using `MappMessageHandler.canHandle()` (checks for `data["p"]`)
3. **If Mapp message** → `MappMessageHandler.handle()` delegates to Mapp SDK
   - Mapp SDK displays notification natively (works in all app states)
   - Flutter callbacks are triggered when app is open
4. **If non-Mapp message** → handle with your own logic

## Benefits

- ✅ **Works in all app states** (foreground, background, quit)
- ✅ **No Dart background handlers** needed
- ✅ **No firebase_messaging plugin** dependency
- ✅ **Minimal setup** - just one native service class
- ✅ **Full Mapp SDK features** (rich notifications, deep links, etc.)

## Running the Example

1. Update `lib/engage_config.dart` with your Mapp SDK credentials
2. Add your `google-services.json` to `android/app/`
3. Run: `flutter run`

## See Also

- **Main README**: `../README.md` - General plugin usage
- **Integration Guide**: `../INTEGRATION_FIREBASE_MESSAGING.md` - Detailed integration instructions
- **Mapp Only Example**: `../example_mapp_only/` - Example with default Mapp service
