# Mapp SDK Example (Mapp Only)

This example demonstrates the full Mapp SDK functionality when Mapp is your only push notification provider.

## Use Case

Use this example when:
- Mapp is your only push notification provider
- You want the plugin to handle all Mapp push messages automatically
- You don't need to integrate with other push providers

## Features Demonstrated

- SDK initialization and configuration
- Push notification handling (automatic via plugin's service)
- InApp message triggers and callbacks
- Deep link handling
- Alias management
- Tags management
- Custom attributes
- Device information
- Inbox messages
- Geofencing
- And more...

## Setup

1. Update `lib/engage_config.dart` with your Mapp SDK credentials
2. Add your `google-services.json` to `android/app/`
3. Run: `flutter run`

## See Also

- **Main README**: `../README.md` - General plugin usage and API reference
- **Multi-Provider Example**: `../example_multi_provider/` - Example with firebase_messaging integration
