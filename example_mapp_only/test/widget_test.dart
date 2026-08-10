import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mapp_sdk/method.dart';
import 'package:mapp_sdk_example/main.dart';

void main() {
  const channel = MethodChannel('mapp_sdk');

  setUp(() {
    channel.setMockMethodCallHandler((call) async {
      if (call.method == Method.PERSMISSION_REQUEST_POST_NOTIFICATION) {
        return true;
      }
      return null;
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

  testWidgets('Verify demo home screen', (WidgetTester tester) async {
    await tester.pumpWidget(const MyApp());
    await tester.pumpAndSettle();

    expect(find.text('Mapp SDK Demo'), findsOneWidget);
    expect(find.text('Alias Setup'), findsOneWidget);
  });
}
