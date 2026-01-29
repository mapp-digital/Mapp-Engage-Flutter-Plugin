import 'package:mapp_sdk/helper_classes.dart';

class EngageConfig{
  final String sdkKey;
  final String appID;
  final String tenantID;
  final SERVER server;

  EngageConfig({required this.sdkKey, required this.appID, required this.tenantID, required this.server});
}