import 'package:flutter/material.dart';
import 'package:mapp_sdk/mapp_sdk.dart';

import '../dialog.dart';

class AliasPage extends StatefulWidget {
  const AliasPage({super.key});

  @override
  State<AliasPage> createState() => _AliasPageState();
}

final aliasController = TextEditingController();

void _resetAliasField() {
  aliasController.clear();
  aliasController.text = "";
}

void _setAliasWithResend(String? alias, bool resend) async {
  if (alias == null || alias.isEmpty) {
    debugPrint("Alias is empty, not setting alias with resend.");
    Dialogs.showGlobalDialog("Empty", "Alias is empty, cannot set alias!");
    return;
  }
  MappSdk.setAliasWithResend(alias, resend).then((result) {
    debugPrint("Alias with resend set result: $result");
    Dialogs.showGlobalDialog("Alias Set", "Alias set to: $result");
    if (alias == result) {
      aliasController.clear();
    }
  });
}

void _setAlias(String? alias) async {
  if (alias == null || alias.isEmpty) {
    debugPrint("Alias is empty, not setting alias with resend.");
    Dialogs.showGlobalDialog("Empty", "Alias is empty, cannot set alias!");
    return;
  }
  MappSdk.setAlias(alias).then((result) {
    debugPrint("Alias set result: $result");
    Dialogs.showGlobalDialog("Alias Set", "Alias set to: $result");
    if (alias == result) {
      aliasController.clear();
    }
  });
}

void _getAlias() async {
  MappSdk.getAlias().then((alias) {
    debugPrint("Retrieved alias: $alias");
    Dialogs.showGlobalDialog("Current Alias", alias);
  });
}

class _AliasPageState extends State<AliasPage> {
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text("Alias Page"),
      ),
      body: Column(
        children: [
          Card.outlined(
            child: TextField(
              controller: aliasController,
              onChanged: (value) => setState(() {
                aliasController.text = value;
              }),
              decoration: const InputDecoration(
                contentPadding: EdgeInsets.all(10),
                border: InputBorder.none,
                labelText: "Alias",
                hintText: "Enter alias",
              ),
            ),
          ),
          SizedBox(
            width: double.infinity,
            child: ElevatedButton(
              onPressed: () {
                _setAlias(aliasController.text);
              },
              child: const Text("Set Alias"),
            ),
          ),
          SizedBox(
            width: double.infinity,
            child: ElevatedButton(
              onPressed: () {
                _setAliasWithResend(aliasController.text, false);
              },
              child: const Text("Set Alias / Resend FALSE"),
            ),
          ),
          SizedBox(
            width: double.infinity,
            child: ElevatedButton(
              onPressed: () {
                _setAliasWithResend(aliasController.text, true);
              },
              child: const Text("Set Alias / Resend TRUE"),
            ),
          ),
          SizedBox(
            width: double.infinity,
            child: ElevatedButton(
              onPressed: () {
                _getAlias();
              },
              child: const Text("Get Alias"),
            ),
          ),
        ],
      ),
    );
  }
}
