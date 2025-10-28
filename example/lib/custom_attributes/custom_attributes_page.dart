import 'package:flutter/material.dart';
import 'package:mapp_sdk/mapp_sdk.dart';
import 'package:mapp_sdk_example/custom_attributes/custom_attribute.dart';
import 'package:mapp_sdk_example/dialog.dart';

class CustomAttributesPage extends StatefulWidget {
  @override
  _CustomAttributesPageState createState() => _CustomAttributesPageState();
}

class _CustomAttributesPageState extends State<CustomAttributesPage>
    with SingleTickerProviderStateMixin {
  late TabController _tabController;

  List<CustomAttribute> addAttributes = [];
  List<String> attributeKeys = [];

  @override
  void initState() {
    super.initState();
    _tabController = TabController(length: 2, vsync: this);
  }

  /// ---- ADD TAB ----

  void _showAddAttributeDialog() async {
    String key = "";
    String value = "";
    String selectedType = "string"; // default type

    final keyController = TextEditingController();
    final valueController = TextEditingController();

    void resetFields() {
      keyController.clear();
      valueController.clear();
      key = "";
      value = "";
    }

    var customAttribute = await showDialog<CustomAttribute>(
      context: context,
      builder: (context) => StatefulBuilder(
        builder: (context, setState) => AlertDialog(
          title: const Text("Add Attribute"),
          content: SingleChildScrollView(
            child: Column(
              mainAxisSize: MainAxisSize.min,
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const Text("Select Data Type:"),
                RadioGroup<String>(
                  groupValue: selectedType,
                  onChanged: (value) {
                    setState(() {
                      selectedType = value ?? "string";
                      resetFields();
                      print(value);
                    });
                  },
                  child: Column(
                    children: [
                      RadioListTile<String>(
                        title: const Text("String"),
                        value: "string",
                      ),
                      RadioListTile<String>(
                        title: const Text("Boolean"),
                        value: "boolean",
                      ),
                      RadioListTile<String>(
                        title: const Text("Date"),
                        value: "date",
                      ),
                      RadioListTile<String>(
                        title: const Text("Number"),
                        value: "number",
                      ),
                    ],
                  ),
                ),
                const SizedBox(height: 12),
                TextField(
                  controller: keyController,
                  decoration: const InputDecoration(labelText: "Key"),
                  onChanged: (val) => key = val,
                ),
                const SizedBox(height: 12),

                // ---- Dynamic Value Field ----
                if (selectedType == "boolean") ...[
                  InputDecorator(
                    decoration:
                        const InputDecoration(labelText: "Value (BOOLEAN)"),
                    child: DropdownButtonHideUnderline(
                      child: DropdownButton<String>(
                        value: value.isEmpty ? null : value,
                        hint: const Text("Select TRUE or FALSE"),
                        isExpanded: true,
                        items: const [
                          DropdownMenuItem(value: "TRUE", child: Text("TRUE")),
                          DropdownMenuItem(
                              value: "FALSE", child: Text("FALSE")),
                        ],
                        onChanged: (val) {
                          setState(() {
                            value = val!;
                            valueController.text = value;
                          });
                        },
                      ),
                    ),
                  ),
                ] else if (selectedType == "date") ...[
                  TextField(
                    controller: valueController,
                    decoration: const InputDecoration(
                      labelText: "Value (DATE)",
                      suffixIcon: Icon(Icons.calendar_today),
                    ),
                    readOnly: true,
                    onTap: () async {
                      final now = DateTime.now();
                      final picked = await showDatePicker(
                        context: context,
                        initialDate: now,
                        firstDate: DateTime(2000),
                        lastDate: DateTime(2100),
                      );
                      if (picked != null) {
                        final time = await showTimePicker(
                          context: context,
                          initialTime: TimeOfDay.now(),
                        );
                        final fullDateTime = DateTime(
                          picked.year,
                          picked.month,
                          picked.day,
                          time?.hour ?? 0,
                          time?.minute ?? 0,
                        );
                        setState(() {
                          value = fullDateTime.toIso8601String();
                          valueController.text = value;
                        });
                      }
                    },
                  ),
                ] else ...[
                  // Default for string or number
                  TextField(
                    controller: valueController,
                    decoration: InputDecoration(
                      labelText: "Value (${selectedType.toUpperCase()})",
                    ),
                    keyboardType: selectedType == "number"
                        ? TextInputType.number
                        : TextInputType.text,
                    onChanged: (val) => value = val,
                  ),
                ],
              ],
            ),
          ),
          actions: [
            TextButton(
              onPressed: () => Navigator.pop(context),
              child: const Text("Cancel"),
            ),
            ElevatedButton(
              onPressed: () {
                if (key.isNotEmpty && value.isNotEmpty) {
                  Navigator.pop(
                      context,
                      CustomAttribute(
                          key: key, value: value, type: selectedType));
                }
              },
              child: const Text("Add"),
            ),
          ],
        ),
      ),
    );

    if (customAttribute != null) {
      setState(() {
        addAttributes.add(customAttribute);
      });
    }
  }

  void _saveAttributes() {
    final Map<String, dynamic> attributes = addAttributes.fold({}, (map, attr) {
      switch (attr.type) {
        case "boolean":
          map[attr.key] = attr.value.toUpperCase() == "TRUE";
          break;
        case "number":
          map[attr.key] = num.tryParse(attr.value) ?? 0;
          break;
        case "date":
          map[attr.key] = DateTime.tryParse(attr.value)?.toIso8601String() ??
              DateTime.now().toIso8601String();
          break;
        default:
          map[attr.key] = attr.value;
      }
      return map;
    });

    MappSdk.setCustomAttributes(attributes).then((success) {
      final message = success
          ? "Custom attributes saved successfully."
          : "Failed to save custom attributes.";
      Dialogs.showGlobalDialog("Save Attributes", message);
    });
  }

  /// ---- DELETE TAB ----

  void _showAddDeleteDialog() {
    String name = "";

    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text("Add Attribute Name"),
        content: TextField(
          decoration: const InputDecoration(labelText: "Name"),
          onChanged: (val) => name = val,
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text("Cancel"),
          ),
          ElevatedButton(
            onPressed: () {
              if (name.isNotEmpty) {
                setState(() {
                  attributeKeys.add(name);
                });
                Navigator.pop(context);
              }
            },
            child: const Text("Add"),
          ),
        ],
      ),
    );
  }

  void _getAttributes() {
    final List<String> keys =
        attributeKeys.isEmpty ? List.empty() : attributeKeys;
    MappSdk.getCustomAttributes(keys).then((attributes) {
      final String content =
          attributes.entries.map((e) => "${e.key}: ${e.value}").join("\n");
      debugPrint("Retrieved attributes: $attributes");
      Dialogs.showGlobalDialog("Attributes Retrieved", content);
    }).catchError((error) {
      debugPrint("Error retrieving attributes: $error");
      Dialogs.showGlobalDialog("Error", "Failed to retrieve attributes.");
    });
  }

  /// ---- UI ----

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text("Custom Attributes"),
        bottom: TabBar(
          controller: _tabController,
          tabs: const [
            Tab(text: "Add"),
            Tab(text: "View"),
          ],
        ),
      ),
      body: TabBarView(
        controller: _tabController,
        children: [
          // ADD TAB
          Column(
            children: [
              Expanded(
                child: addAttributes.isEmpty
                    ? const Center(child: Text("No attributes added yet."))
                    : ListView.builder(
                        itemCount: addAttributes.length,
                        itemBuilder: (context, index) {
                          final attribute = addAttributes[index];
                          return Card(
                            margin: const EdgeInsets.symmetric(
                                horizontal: 12, vertical: 6),
                            child: ListTile(
                              title: Text(attribute.key),
                              subtitle: Text(attribute.value),
                              trailing: IconButton(
                                icon:
                                    const Icon(Icons.close, color: Colors.red),
                                onPressed: () {
                                  setState(() {
                                    addAttributes.removeAt(index);
                                  });
                                },
                              ),
                            ),
                          );
                        },
                      ),
              ),
              Padding(
                padding: const EdgeInsets.all(16),
                child: Column(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    SizedBox(
                      width: double.infinity,
                      child: ElevatedButton.icon(
                        onPressed: () => {_showAddAttributeDialog()},
                        icon: const Icon(Icons.add),
                        label: const Text("Add Attribute"),
                      ),
                    ),
                    SizedBox(
                      width: double.infinity,
                      child: ElevatedButton.icon(
                        onPressed: _saveAttributes,
                        icon: const Icon(Icons.save),
                        label: const Text("Save"),
                      ),
                    ),
                  ],
                ),
              ),
            ],
          ),

          // DELETE TAB
          Column(
            children: [
              Expanded(
                child: attributeKeys.isEmpty
                    ? const Center(child: Text("No attributes to view."))
                    : ListView.builder(
                        itemCount: attributeKeys.length,
                        itemBuilder: (context, index) {
                          final name = attributeKeys[index];
                          return Card(
                            margin: const EdgeInsets.symmetric(
                                horizontal: 12, vertical: 6),
                            child: ListTile(
                              title: Text(name),
                              trailing: IconButton(
                                icon:
                                    const Icon(Icons.close, color: Colors.red),
                                onPressed: () {
                                  setState(() {
                                    attributeKeys.removeAt(index);
                                  });
                                },
                              ),
                            ),
                          );
                        },
                      ),
              ),
              Padding(
                padding: const EdgeInsets.all(16),
                child: Column(
                  mainAxisSize: MainAxisSize.max,
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    SizedBox(
                      width: double.infinity,
                      child: ElevatedButton.icon(
                        onPressed: _showAddDeleteDialog,
                        icon: const Icon(Icons.add),
                        label: const Text("Add Name"),
                      ),
                    ),
                    SizedBox(
                      width: double.infinity,
                      child: ElevatedButton.icon(
                        onPressed: _getAttributes,
                        icon: const Icon(Icons.get_app),
                        label: const Text(
                          "Get Attributes",
                          style: TextStyle(color: Colors.blue),
                        ),
                        style: ElevatedButton.styleFrom(
                          iconColor: Colors.blue,
                        ),
                      ),
                    ),
                  ],
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }
}
