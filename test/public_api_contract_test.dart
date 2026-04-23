import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mapp_sdk/helper_classes.dart';
import 'package:mapp_sdk/mapp_sdk.dart';
import 'package:mapp_sdk/method.dart';
import 'package:mapp_sdk/notification_mode.dart';

void main() {
  const MethodChannel channel = MethodChannel('mapp_sdk');

  final List<MethodCall> calls = <MethodCall>[];
  final Map<String, dynamic> responses = <String, dynamic>{};

  TestWidgetsFlutterBinding.ensureInitialized();

  setUp(() {
    calls.clear();
    responses.clear();
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      calls.add(methodCall);
      return responses[methodCall.method];
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

  group('Main Branch Method Constants Contract', () {
    test('method names stay stable', () {
      expect(Method.GET_PLATFORM_VERSION, 'getPlatformVersion');
      expect(Method.IS_READY, 'isReady');
      expect(Method.ENGAGE, 'engage');
      expect(Method.SET_ALIAS, 'setDeviceAlias');
      expect(Method.GET_ALIAS, 'getDeviceAlias');
      expect(Method.GET_DEVICE_INFO, 'getDeviceInfo');
      expect(Method.OPT_IN, 'optIn');
      expect(Method.IS_PUSH_ENABLED, 'isPushEnabled');
      expect(Method.TRIGGER_INAPP, 'triggerInApp');
      expect(Method.FETCH_INBOX_MESSAGES, 'fetchInboxMessage');
      expect(Method.FETCH_INBOX_MESSAGES_WITH_ID, 'fetchInBoxMessageWithMessageId');
      expect(Method.LOGOUT_WITH_OPT_IN, 'logoutWithOptin');
      expect(Method.REMOVE_BADGE_NUMBER, 'removeBadgeNumber');
      expect(Method.INAPP_MARK_AS_READ, 'inAppMarkAsRead');
      expect(Method.INAPP_MARK_AS_UNREAD, 'inAppMarkAsUnread');
      expect(Method.INAPP_MARK_AS_DELETED, 'inAppMarkAsDeleted');
      expect(Method.START_GEOFENCING, 'startGeofencing');
      expect(Method.STOP_GEOFENCING, 'stopGeofencing');
      expect(Method.PERSMISSION_REQUEST_POST_NOTIFICATION, 'requestPermissionPostNotification');
      expect(Method.SET_CUSTOM_ATTRIBUTES, 'setCustomAttributes');
      expect(Method.GET_CUSTOM_ATTRIBUTES, 'getCustomAttributes');
      expect(Method.ADD_TAG, 'addTag');
      expect(Method.GET_TAGS, 'getTags');
      expect(Method.REMOVE_TAG, 'removeTag');
      expect(Method.SET_REMOTE_MESSAGE, 'setRemoteMessage');
    });
  });

  group('Main Branch Flutter API Contract', () {
    test('platformVersion', () async {
      responses[Method.GET_PLATFORM_VERSION] = '42';
      expect(await MappSdk.platformVersion, '42');
      expect(calls.single.method, Method.GET_PLATFORM_VERSION);

      calls.clear();
      responses[Method.GET_PLATFORM_VERSION] = null;
      expect(await MappSdk.platformVersion, 'unknown');
      expect(calls.single.method, Method.GET_PLATFORM_VERSION);
    });

    test('engage channel call shape', () async {
      responses[Method.ENGAGE] = 'OK';

      final result = await MappSdk.engage(
        'sdk-key',
        'google-project-id',
        SERVER.EMC,
        'app-id',
        'tenant-id',
        NotificationMode.backgroundAndForeground,
      );

      expect(result, contains('successfull'));
      expect(calls.single.method, Method.ENGAGE);

      final args = calls.single.arguments as List<dynamic>;
      expect(args[0], 'sdk-key');
      expect(args[1], SERVER.EMC.index);
      expect(args[2], 'app-id');
      expect(args[3], 'tenant-id');
      expect(args[4], anyOf(2, true));
    });

    test('postponeNotificationRequest', () async {
      responses[Method.POSTPONE_NOTIFICATION_REQUEST] = 'OK';

      final result = await MappSdk.postponeNotificationRequest(true);

      expect(result, contains('successfull'));
      expect(calls.single.method, Method.POSTPONE_NOTIFICATION_REQUEST);
      expect(calls.single.arguments, <dynamic>[true]);
    });

    test('showNotificationsOnForeground does not call channel on non-iOS', () async {
      final result = await MappSdk.showNotificationsOnForeground(true);

      expect(result, 'Required only for iOS.');
      expect(calls, isEmpty);
    });

    test('isReady', () async {
      responses[Method.IS_READY] = true;

      expect(await MappSdk.isReady(), isTrue);
      expect(calls.single.method, Method.IS_READY);
    });

    test('setPushEnabled bool/string parsing', () async {
      responses[Method.OPT_IN] = true;
      expect(await MappSdk.setPushEnabled(true), contains('OptIn set to: true'));

      calls.clear();
      responses[Method.OPT_IN] = 'false';
      expect(await MappSdk.setPushEnabled(false), contains('OptIn set to: false'));
      expect(calls.single.method, Method.OPT_IN);
      expect(calls.single.arguments, <dynamic>[false]);
    });

    test('isPushEnabled', () async {
      responses[Method.IS_PUSH_ENABLED] = false;

      expect(await MappSdk.isPushEnabled(), isFalse);
      expect(calls.single.method, Method.IS_PUSH_ENABLED);
    });

    test('alias methods', () async {
      responses[Method.SET_ALIAS] = 'myAlias';
      expect(await MappSdk.setAlias('myAlias'), 'myAlias');
      expect(calls.single.method, Method.SET_ALIAS);
      expect(calls.single.arguments, <dynamic>['myAlias']);

      calls.clear();
      responses[Method.SET_ALIAS] = 'myAlias2';
      expect(await MappSdk.setAliasWithResend('myAlias2', true), 'myAlias2');
      expect(calls.single.method, Method.SET_ALIAS);
      expect(calls.single.arguments, <dynamic>['myAlias2', true]);

      calls.clear();
      responses[Method.GET_ALIAS] = 'aliasFromNative';
      expect(await MappSdk.getAlias(), 'aliasFromNative');
      expect(calls.single.method, Method.GET_ALIAS);
    });

    test('logOut', () async {
      responses[Method.LOGOUT_WITH_OPT_IN] = 'logged out';

      expect(await MappSdk.logOut(true), 'logged out');
      expect(calls.single.method, Method.LOGOUT_WITH_OPT_IN);
      expect(calls.single.arguments, <dynamic>[true]);
    });

    test('getDeviceInfo', () async {
      responses[Method.GET_DEVICE_INFO] = <String, dynamic>{
        'id': 'abc',
        'osVersion': '14',
      };

      expect(await MappSdk.getDeviceInfo(), <String, dynamic>{
        'id': 'abc',
        'osVersion': '14',
      });
      expect(calls.single.method, Method.GET_DEVICE_INFO);
    });

    test('removeBadgeNumber fallback', () async {
      responses[Method.REMOVE_BADGE_NUMBER] = null;

      expect(await MappSdk.removeBadgeNumber(), 'OK');
      expect(calls.single.method, Method.REMOVE_BADGE_NUMBER);
    });

    test('triggerInApp', () async {
      responses[Method.TRIGGER_INAPP] = 'triggered';

      expect(await MappSdk.triggerInApp('event-x'), 'triggered');
      expect(calls.single.method, Method.TRIGGER_INAPP);
      expect(calls.single.arguments, <dynamic>['event-x']);
    });

    test('inbox methods contract', () async {
      responses[Method.FETCH_INBOX_MESSAGES] = '[{"templateId":1}]';

      expect(await MappSdk.fetchInboxMessage(), '[{"templateId":1}]');
      expect(calls.single.method, Method.FETCH_INBOX_MESSAGES);
      expect(calls.single.arguments, isNull);

      calls.clear();
      responses[Method.FETCH_INBOX_MESSAGES_WITH_ID] = '{"templateId":7}';
      expect(await MappSdk.fetchInBoxMessageWithMessageId(7), '{"templateId":7}');
      expect(calls.single.method, Method.FETCH_INBOX_MESSAGES_WITH_ID);
      expect(calls.single.arguments, <dynamic>[7]);
    });

    test('inbox mark methods contract', () async {
      responses[Method.INAPP_MARK_AS_READ] = true;
      responses[Method.INAPP_MARK_AS_UNREAD] = true;
      responses[Method.INAPP_MARK_AS_DELETED] = false;

      expect(await MappSdk.inAppMarkAsRead('1', 'e1'), isTrue);
      expect(await MappSdk.inAppMarkAsUnread('1', 'e1'), isTrue);
      expect(await MappSdk.inAppMarkAsDeleted('1', 'e1'), isFalse);

      expect(calls[0].method, Method.INAPP_MARK_AS_READ);
      expect(calls[0].arguments, <dynamic>['1', 'e1']);
      expect(calls[1].method, Method.INAPP_MARK_AS_UNREAD);
      expect(calls[1].arguments, <dynamic>['1', 'e1']);
      expect(calls[2].method, Method.INAPP_MARK_AS_DELETED);
      expect(calls[2].arguments, <dynamic>['1', 'e1']);
    });

    test('geofencing methods contract', () async {
      responses[Method.START_GEOFENCING] = 'started';
      responses[Method.STOP_GEOFENCING] = 'stopped';

      expect(await MappSdk.startGeoFencing(), 'started');
      expect(await MappSdk.stopGeoFencing(), 'stopped');

      expect(calls[0].method, Method.START_GEOFENCING);
      expect(calls[1].method, Method.STOP_GEOFENCING);
    });

    test('permission request delegates on non-iOS', () async {
      responses[Method.PERSMISSION_REQUEST_POST_NOTIFICATION] = true;

      expect(await MappSdk.requestPermissionPostNotifications(), isTrue);
      expect(calls.single.method, Method.PERSMISSION_REQUEST_POST_NOTIFICATION);
    });

    test('custom attributes contract', () async {
      responses[Method.SET_CUSTOM_ATTRIBUTES] = true;
      expect(await MappSdk.setCustomAttributes(<String, dynamic>{'k': 'v'}), isTrue);
      expect(calls.single.method, Method.SET_CUSTOM_ATTRIBUTES);
      expect(calls.single.arguments, <dynamic>[
        <String, dynamic>{'k': 'v'}
      ]);

      calls.clear();
      expect(await MappSdk.getCustomAttributes(null), <String, String>{});
      expect(await MappSdk.getCustomAttributes(<String>[]), <String, String>{});
      expect(calls, isEmpty);

      responses[Method.GET_CUSTOM_ATTRIBUTES] = <String, String>{
        'first_name': 'Alice',
      };
      expect(await MappSdk.getCustomAttributes(<String>['first_name']), <String, String>{
        'first_name': 'Alice',
      });
      expect(calls.single.method, Method.GET_CUSTOM_ATTRIBUTES);
      expect(calls.single.arguments, <dynamic>[
        <String>['first_name']
      ]);
    });

    test('tags contract', () async {
      responses[Method.ADD_TAG] = true;
      responses[Method.GET_TAGS] = <dynamic>['tag1', 'tag2'];
      responses[Method.REMOVE_TAG] = true;

      expect(await MappSdk.addTag('tag1'), isTrue);
      expect(await MappSdk.getTags(), <String>['tag1', 'tag2']);
      expect(await MappSdk.removeTag('tag1'), isTrue);

      expect(calls[0].method, Method.ADD_TAG);
      expect(calls[0].arguments, <dynamic>['tag1']);
      expect(calls[1].method, Method.GET_TAGS);
      expect(calls[2].method, Method.REMOVE_TAG);
      expect(calls[2].arguments, <dynamic>['tag1']);
    });
  });
}
