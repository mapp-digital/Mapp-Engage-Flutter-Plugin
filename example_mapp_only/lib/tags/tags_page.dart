import 'package:flutter/material.dart';
import 'package:mapp_sdk/mapp_sdk.dart';

import '../dialog.dart';

class TagsPage extends StatefulWidget {
  const TagsPage({super.key});

  @override
  State<TagsPage> createState() => _TagsPageState();
}

final tagController = TextEditingController();

void _resetTagController() {
  tagController.text = "";
  tagController.clear();
}

void _getTags() async {
  MappSdk.getTags().then((tags) {
    debugPrint("Retrieved tags: $tags");
    String content = tags.isEmpty ? "- empty list -" : tags.join(", ");
    Dialogs.showGlobalDialog("Current Tags", content);
  });
}

void _removeTag(String? tag) {
  if (tag == null || tag.isEmpty) {
    debugPrint("Tag is empty, not removing tag.");
    Dialogs.showGlobalDialog("Empty", "Tag is empty, cannot remove tag!");
    return;
  }

  MappSdk.removeTag(tag).then((result) {
    debugPrint("Tag remove result: $result");
    Dialogs.showGlobalDialog("Tag Removed", "Tag removed: $tag");
    _resetTagController();
  });
}

void _setTag(String? tag) {
  if (tag == null || tag.isEmpty) {
    debugPrint("Tag is empty, not adding tag.");
    Dialogs.showGlobalDialog("Empty", "Tag is empty, cannot add tag!");
    return;
  }

  MappSdk.addTag(tag).then((result) {
    debugPrint("Tag add result: $result");
    Dialogs.showGlobalDialog("Tag Added", "Tag added: $tag");
    _resetTagController();
  });
}

class _TagsPageState extends State<TagsPage> {
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text("Tags Management"),
      ),
      body: Column(
        children: [
          Card.outlined(
            child: TextField(
              controller: tagController,
              onChanged: (value) => setState(() {
                tagController.text = value;
              }),
              decoration: const InputDecoration(
                contentPadding: EdgeInsets.all(10),
                border: InputBorder.none,
                labelText: "Tag",
                hintText: "Enter tag",
              ),
            ),
          ),
          SizedBox(
            width: double.infinity,
            child: ElevatedButton(
              onPressed: () {
                _setTag(tagController.text);
              },
              child: const Text("Set Tag"),
            ),
          ),
          SizedBox(
            width: double.infinity,
            child: ElevatedButton(
              onPressed: () {
                _removeTag(tagController.text);
              },
              child: const Text("Remove Tag"),
            ),
          ),
          SizedBox(
            width: double.infinity,
            child: ElevatedButton(
              onPressed: () {
                _getTags();
              },
              child: const Text("Get Tags"),
            ),
          ),
        ],
      ),
    );
  }
}
