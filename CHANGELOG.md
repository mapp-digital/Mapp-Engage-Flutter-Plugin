## 0.0.12
- update setPushEnable method on iOS side to return description
- added method to set multiple custom attributes in a single call: setCustomAttributes(Map<String,dynamic>)
- added method get list of attributes for a requested list of keys: getCustomAttributes(List<String>)
- added method to set alias and trigger resending of custom parameters: setAliasWithResend(String, Boolean)
- added method to set tag: setTag(String)
- added method to get tags: getTags()
- added method to set tag: removeTag(String)
- Android updates:
  - Engage SDK version 6.1.2
  - targetSdkVersion 36
  - Firebase BOM: 34.4.0
  - Appocompat: 1.7.1
  - Migrated gradle apply to declarative plugins block
- iOS updates:
  - Engage SDK version 6.1.0
- code improvements

## 0.0.11
- update method for enabling foreground notification at iOS
- iOS mapp engage sdk: 6.0.10

## 0.0.10
- Updated Dart version (sdk: ">=3.5.0 <4.0.0")
- Updated Flutter version (flutter: ">=3.24.0")
- Android updates:
  - Updated native android libraries
  - Updated Java version to Java 17
  - Updated targetSdk for Android to version 34
  - Updated Mapp Engage Android SDK to version 6.0.25
  - Updated firebase-bom to version 33.5.1
  - Updated Gradle wrapper to version 8.7
  - Updated Android Gradle Plugin to version 8.5.2
- iOS updates:
  - update Mapp Engage library to 6.0.8

## 0.0.9

- Update ios part which now contains new native SDK

## 0.0.8

- Changes to pubspec list so the plugin now can work on flutter apps which are using swift for iOS part.

## 0.0.7

- Added method to request runtime permission for POST_NOTIFICATION from a plugin.

## 0.0.6

- Added support for user matching feature
- Updated native dependencies for Android

## 0.0.5

- Updated native dependencies for Android

## 0.0.4

- This is beta version of flutter plugin which enables you to use all mobile features from Mapp Engage platform. This version contains implementation for both iOS and Android.
