import 'dart:async';
import 'dart:io';

import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:integration_test/integration_test.dart';
import 'package:mapp_sdk/helper_classes.dart';
import 'package:mapp_sdk/mapp_sdk.dart';
import 'package:mapp_sdk/notification_mode.dart';
import 'package:mapp_sdk_example/engage_config.dart';

final androidConfig = EngageConfig(
  sdkKey: '183408d0cd3632.83592719',
  appID: '206974',
  tenantID: '5963',
  server: SERVER.L3,
);

const bool _runInteractivePermissionMatrix = bool.fromEnvironment(
    'RUN_INTERACTIVE_PERMISSION_MATRIX',
    defaultValue: false);

class _Outcome {
  const _Outcome.success(this.value)
      : error = null,
        missingPlugin = null,
        timedOut = false;

  const _Outcome.platformError(this.error)
      : value = null,
        missingPlugin = null,
        timedOut = false;

  const _Outcome.missingPlugin(this.missingPlugin)
      : value = null,
        error = null,
        timedOut = false;

  const _Outcome.timeout()
      : value = null,
        error = null,
        missingPlugin = null,
        timedOut = true;

  final dynamic value;
  final PlatformException? error;
  final MissingPluginException? missingPlugin;
  final bool timedOut;

  bool get isSuccess => !timedOut && error == null;
  bool get isPlatformError => !timedOut && error != null;
  bool get isMissingPlugin => !timedOut && missingPlugin != null;
}

class _SmokeCase {
  const _SmokeCase({
    required this.name,
    required this.action,
    this.allowPlatformError = true,
    this.allowMissingPlugin = false,
    this.verifySuccess,
  });

  final String name;
  final Future<dynamic> Function() action;
  final bool allowPlatformError;
  final bool allowMissingPlugin;
  final void Function(dynamic value)? verifySuccess;
}

Future<_Outcome> _run(Future<dynamic> Function() action) async {
  try {
    final result = await action().timeout(const Duration(seconds: 20));
    return _Outcome.success(result);
  } on MissingPluginException catch (e) {
    return _Outcome.missingPlugin(e);
  } on PlatformException catch (e) {
    return _Outcome.platformError(e);
  } on TimeoutException {
    return const _Outcome.timeout();
  }
}

Future<void> _runMatrix(List<_SmokeCase> cases) async {
  for (final smokeCase in cases) {
    final outcome = await _run(smokeCase.action);

    expect(
      outcome.timedOut,
      isFalse,
      reason: '${smokeCase.name} timed out',
    );

    if (outcome.isSuccess) {
      if (smokeCase.verifySuccess != null) {
        smokeCase.verifySuccess!(outcome.value);
      }
      continue;
    }

    if (outcome.isPlatformError) {
      expect(
        smokeCase.allowPlatformError,
        isTrue,
        reason: '${smokeCase.name} failed with unexpected PlatformException: '
            '${outcome.error?.code} ${outcome.error?.message}',
      );
      continue;
    }

    if (outcome.isMissingPlugin) {
      expect(
        smokeCase.allowMissingPlugin,
        isTrue,
        reason:
            '${smokeCase.name} failed with unexpected MissingPluginException: '
            '${outcome.missingPlugin}',
      );
      continue;
    }

    fail('${smokeCase.name} produced unexpected outcome');
  }
}

