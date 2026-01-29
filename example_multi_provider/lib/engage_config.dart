import 'package:mapp_sdk/helper_classes.dart';

class EngageConfig {
  final String sdkKey;
  final String appID;
  final String tenantID;
  final SERVER server;

  const EngageConfig({
    required this.sdkKey,
    required this.appID,
    required this.tenantID,
    required this.server,
  });
}

// Example configuration values (copied from the Mapp-only example).
// Replace these with your own credentials when running the sample.
const EngageConfig androidConfig = EngageConfig(
  sdkKey: "183408d0cd3632.83592719",
  appID: "206974",
  tenantID: "5963",
  server: SERVER.L3,
);

const EngageConfig iOSConfig = EngageConfig(
  sdkKey: "194836e00ab678.39583584",
  appID: "301677",
  tenantID: "33",
  server: SERVER.TEST,
);