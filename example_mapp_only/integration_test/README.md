# Android Smoke Matrix

This folder contains Android integration smoke tests that validate the Flutter plugin contract against a real device/emulator and native plugin wiring.

## Test file

- `android_smoke_matrix_test.dart`
- `android_smoke_matrix_test.dart` now includes a full Flutter API smoke matrix:
  - engage/state methods
  - push opt in/out + alias
  - inbox fetch + mark methods
  - in-app trigger
  - tags/custom attributes
  - geofencing
  - remote message delegation
  - optional runtime permission check

## Run

From `example_mapp_only/`:

```bash
flutter pub get
flutter devices
flutter test integration_test/android_smoke_matrix_test.dart -d <device-id>
```

Run optional interactive permission check:

```bash
flutter test integration_test/android_smoke_matrix_test.dart -d <device-id> --dart-define=RUN_INTERACTIVE_PERMISSION_MATRIX=true
```

## Android NDK

`integration_test` requires Android NDK `28.2.13676358`.  
This example app pins it in `android/app/build.gradle`:

```gradle
android {
  ndkVersion = "28.2.13676358"
}
```

## Notes

- Runtime permission check is excluded by default to keep runs non-interactive; enable it with `RUN_INTERACTIVE_PERMISSION_MATRIX=true`.
- Backend-dependent calls accept either success or handled `PlatformException`; `MissingPluginException` is treated as failure.
- Use this as a smoke/regression matrix when comparing branches for breaking changes.
