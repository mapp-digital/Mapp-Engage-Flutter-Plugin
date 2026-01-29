# mapp_sdk

Mapp SDK plugin enables the usage of Mapp Engage platform, such as push notifications and InApp messages segmented sendout.

For implementation details please take a look at: 
https://docs.mapp.com/docs/flutter

## Getting Started

### 1. Add the dependency

Add `mapp_sdk` to your `pubspec.yaml`:

```yaml
dependencies:
  mapp_sdk: ^your_version
```

### 2. Initialize the SDK

Initialize the Mapp SDK in your Flutter app:

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
  
  runApp(MyApp());
}
```

### 3. Set up Callbacks

Set up callbacks to receive notifications and events:

```dart
// Push notification callbacks
MappSdk.handledRemoteNotification = (dynamic arguments) {
  print('Mapp notification received: $arguments');
  // Handle notification received
};

MappSdk.handledPushOpen = (dynamic arguments) {
  print('Mapp notification opened: $arguments');
  // Handle notification tap
};

// InApp message callbacks
MappSdk.didReceiveInappMessageWithIdentifier = (dynamic arguments) {
  print('InApp message received: $arguments');
  // Handle InApp message
};

MappSdk.didReceiveDeepLinkWithIdentifier = (dynamic arguments) {
  print('Deep link received: $arguments');
  // Handle deep link
};
```

## Push Notifications

By default, the plugin handles Mapp push notifications automatically via its built-in `MappFlutterMessagingService` on Android. No additional setup is required.

- Mapp messages are received, parsed, and displayed by the native Mapp SDK
- Your Flutter app receives callbacks via `MappSdk.handledRemoteNotification`, `MappSdk.handledPushOpen`, etc.

### Using with firebase_messaging Plugin

If you need to handle push messages from multiple providers (e.g., using the `firebase_messaging` plugin alongside Mapp), see **[INTEGRATION_FIREBASE_MESSAGING.md](INTEGRATION_FIREBASE_MESSAGING.md)** for detailed integration instructions.

## Common API Methods

Below are short examples for the most commonly used APIs. See the online docs for full details.

### SDK Initialization

```dart
// Initialize Mapp SDK with optional notificationMode
await MappSdk.engage(
  sdkKey,
  googleProjectId,
  server,
  appID,
  tenantID,
  NotificationMode.backgroundAndForeground, // or backgroundOnly, foregroundOnly, etc.
);

// Check if SDK is ready
final ready = await MappSdk.isReady();
print('Mapp SDK ready: $ready');
```

### Push Notifications

```dart
// Enable / disable push
await MappSdk.setPushEnabled(true);
await MappSdk.setPushEnabled(false);

// Check if push is enabled
final isEnabled = await MappSdk.isPushEnabled();
print('Push enabled: $isEnabled');

// Request Android 13+ POST_NOTIFICATIONS permission
final granted = await MappSdk.requestPermissionPostNotifications();
print('Post notifications permission granted: $granted');

// Remove badge number (iOS)
final badgeResult = await MappSdk.removeBadgeNumber();
print('Remove badge result: $badgeResult');
```

### Device Management

```dart
// Set alias
await MappSdk.setAlias('user123');

// Set alias and resend custom attributes
await MappSdk.setAliasWithResend('user123', true);

// Get alias
final alias = await MappSdk.getAlias();
print('Current alias: $alias');

// Get device info
final deviceInfo = await MappSdk.getDeviceInfo();
print('Device info: $deviceInfo');
```

### Tags

```dart
// Add a tag
final added = await MappSdk.addTag('premium_user');
print('Tag added: $added');

// Get all tags
final tags = await MappSdk.getTags();
print('Tags: $tags');

// Remove a tag
final removed = await MappSdk.removeTag('premium_user');
print('Tag removed: $removed');
```

### InApp Messages & Inbox

```dart
// Trigger an InApp message by event name
await MappSdk.triggerInApp('app_open');

// Fetch all inbox messages (JSON string)
final inboxJson = await MappSdk.fetchInboxMessage();
print('Inbox messages: $inboxJson');

// Fetch a single inbox message by id
final singleJson = await MappSdk.fetchInBoxMessageWithMessageId(12345);
print('Inbox message: $singleJson');

// Mark inbox/InApp messages as read/unread/deleted
await MappSdk.inAppMarkAsRead('templateId', 'eventId');
await MappSdk.inAppMarkAsUnread('templateId', 'eventId');
await MappSdk.inAppMarkAsDeleted('templateId', 'eventId');
```

### Custom Attributes

```dart
// Set custom attributes
await MappSdk.setCustomAttributes({
  'firstName': 'John',
  'lastName': 'Doe',
  'age': 30,
});

// Get selected custom attributes
final attrs = await MappSdk.getCustomAttributes(['firstName', 'age']);
print('Custom attributes: $attrs');
```

### Geofencing (Android)

```dart
// Start geofencing
final startStatus = await MappSdk.startGeoFencing();
print('Start geofencing: $startStatus');

// Stop geofencing
final stopStatus = await MappSdk.stopGeoFencing();
print('Stop geofencing: $stopStatus');
```

## Troubleshooting

- **SDK not ready**: Ensure `MappSdk.engage()` is called before using other methods and check with `await MappSdk.isReady()`
- **Push notifications not working**: Verify that `MappSdk.engage()` has been called and push is enabled
- **Callbacks not firing**: Make sure you've set up the callback handlers before initializing the SDK
