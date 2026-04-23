import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mapp_sdk/helper_classes.dart';
import 'package:mapp_sdk/mapp_sdk.dart';
import 'package:mapp_sdk/method.dart';
import 'package:mapp_sdk/notification_mode.dart';

void main() {
  const MethodChannel channel = MethodChannel('mapp_sdk');
  final List<MethodCall> methodCalls = <MethodCall>[];
  final Map<String, dynamic> stubResponses = <String, dynamic>{};

  TestWidgetsFlutterBinding.ensureInitialized();

  setUp(() {
    methodCalls.clear();
    stubResponses.clear();
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      methodCalls.add(methodCall);
      return stubResponses[methodCall.method];
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

  test('platformVersion returns channel value', () async {
    stubResponses[Method.GET_PLATFORM_VERSION] = '42';

    expect(await MappSdk.platformVersion, '42');
    expect(methodCalls.single.method, Method.GET_PLATFORM_VERSION);
  });

  test('platformVersion falls back to unknown when null', () async {
    stubResponses[Method.GET_PLATFORM_VERSION] = null;

    expect(await MappSdk.platformVersion, 'unknown');
    expect(methodCalls.single.method, Method.GET_PLATFORM_VERSION);
  });

  test('engage sends expected method and args', () async {
    stubResponses[Method.ENGAGE] = 'OK';

    final result = await MappSdk.engage(
      'sdk-key',
      'google-project-id',
      SERVER.EMC,
      'app-id',
      'tenant-id',
      NotificationMode.backgroundAndForeground,
    );

    expect(result, contains('successfull'));
    expect(methodCalls.single.method, Method.ENGAGE);
    expect(methodCalls.single.arguments, isA<List<dynamic>>());
    final args = methodCalls.single.arguments as List<dynamic>;
    expect(args[0], 'sdk-key');
    expect(args[1], SERVER.EMC.index);
    expect(args[2], 'app-id');
    expect(args[3], 'tenant-id');
    expect(args[4], anyOf(2, true));
  });

  test('fetchInboxMessage uses legacy fetch-all channel method', () async {
    stubResponses[Method.FETCH_INBOX_MESSAGES] = '[{"templateId":1}]';

    final result = await MappSdk.fetchInboxMessage();

    expect(result, '[{"templateId":1}]');
    expect(methodCalls.single.method, Method.FETCH_INBOX_MESSAGES);
    expect(methodCalls.single.arguments, isNull);
  });

  test('fetchInBoxMessageWithMessageId sends message id', () async {
    stubResponses[Method.FETCH_INBOX_MESSAGES_WITH_ID] = '{"templateId":7}';

    final result = await MappSdk.fetchInBoxMessageWithMessageId(7);

    expect(result, '{"templateId":7}');
    expect(methodCalls.single.method, Method.FETCH_INBOX_MESSAGES_WITH_ID);
    expect(methodCalls.single.arguments, <dynamic>[7]);
  });

  test('setPushEnabled parses bool and string bool responses', () async {
    stubResponses[Method.OPT_IN] = true;
    final resultBool = await MappSdk.setPushEnabled(true);
    expect(resultBool, contains('OptIn set to: true'));

    stubResponses[Method.OPT_IN] = 'false';
    final resultString = await MappSdk.setPushEnabled(false);
    expect(resultString, contains('OptIn set to: false'));
    expect(methodCalls.length, 2);
    expect(methodCalls[0].method, Method.OPT_IN);
    expect(methodCalls[1].method, Method.OPT_IN);
  });

  test('getCustomAttributes returns empty map without channel call', () async {
    final resultNull = await MappSdk.getCustomAttributes(null);
    final resultEmpty = await MappSdk.getCustomAttributes(<String>[]);

    expect(resultNull, <String, String>{});
    expect(resultEmpty, <String, String>{});
    expect(methodCalls, isEmpty);
  });

  test('getCustomAttributes maps channel response', () async {
    stubResponses[Method.GET_CUSTOM_ATTRIBUTES] = <String, String>{
      'first_name': 'Alice',
      'status': 'active',
    };

    final result = await MappSdk.getCustomAttributes(<String>['first_name']);

    expect(result, <String, String>{
      'first_name': 'Alice',
      'status': 'active',
    });
    expect(methodCalls.single.method, Method.GET_CUSTOM_ATTRIBUTES);
    expect(methodCalls.single.arguments, <dynamic>[
      <String>['first_name']
    ]);
  });

  test('tags methods keep contract', () async {
    stubResponses[Method.ADD_TAG] = true;
    stubResponses[Method.GET_TAGS] = <dynamic>['tag1', 'tag2'];
    stubResponses[Method.REMOVE_TAG] = true;

    expect(await MappSdk.addTag('tag1'), isTrue);
    expect(await MappSdk.getTags(), <String>['tag1', 'tag2']);
    expect(await MappSdk.removeTag('tag1'), isTrue);

    expect(methodCalls.map((e) => e.method).toList(), <String>[
      Method.ADD_TAG,
      Method.GET_TAGS,
      Method.REMOVE_TAG,
    ]);
  });

  test('requestPermissionPostNotifications delegates on non-iOS', () async {
    stubResponses[Method.PERSMISSION_REQUEST_POST_NOTIFICATION] = true;

    final result = await MappSdk.requestPermissionPostNotifications();

    expect(result, isTrue);
    expect(methodCalls.single.method, Method.PERSMISSION_REQUEST_POST_NOTIFICATION);
  });
}