void main() {
  IntegrationTestWidgetsFlutterBinding.ensureInitialized();

  group('Android Integration Smoke Matrix', () {
    testWidgets('full non-interactive plugin API matrix', (_) async {
      if (!Platform.isAndroid) return;

      final engageResult = await MappSdk.engage(
        androidConfig.sdkKey,
        '',
        androidConfig.server,
        androidConfig.appID,
        androidConfig.tenantID,
        NotificationMode.backgroundOnly,
      );
      expect(engageResult, isNotNull);

      final uniqueSuffix = DateTime.now().millisecondsSinceEpoch;
      final alias = 'smoke_alias_$uniqueSuffix';
      final tag = 'smoke_tag_$uniqueSuffix';

      final cases = <_SmokeCase>[
        _SmokeCase(
          name: 'platformVersion',
          allowPlatformError: false,
          action: () => MappSdk.platformVersion,
          verifySuccess: (value) {
            expect(value, isA<String>());
            expect((value as String).isNotEmpty, isTrue);
          },
        ),
        _SmokeCase(
          name: 'showNotificationsOnForeground (android no-op)',
          allowPlatformError: false,
          action: () => MappSdk.showNotificationsOnForeground(true),
          verifySuccess: (value) {
            expect(value, 'Required only for iOS.');
          },
        ),
        _SmokeCase(
          name: 'postponeNotificationRequest',
          allowMissingPlugin: true,
          action: () => MappSdk.postponeNotificationRequest(false),
        ),
        _SmokeCase(
          name: 'isReady',
          allowPlatformError: false,
          action: () => MappSdk.isReady(),
          verifySuccess: (value) => expect(value, isA<bool>()),
        ),
        _SmokeCase(
          name: 'setPushEnabled(true)',
          action: () => MappSdk.setPushEnabled(true),
        ),
        _SmokeCase(
          name: 'setPushEnabled(false)',
          action: () => MappSdk.setPushEnabled(false),
        ),
        _SmokeCase(
          name: 'isPushEnabled',
          action: () => MappSdk.isPushEnabled(),
        ),
        _SmokeCase(
          name: 'setAlias',
          action: () => MappSdk.setAlias(alias),
        ),
        _SmokeCase(
          name: 'setAliasWithResend',
          action: () => MappSdk.setAliasWithResend(alias, true),
        ),
        _SmokeCase(
          name: 'getAlias',
          action: () => MappSdk.getAlias(),
        ),
        _SmokeCase(
          name: 'logOut(true)',
          action: () => MappSdk.logOut(true),
        ),
        _SmokeCase(
          name: 'getDeviceInfo',
          action: () => MappSdk.getDeviceInfo(),
        ),
        _SmokeCase(
          name: 'removeBadgeNumber',
          allowPlatformError: false,
          action: () => MappSdk.removeBadgeNumber(),
          verifySuccess: (value) => expect(value, isA<String>()),
        ),
        _SmokeCase(
          name: 'triggerInApp',
          action: () => MappSdk.triggerInApp('app_open'),
        ),
        _SmokeCase(
          name: 'fetchInboxMessage (all)',
          action: () => MappSdk.fetchInboxMessage(),
        ),
        _SmokeCase(
          name: 'fetchInBoxMessageWithMessageId',
          action: () => MappSdk.fetchInBoxMessageWithMessageId(1),
        ),
        _SmokeCase(
          name: 'inAppMarkAsRead',
          action: () => MappSdk.inAppMarkAsRead('1', 'event-1'),
        ),
        _SmokeCase(
          name: 'inAppMarkAsUnread',
          action: () => MappSdk.inAppMarkAsUnread('1', 'event-1'),
        ),
        _SmokeCase(
          name: 'inAppMarkAsDeleted',
          action: () => MappSdk.inAppMarkAsDeleted('1', 'event-1'),
        ),
        _SmokeCase(
          name: 'setCustomAttributes',
          action: () => MappSdk.setCustomAttributes(<String, dynamic>{
            'smoke_attr': 'value',
            'smoke_ts': DateTime.now().toIso8601String(),
          }),
        ),
        _SmokeCase(
          name: 'getCustomAttributes (null)',
          allowPlatformError: false,
          action: () => MappSdk.getCustomAttributes(null),
          verifySuccess: (value) => expect(value, isA<Map<String, String>>()),
        ),
        _SmokeCase(
          name: 'getCustomAttributes (empty)',
          allowPlatformError: false,
          action: () => MappSdk.getCustomAttributes(<String>[]),
          verifySuccess: (value) => expect(value, isA<Map<String, String>>()),
        ),
        _SmokeCase(
          name: 'getCustomAttributes (keys)',
          action: () => MappSdk.getCustomAttributes(<String>['smoke_attr']),
        ),
        _SmokeCase(
          name: 'addTag',
          action: () => MappSdk.addTag(tag),
        ),
        _SmokeCase(
          name: 'getTags',
          action: () => MappSdk.getTags(),
        ),
        _SmokeCase(
          name: 'removeTag',
          action: () => MappSdk.removeTag(tag),
        ),
        _SmokeCase(
          name: 'startGeoFencing',
          action: () => MappSdk.startGeoFencing(),
        ),
        _SmokeCase(
          name: 'stopGeoFencing',
          action: () => MappSdk.stopGeoFencing(),
        ),
      ];

      await _runMatrix(cases);

      // Remote message delegation: should never crash for either path.
      await MappSdk.handleRemoteMessage(<String, dynamic>{'foo': 'bar'});
      await MappSdk.handleRemoteMessage(<String, dynamic>{
        'p': '1',
        'title': 'smoke',
      });
    });

    testWidgets('interactive permission matrix (opt-in)', (_) async {
      if (!Platform.isAndroid || !_runInteractivePermissionMatrix) return;

      final permissionOutcome =
          await _run(() => MappSdk.requestPermissionPostNotifications());

      expect(permissionOutcome.timedOut, isFalse,
          reason: 'permission call timed out');

      if (permissionOutcome.isSuccess) {
        expect(permissionOutcome.value, isA<bool>());
      } else {
        expect(permissionOutcome.isPlatformError, isTrue);
      }
    });
  });
}
