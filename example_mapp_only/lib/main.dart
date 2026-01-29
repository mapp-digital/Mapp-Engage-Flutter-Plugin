import 'package:flutter/material.dart';

import 'home_page.dart';

final GlobalKey<NavigatorState> navigatorKey = GlobalKey<NavigatorState>();

void main() {
  WidgetsFlutterBinding.ensureInitialized();

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
