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

Use Flutter 3.38 or later. On iOS, this example requires iOS 16.0 or later, including for the simulator. The app, notification extension, and CocoaPods targets use this minimum to match the bundled AppoxeeInapp framework.

The iOS example uses Flutter's UIScene lifecycle and registers plugins when the implicit engine initializes, so it can launch when built with Xcode 27. See the [Flutter UIScene migration guide](https://docs.flutter.dev/release/breaking-changes/uiscenedelegate) for details.

1. Update `lib/engage_config.dart` with your Mapp SDK credentials
2. Add your `google-services.json` to `android/app/`
3. Run: `flutter run`

## See Also

- **Main README**: `../README.md` - General plugin usage and API reference
- **Multi-Provider Example**: `../example_multi_provider/` - Example with firebase_messaging integration
