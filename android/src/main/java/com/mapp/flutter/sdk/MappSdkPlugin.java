package com.mapp.flutter.sdk;

import static com.appoxee.internal.ui.UiUtils.getInAppStatisticsRequestObject;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.TextUtils;
import android.util.ArraySet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;

import com.appoxee.Appoxee;
import com.appoxee.AppoxeeOptions;
import com.appoxee.DeviceInfo;
import com.appoxee.GetCustomAttributesCallback;
import com.appoxee.RequestStatus;
import com.appoxee.internal.geo.geofencing.GeofenceStatus;
import com.appoxee.internal.inapp.model.APXInboxMessage;
import com.appoxee.internal.inapp.model.InAppInboxCallback;
import com.appoxee.internal.inapp.model.InAppStatistics;
import com.appoxee.internal.logger.Logger;
import com.appoxee.internal.logger.LoggerFactory;
import com.appoxee.internal.permission.GeofencePermissions;
import com.appoxee.internal.permission.GeofencingPermissionsCallback;
import com.appoxee.push.NotificationMode;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry;

/**
 * MappSdkPlugin
 */
@SuppressWarnings("Convert2MethodRef")
public class MappSdkPlugin
        implements FlutterPlugin, ActivityAware, MethodCallHandler, PluginRegistry.RequestPermissionsResultListener {

    public static final String ENGINE_ID = "MappSdkPluggin";
    public static final String MAPP_CHANNEL_NAME = "mapp_sdk";

    private static final int POST_NOTIFICATION_PERMISSION_REQUEST_CODE = 190;

    private static final String REQUESTED_PERMISSIONS_KEY = "requested_permissions";

    private static final String SHARED_PREFS_FILE_NAME = "mapp_engage_flutter_plugin_prefs";

    /// The MethodChannel that will the communication between Flutter and native
    /// Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine
    /// and unregister it
    /// when the Flutter Engine is detached from the Activity
    private MethodChannel channel;
    private Application application;
    private Activity activity;
    private Result result;

    private final Logger devLogger = LoggerFactory.getDevLogger();

    private GeofencePermissions geofencePermissions;

    private SharedPreferences sharedPrefs;

    private final GeofencingPermissionsCallback geofencingPermissionsCallback = new GeofencingPermissionsCallback() {
        @Override
        public void onGranted() {
            Appoxee.instance().startGeoFencing(res -> {
                result.success(res);
            });
        }

        @Override
        public void onPermissionsNotGranted(List<String> permissions) {
            if (result != null) {
                result.success(GeofenceStatus.GEOFENCE_LOCATION_PERMISSIONS_NOT_GRANTED);
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle("Permissions not granted");
            builder.setMessage("Following permissions are required: \n" + TextUtils.join(", ", permissions.toArray()) +
                    "\nDo you want to allow requested permissions?");
            builder.setPositiveButton("OK", (dialog, position) -> {
                geofencePermissions.requestPermissions();
            });
            builder.setNegativeButton("Cancel", null);
            builder.create().show();
        }

        @Override
        public void onPermanentlyDeniedPermissions(List<String> permissions) {
            if (result != null) {
                result.success(GeofenceStatus.GEOFENCE_LOCATION_PERMISSIONS_NOT_GRANTED);
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle("Permissions permanently denied");
            builder.setMessage("Following permissions are required: \n" + TextUtils.join(", ", permissions.toArray()) +
                    "\nDo you want to open system settings and manually grant required permissions?");
            builder.setPositiveButton("OK", (dialog, position) -> {
                geofencePermissions.openPermissionSettings();
            });
            builder.setNegativeButton("Cancel", null);
            builder.create().show();
        }
    };

    private final Appoxee.OnInitCompletedListener onInitCompletedListener = new Appoxee.OnInitCompletedListener() {
        @Override
        public void onInitCompleted(boolean successful, Exception failReason) {
            if (successful) {
                DeviceInfo info = Appoxee.instance().getDeviceInfo();
                devLogger.d("DEVICE INFO: " + info);
            }
            if (failReason != null) {
                devLogger.e("engage", failReason.getMessage(), null);
            }
        }
    };

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        devLogger.d("attached to engine");
        application = (Application) flutterPluginBinding.getApplicationContext();
        channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), MAPP_CHANNEL_NAME);
        EventEmitter.getInstance().attachChannel(channel);
        sharedPrefs = application.getApplicationContext().getSharedPreferences(SHARED_PREFS_FILE_NAME,
                Context.MODE_PRIVATE);
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        devLogger.d("detached from engine");
        channel.setMethodCallHandler(null);
        EventEmitter.getInstance().detachChannel();
        application = null;
    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
        List<Object> args = call.arguments();
        devLogger.d("method: " + call.method);
        switch (call.method) {
            case Method.GET_PLATFORM_VERSION:
                result.success("Android " + android.os.Build.VERSION.RELEASE);
                break;
            case Method.ENGAGE:
                engage(args, result);
                break;
            case Method.SET_DEVICE_ALIAS:
                setDeviceAlias(args, result);
                break;
            case Method.GET_DEVICE_ALIAS:
                getDeviceAlias(result);
                break;
            case Method.IS_PUSH_ENABLED:
                isPushEnabled(result);
                break;
            case Method.OPT_IN:
                setPushEnabled(args, result);
                break;
            case Method.TRIGGER_IN_APP:
                triggerInApp(args, result);
                break;
            case Method.IS_READY:
                isReady(result);
                break;
            case Method.GET_DEVICE_INFO:
                getDeviceInfo(result);
                break;
            case Method.FETCH_INBOX_MESSAGE:
                fetchInboxMessage(args, result);
                break;
            case Method.FETCH_INBOX_MESSAGES:
                fetchInboxMessages(result);
                break;
            case Method.GET_FCM_TOKEN:
                getFcmToken(result);
                break;
            case Method.SET_TOKEN:
                setToken(args, result);
                break;
            case Method.START_GEOFENCING:
                startGeoFencing(result);
                break;
            case Method.STOP_GEOFENCING:
                stopGeoFencing(result);
                break;
            case Method.ADD_TAG:
                addTag(args, result);
                break;
            case Method.REMOVE_TAG:
                removeTag(args, result);
                break;
            case Method.FETCH_DEVICE_TAGS:
                getTags(result);
                break;
            case Method.LOGOUT_WITH_OPT_IN:
                logOut(args, result);
                break;
            case Method.IS_DEVICE_REGISTERED:
                isDeviceRegistered(result);
                break;
            case Method.SET_REMOTE_MESSAGE:
                // TODO get remoteMessage and pass data to a native part
                // setRemoteMessage(null,result);
                break;
            case Method.REMOVE_BADGE_NUMBER:
                removeBadgeNumber(result);
                break;
            case Method.INAPP_MARK_AS_READ:
                inAppMarkAsRead(args, result);
                break;
            case Method.INAPP_MARK_AS_UNREAD:
                inAppMarkAsUnRead(args, result);
                break;
            case Method.INAPP_MARK_AS_DELETED:
                inAppMarkAsDeleted(args, result);
                break;
            case Method.PERSMISSION_REQUEST_POST_NOTIFICATION:
                this.result = result;
                requestPermissionPostNotification();
                break;
            case Method.SET_CUSTOM_ATTRIBUTES:
                setCustomAttributes(args, result);
                break;
            case Method.GET_CUSTOM_ATTRIBUTES:
                getCustomAttributes(args, result);
                break;
            case Method.SHOW_NOTIFICATIONS_ON_FOREGROUND:
                showNotificationsOnForeground(args, result);
            default:
                result.notImplemented();
                break;
        }
    }

    private NotificationMode getNotificationMode(List<Object> args) {
        try {
            int notificationMode = args.size() > 4 ? (Integer) args.get(4) : NotificationMode.BACKGROUND_AND_FOREGROUND.ordinal();
            return NotificationMode.values()[notificationMode];
        } catch (Exception e) {
            return NotificationMode.BACKGROUND_AND_FOREGROUND;
        }
    }

    private void engage(List<Object> args, @NonNull Result result) {
        try {
            // [sdkKey, server.index, appID, tenantID, notificationMode]
            AppoxeeOptions options = new AppoxeeOptions();
            options.sdkKey = (String) args.get(0);
            options.server = getServerByIndex((Integer) args.get(1));
            options.appID = (String) args.get(2);
            options.tenantID = (String) args.get(3);
            options.notificationMode = getNotificationMode(args);
            Appoxee.engage(application, options);
            this.result = result;
            Appoxee.instance().addInitListener(onInitCompletedListener);
            Appoxee.instance().setReceiver(PushBroadcastReceiver.class);
            result.success("OK");
            LoggerFactory.getDevLogger().d("ENGAGE OPTIONS", stringify(options));
        } catch (Exception e) {
            result.error(Method.ENGAGE, e.getMessage(), null);
        }
    }

    /** @noinspection StringBufferReplaceableByString*/
    private String stringify(AppoxeeOptions options){
        StringBuilder sb=new StringBuilder();
        sb.append("\n");
        sb.append("SDK Key: ").append(options.sdkKey).append("\n");
        sb.append("Server: ").append(options.server.getValue()).append("\n");
        sb.append("App ID: ").append(options.appID).append("\n");
        sb.append("Tenant ID: ").append(options.tenantID).append("\n");
        sb.append("Notification Mode: ").append(options.notificationMode.name());
        sb.append("\n");
        return sb.toString();
    }

    private void setDeviceAlias(List<Object> args, @NonNull Result result) {
        if (args != null && !args.isEmpty()) {
            String alias = (String) args.get(0);
            boolean resend = args.size() > 1 && (boolean) args.get(1);
            Appoxee.instance().setAlias(alias, resend);
            result.success(alias);
        } else {
            result.error("NO_ARGS_PROVIDED", "No arguments provided!!!", null);
        }
    }

    private void getDeviceAlias(@NonNull Result result) {
        try {
            String alias = Appoxee.instance().getAlias();
            result.success(alias);
        } catch (Exception e) {
            result.error(Method.GET_DEVICE_ALIAS, e.getMessage(), null);
        }
    }

    private void isPushEnabled(@NonNull Result result) {
        try {
            boolean isPushEnabled = Appoxee.instance().isPushEnabled();
            result.success(isPushEnabled);
        } catch (Exception e) {
            result.error(Method.IS_PUSH_ENABLED, e.getMessage(), null);
        }
    }

    private void setPushEnabled(@Nullable List<Object> args, @NonNull Result result) {
        try {
            boolean isEnabled = args != null && !args.isEmpty() && (boolean) args.get(0);
            RequestStatus status = Appoxee.instance().setPushEnabled(isEnabled);
            if (status == RequestStatus.SUCCESS) {
                result.success(true);
            } else {
                result.error(Method.OPT_IN, "Error getting push enabled state", null);
            }
        } catch (Exception e) {
            result.error(Method.OPT_IN, e.getMessage(), null);
        }
    }

    private void triggerInApp(List<Object> args, @NonNull Result result) {
        try {
            String event = args != null && !args.isEmpty() ? (String) args.get(1) : null;
            Appoxee.instance().triggerInApp(activity, event);
            result.success("");
        } catch (Exception e) {
            result.error(Method.IS_READY, e.getMessage(), null);
        }
    }

    private void isReady(@NonNull Result result) {
        try {
            boolean isReady = Appoxee.instance().isReady();
            result.success(isReady);
        } catch (Exception e) {
            result.error(Method.IS_READY, e.getMessage(), null);
        }
    }

    private void getDeviceInfo(@NonNull Result result) {
        try {
            DeviceInfo deviceInfo = Appoxee.instance().getDeviceInfo();
            if (deviceInfo != null) {
                result.success(MappSerializer.deviceInfoToMap(deviceInfo));
            } else {
                result.error(Method.GET_DEVICE_INFO, "Can't get device info!", null);
            }
        } catch (Exception e) {
            result.error(Method.GET_DEVICE_INFO, e.getMessage(), null);
        }
    }

    private void fetchInboxMessage(List<Object> args, @NonNull Result result) {
        try {
            int templateId = args != null && !args.isEmpty() ? (Integer) args.get(0) : -1;
            Appoxee.instance().fetchInboxMessage(templateId);
            handleInAppInboxMessages(result);
        } catch (Exception e) {
            result.error(Method.FETCH_INBOX_MESSAGE, e.getMessage(), null);
        }
    }

    private void fetchInboxMessages(@NonNull Result result) {
        try {
            Appoxee.instance().fetchInboxMessages();
            handleInAppInboxMessages(result);
        } catch (Exception e) {
            result.error(Method.FETCH_INBOX_MESSAGES, e.getMessage(), null);
        }
    }

    private void getFcmToken(@NonNull Result result) {
        try {
            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
                String token = task.getResult();
                result.success(token);
            });
        } catch (Exception e) {
            result.error(Method.GET_FCM_TOKEN, e.getMessage(), null);
        }
    }

    private void setToken(List<Object> args, @NonNull Result result) {
        try {
            String token = (args != null && !args.isEmpty()) ? (String) args.get(0) : null;
            Appoxee.instance().setToken(token);
            result.success(token);
        } catch (Exception e) {
            result.error(Method.SET_TOKEN, e.getMessage(), null);
        }
    }

    private void startGeoFencing(@NonNull Result result) {
        try {
            this.result = result;
            geofencePermissions.requestPermissions();
        } catch (Exception e) {
            result.error(Method.START_GEOFENCING, e.getMessage(), null);
        }
    }

    private void stopGeoFencing(@NonNull Result result) {
        try {
            Appoxee.instance().stopGeoFencing(resString -> result.success(resString));
        } catch (Exception e) {
            result.error(Method.STOP_GEOFENCING, e.getMessage(), null);
        }
    }

    private void addTag(List<Object> args, @NonNull Result result) {
        try {
            String tag = (args != null && !args.isEmpty()) ? (String) args.get(0) : null;
            RequestStatus status = Appoxee.instance().addTag(tag);
            if (status == RequestStatus.SUCCESS) {
                result.success(true);
            } else {
                result.error(Method.ADD_TAG, "Error adding TAG!", null);
            }
        } catch (Exception e) {
            result.error(Method.ADD_TAG, e.getMessage(), null);
        }
    }

    private void removeTag(List<Object> args, @NonNull Result result) {
        try {
            String tag = (args != null && !args.isEmpty()) ? (String) args.get(0) : null;
            RequestStatus status = Appoxee.instance().removeTag(tag);
            if (status == RequestStatus.SUCCESS) {
                result.success(true);
            } else {
                result.error(Method.REMOVE_TAG, "Error removing TAG!", null);
            }
        } catch (Exception e) {
            result.error(Method.REMOVE_TAG, "Error removing TAG!", null);
        }
    }

    private void getTags(@NonNull Result result) {
        try {
            Set<String> tags = Appoxee.instance().getTags();
            result.success(tags);
        } catch (Exception e) {
            result.error(Method.FETCH_DEVICE_TAGS, e.getMessage(), null);
        }
    }

    private void logOut(List<Object> args, @NonNull Result result) {
        try {
            boolean pushEnabled = args != null && !args.isEmpty() && (boolean) args.get(0);
            Appoxee.instance().logOut(pushEnabled);
            result.success("logged out with 'PushEnabled' status: " + pushEnabled);
        } catch (Exception e) {
            result.error(Method.LOGOUT_WITH_OPT_IN, e.getMessage(), null);
        }
    }

    private void isDeviceRegistered(@NonNull Result result) {
        try {
            boolean isRegistered = Appoxee.instance().isDeviceRegistered();
            result.success(isRegistered);
        } catch (Exception e) {
            result.error(Method.IS_DEVICE_REGISTERED, e.getMessage(), null);
        }
    }

    private void removeBadgeNumber(@NonNull Result result) {
        Appoxee.removeBadgeNumber(application.getApplicationContext());
        result.success(true);
    }

    public void inAppMarkAsRead(List<Object> args, @NonNull Result result) {
        try {
            if (args == null || args.size() < 2)
                return;
            int templateId = Integer.parseInt(args.get(0).toString());
            String eventId = args.get(1).toString();

            Appoxee.instance().triggerStatistcs(activity, getInAppStatisticsRequestObject(templateId,
                    eventId,
                    InAppStatistics.INBOX_INBOX_MESSAGE_READ_KEY, null, null, null));
            result.success(true);
        } catch (Exception e) {
            result.error(Method.INAPP_MARK_AS_READ, e.getMessage(), null);
        }
    }

    public void inAppMarkAsUnRead(List<Object> args, @NonNull Result result) {
        try {
            if (args == null || args.size() < 2)
                return;
            int templateId = Integer.parseInt(args.get(0).toString());
            String eventId = args.get(1).toString();
            Appoxee.instance().triggerStatistcs(activity, getInAppStatisticsRequestObject(templateId,
                    eventId,
                    InAppStatistics.INBOX_INBOX_MESSAGE_UNREAD_KEY, null, null, null));
            result.success(true);
        } catch (Exception e) {
            result.error(Method.INAPP_MARK_AS_UNREAD, e.getMessage(), null);
        }
    }

    public void inAppMarkAsDeleted(List<Object> args, @NonNull Result result) {
        try {
            if (args == null || args.size() < 2)
                return;
            int templateId = Integer.parseInt(args.get(0).toString());
            String eventId = args.get(1).toString();
            Appoxee.instance().triggerStatistcs(activity, getInAppStatisticsRequestObject(templateId,
                    eventId,
                    InAppStatistics.INBOX_INBOX_MESSAGE_DELETED_KEY, null, null, null));
            result.success(true);
        } catch (Exception e) {
            result.error(Method.INAPP_MARK_AS_DELETED, e.getMessage(), null);
        }
    }

    private AppoxeeOptions.Server getServerByIndex(int index) {
        if (index < 0 || index > AppoxeeOptions.Server.values().length) {
            throw new IndexOutOfBoundsException(
                    "Server must be one of the following: L3 [0], L3_US [1], EMC [2], EMC_US [3], CROC [4], TEST [5], TEST55 [6] and proper index provided.");
        }
        return AppoxeeOptions.Server.values()[index];
    }

    private void handleInAppInboxMessages(@NonNull Result result) {
        InAppInboxCallback inAppInboxCallback = new InAppInboxCallback();
        inAppInboxCallback.addInAppInboxMessagesReceivedCallback(new InAppInboxCallback.onInAppInboxMessagesReceived() {
            @Override
            public void onInAppInboxMessages(List<APXInboxMessage> messages) {
                JSONArray array = new JSONArray();
                for (APXInboxMessage message : messages) {
                    JSONObject json = MappSerializer.messageToJson(message);
                    array.put(json);
                }
                result.success(array.toString());
            }

            @Override
            public void onInAppInboxMessage(APXInboxMessage message) {
                JSONObject json = MappSerializer.messageToJson(message);
                result.success(json.toString());
            }
        });
    }

    @Override
    public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
        devLogger.d("attached to activity");
        this.activity = binding.getActivity();
        this.channel.setMethodCallHandler(this);
        this.geofencePermissions = new GeofencePermissions((FragmentActivity) activity, geofencingPermissionsCallback);
        binding.addRequestPermissionsResultListener(this);
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {
        devLogger.d("detached from activity for config changes: "
                + (activity != null ? activity.getClass().getName() : "null"));
        this.activity = null;
        this.channel.setMethodCallHandler(null);
        this.geofencePermissions = null;
    }

    @Override
    public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {
        devLogger.d("reattached to activity on config changes: " + binding.getActivity());
        this.activity = binding.getActivity();
        this.channel.setMethodCallHandler(this);
        this.geofencePermissions = new GeofencePermissions((FragmentActivity) activity, geofencingPermissionsCallback);
        binding.addRequestPermissionsResultListener(this);
    }

    @Override
    public void onDetachedFromActivity() {
        devLogger.d("detached from activity: " + (activity != null ? activity.getClass().getName() : "null"));
        this.activity = null;
        this.channel.setMethodCallHandler(null);
        this.geofencePermissions = null;
    }

    public static void handleIntent(Activity activity, Intent intent) {
        if (activity == null || intent == null)
            return;
        Intent richIntent = intent.getParcelableExtra("intent");
        if (richIntent != null && richIntent.getAction().equals(Action.RICH_PUSH)) {
            Appoxee.handleRichPush(activity, richIntent);
        }
    }

    private void requestPermissionPostNotification() {
        // check permission only for Android 13 and higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // check if permission already granted
            if (activity
                    .checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                // if granted, set result to true
                result.success(true);
            } else {
                Set<String> existingPermissions = sharedPrefs.getStringSet(REQUESTED_PERMISSIONS_KEY, new ArraySet<>());
                if (activity.shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) ||
                        !existingPermissions.contains(Manifest.permission.POST_NOTIFICATIONS)) {
                    Set<String> set = new ArraySet<>(existingPermissions);
                    set.add(Manifest.permission.POST_NOTIFICATIONS);
                    sharedPrefs.edit().putStringSet("requested_permissions", set).apply();
                    activity.requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},
                            POST_NOTIFICATION_PERMISSION_REQUEST_CODE);
                } else {
                    result.error("PERMISSION_PERMANENTLY_DENIED",
                            "Permission is permanently denied. Go to system settings and enable Notification permission",
                            null);
                }
            }
        } else {
            // For Android bellow version 13, set notification permission as enabled.
            result.success(true);
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private void resolvePostNotificationPermissionResult() {
        // check if permission is denied
        if (activity.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_DENIED) {
            // if permission is denied we need to check if permission rationale should be
            // shown;
            // it rationale should be shown, we can request permission once again
            if (activity.shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                activity.requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        POST_NOTIFICATION_PERMISSION_REQUEST_CODE);
            } else {
                // when permission was denied and rationale should not be shown, then permission
                // is permanently denied;
                // in that case, app can't request permission and only way to handle this is to
                // open application system settings;
                // in this case, we only send error and leave opening the system settings to the
                // client application
                result.error("PERMISSION_PERMANENTLY_DENIED",
                        "Permission is permanently denied. Go to system settings and enable Notification permission",
                        null);
            }
        }
    }

    @Override
    public boolean onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                              @NonNull int[] grantResults) {
        if (requestCode == POST_NOTIFICATION_PERMISSION_REQUEST_CODE) {
            for (int i = 0; i < permissions.length; i++) {
                String p = permissions[i];
                int grantResult = grantResults[i];
                if (Objects.equals(p, Manifest.permission.POST_NOTIFICATIONS)) {
                    result.success(grantResult == PackageManager.PERMISSION_GRANTED);
                    return true;
                }
            }
            result.success(false);
            return true;
        } else
            return false;
    }

    /**
     * @noinspection unchecked
     */
    private void setCustomAttributes(List<Object> args, @NonNull Result result) {
        try {
            Map<String, Object> attributes = args != null && !args.isEmpty() ? (Map<String, Object>) args.get(0) : Map.of();
            if (attributes != null && !attributes.isEmpty()) {
                Appoxee.instance().setAttributes(attributes);
                result.success(true);
            } else {
                result.error(Method.SET_CUSTOM_ATTRIBUTES, "Empty attributes list!", null);
            }
        } catch (Exception e) {
            result.error(Method.SET_CUSTOM_ATTRIBUTES, e.getMessage(), null);
        }
    }

    /**
     * @noinspection unchecked
     */
    private void getCustomAttributes(List<Object> args, @NonNull Result result) {
        try {
            List<String> keys = args != null && !args.isEmpty() ? (List<String>) args.get(0) : Collections.emptyList();
            if (keys != null && !keys.isEmpty()) {
                Appoxee.instance().getCustomAttributes(keys, new GetCustomAttributesCallback() {
                    @Override
                    public void onSuccess(Map<String, String> customAttributes) {
                        result.success(customAttributes);
                    }

                    @Override
                    public void onError(String errorMessage) {
                        result.error(Method.GET_CUSTOM_ATTRIBUTES, errorMessage, null);
                    }
                });

            } else {
                result.error(Method.GET_CUSTOM_ATTRIBUTES, "Empty attributes keys!", null);
            }
        } catch (Exception e) {
            result.error(Method.GET_CUSTOM_ATTRIBUTES, e.getMessage(), null);
        }
    }

    private void showNotificationsOnForeground(List<Object> args, @NonNull Result result) {
        try {
            boolean showNotificationOnForeground = args != null && !args.isEmpty() && (boolean) args.get(0);

            result.success(true);
        } catch (Exception e) {
            result.error(Method.SHOW_NOTIFICATIONS_ON_FOREGROUND, e.getMessage(), null);
        }
    }
}
