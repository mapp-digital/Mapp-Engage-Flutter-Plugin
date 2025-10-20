// ignore_for_file: constant_identifier_names

class Method {
  static const GET_PLATFORM_VERSION = 'getPlatformVersion';
  static const IS_READY = 'isReady';
  static const ENGAGE = 'engage';
  static const SET_ALIAS = 'setDeviceAlias';
  static const GET_ALIAS = 'getDeviceAlias';
  static const GET_DEVICE_INFO = 'getDeviceInfo';
  static const OPT_IN = 'optIn';
  static const IS_PUSH_ENABLED = 'isPushEnabled';
  static const TRIGGER_INAPP = 'triggerInApp';
  static const FETCH_INBOX_MESSAGES = 'fetchInboxMessage';
  static const FETCH_INBOX_MESSAGES_WITH_ID = 'fetchInBoxMessageWithMessageId';
  static const SHOW_NOTIFICATION_ON_FOREGROUND =
      'showNotificationsOnForeground';
  static const POSTPONE_NOTIFICATION_REQUEST = 'postponeNotificationRequest';
  static const LOGOUT_WITH_OPT_IN = 'logoutWithOptin';
  static const REMOVE_BADGE_NUMBER = 'removeBadgeNumber';

  static const DID_RECEIVE_DEEP_LINK_WITH_IDENTIFIER =
      'didReceiveDeepLinkWithIdentifier';

  static const DID_RECEIVE_INAPP_MESSAGE_WITH_IDENTIFIER =
      'didReceiveInappMessageWithIdentifier';

  static const DID_RECEIVE_CUSTOM_LINK_WITH_IDENTIFIER =
      'didReceiveCustomLinkWithIdentifier';

  static const INAPP_CALL_FAILED_WITH_RESPONSE = 'inAppCallFailedWithResponse';
  static const DID_RECEIVE_INBOX_MESSAGE = 'didReceiveInBoxMessage';
  static const DID_RECEIVE_INBOX_MESSAGES = 'didReceiveInBoxMessages';
  static const HANDLED_REMOTE_NOTIFICATION = 'handledRemoteNotification';
  static const HANDLED_RICH_CONTENT = 'handledRichContent';
  static const HANDLED_PUSH_OPEN = 'handledPushOpen';
  static const HANDLED_PUSH_DISMISS = 'handledPushDismiss';
  static const HANDLED_PUSH_SILENT = 'handledPushSilent';

  static const INAPP_MARK_AS_READ = 'inAppMarkAsRead';
  static const INAPP_MARK_AS_UNREAD = 'inAppMarkAsUnread';
  static const INAPP_MARK_AS_DELETED = 'inAppMarkAsDeleted';

  //geotargeting
  static const START_GEOFENCING = 'startGeofencing';
  static const STOP_GEOFENCING = 'stopGeofencing';
  static const PERSMISSION_REQUEST_POST_NOTIFICATION =
      "requestPermissionPostNotification";

  static const SET_CUSTOM_ATTRIBUTES="setCustomAttributes";
  static const GET_CUSTOM_ATTRIBUTES="getCustomAttributes";
  static const ADD_TAG="addTag";
  static const GET_TAGS="getTags";
  static const REMOVE_TAG="removeTag";
}
