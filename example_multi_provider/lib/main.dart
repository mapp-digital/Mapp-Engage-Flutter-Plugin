import 'dart:io';

import 'package:flutter/material.dart';
import 'package:mapp_sdk/mapp_sdk.dart';
import 'package:mapp_sdk/notification_mode.dart';
import 'package:mapp_sdk_multi_provider_example/engage_config.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _status = 'Initializing...';
  final List<String> _messageLog = [];

  late final EngageConfig _appConfig =
      Platform.isAndroid ? androidConfig : iOSConfig;

  @override
  void initState() {
    super.initState();
    _initMapp();
  }

  Future<void> _initMapp() async {
    // Set up Mapp SDK callbacks
    MappSdk.handledRemoteNotification = (dynamic arguments) {
      setState(() {
        _messageLog.add('Mapp notification received: $arguments');
      });
      debugPrint('Mapp notification received: $arguments');
    };

    MappSdk.handledPushOpen = (dynamic arguments) {
      setState(() {
        _messageLog.add('Mapp notification opened: $arguments');
      });
      debugPrint('Mapp notification opened: $arguments');
    };

    MappSdk.handledPushDismiss = (dynamic arguments) {
      setState(() {
        _messageLog.add('Mapp notification dismissed: $arguments');
      });
      debugPrint('Mapp notification dismissed: $arguments');
    };

    MappSdk.handledPushSilent = (dynamic arguments) {
      setState(() {
        _messageLog.add('Mapp silent push: $arguments');
      });
      debugPrint('Mapp silent push: $arguments');
    };

    try {
      // Initialize Mapp SDK using same logic as the Mapp-only example
      await MappSdk.engage(
        _appConfig.sdkKey,
        "", // googleProjectId not used in current native SDK
        _appConfig.server,
        _appConfig.appID,
        _appConfig.tenantID,
        NotificationMode.backgroundOnly,
      );

      // Request notification permission on Android 13+ (no-op on iOS)
      await MappSdk.requestPermissionPostNotifications();

      setState(() {
        _status = 'Mapp SDK initialized successfully\n'
            'All push messages are handled natively via UnifiedFirebaseMessagingService';
      });
    } catch (e) {
      setState(() {
        _status = 'Mapp SDK initialization failed: $e';
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Mapp Multi Provider (Native)',
      theme: ThemeData(
        primarySwatch: Colors.blue,
        useMaterial3: true,
      ),
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Mapp Multi Provider Test app'),
        ),
        body: Padding(
          padding: const EdgeInsets.all(16.0),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Card(
                child: Padding(
                  padding: const EdgeInsets.all(16.0),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      const Text(
                        'Status:',
                        style: TextStyle(fontWeight: FontWeight.bold),
                      ),
                      const SizedBox(height: 8),
                      Text(_status),
                    ],
                  ),
                ),
              ),
              const SizedBox(height: 16),
              const Text(
                'Mapp Events:',
                style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
              ),
              const SizedBox(height: 8),
              Expanded(
                child: _messageLog.isEmpty
                    ? const Center(
                        child: Text('No Mapp events received yet'),
                      )
                    : ListView.builder(
                        itemCount: _messageLog.length,
                        itemBuilder: (context, index) {
                          return Card(
                            margin: const EdgeInsets.only(bottom: 8),
                            child: Padding(
                              padding: const EdgeInsets.all(12.0),
                              child: Text(
                                _messageLog[index],
                                style: const TextStyle(fontSize: 12),
                              ),
                            ),
                          );
                        },
                      ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
