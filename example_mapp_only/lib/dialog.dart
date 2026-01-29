import 'package:flutter/material.dart';
import 'main.dart'; // for the navigatorKey

class Dialogs {
  static void showGlobalDialog(String title, String content) {
    final context = navigatorKey.currentState?.overlay?.context;

    if (context == null) {
      debugPrint("❌ No context available for dialog");
      return;
    }

    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: Text(title),
        content: Text(content),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(context).pop(),
            child: const Text("OK"),
          ),
        ],
      ),
    );
  }
}

