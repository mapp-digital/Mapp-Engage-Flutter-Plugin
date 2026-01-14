import 'package:firebase_messaging/firebase_messaging.dart';
import 'package:flutter/material.dart';
import 'package:firebase_core/firebase_core.dart';
import 'firebase_options.dart';

import 'home_page.dart';

final GlobalKey<NavigatorState> navigatorKey = GlobalKey<NavigatorState>();

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await Firebase.initializeApp(
    options: DefaultFirebaseOptions.currentPlatform,
  );

  // For apple platforms, make sure the APNS token is available before making any FCM plugin API calls
  final apnsToken = await FirebaseMessaging.instance.getAPNSToken();
  if (apnsToken != null) {
      // APNS token is available, make FCM plugin API requests...
      print("APNS Token: $apnsToken");
  } else {
      // APNS token is not available yet.
      print("APNS Token is not available yet.");
  }

  await FirebaseMessaging.instance.setAutoInitEnabled(true);
  await FirebaseMessaging.instance.subscribeToTopic('sample');

  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      navigatorKey: navigatorKey,
      title: 'Flutter Demo',
      theme: ThemeData(
          primaryColor: const Color(0xFF00BAFF),
          primaryColorDark: const Color(0xFF0592D7),
          cardColor: const Color(0xFF888888)),
      home: const HomePage(),
    );
  }
}
