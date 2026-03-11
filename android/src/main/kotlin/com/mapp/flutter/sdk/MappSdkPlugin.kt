package com.mapp.flutter.sdk

import android.Manifest
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.util.ArraySet
import androidx.annotation.VisibleForTesting
import com.appoxee.Appoxee
import com.appoxee.internal.model.response.DevicePayload
import com.appoxee.internal.model.response.inbox.InboxMessage
import com.appoxee.internal.model.response.inbox.MessageStatus
import com.appoxee.internal.network.Call
import com.appoxee.shared.AppoxeeOptions
import com.appoxee.shared.MappCallback
import com.appoxee.shared.MappResult
import com.appoxee.shared.NotificationMode
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.RemoteMessage
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.PluginRegistry
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import org.json.JSONArray
import kotlin.coroutines.resume

class MappSdkPlugin :
    FlutterPlugin,
    ActivityAware,
    MethodChannel.MethodCallHandler,
    PluginRegistry.RequestPermissionsResultListener {

    companion object {
        const val ENGINE_ID: String = "MappSdkPluggin"
        const val MAPP_CHANNEL_NAME: String = "mapp_sdk"

        private const val POST_NOTIFICATION_PERMISSION_REQUEST_CODE = 190
        private const val REQUESTED_PERMISSIONS_KEY = "requested_permissions"
        private const val SHARED_PREFS_FILE_NAME = "mapp_engage_flutter_plugin_prefs"

        @VisibleForTesting
        @JvmStatic
        internal var cachedOptions: AppoxeeOptions? = null

        @JvmStatic
        fun handleIntent(activity: Activity?, intent: Intent?) {
            if (activity == null || intent == null) {
                return
            }
            // Engage v7 no longer exposes the previous rich-push helper method.
            // We keep this API for binary compatibility with existing host apps.
        }
    }

    private lateinit var channel: MethodChannel
    private lateinit var application: Application
    private var activity: Activity? = null
    private var permissionResult: MethodChannel.Result? = null
    private lateinit var sharedPrefs: SharedPreferences
    private var pluginJob: Job? = null
    private var pluginScope: CoroutineScope? = null

    override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        application = flutterPluginBinding.applicationContext as Application
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, MAPP_CHANNEL_NAME)
        EventEmitter.attachChannel(channel)
        sharedPrefs = application.getSharedPreferences(SHARED_PREFS_FILE_NAME, Context.MODE_PRIVATE)
        pluginJob = SupervisorJob()
        pluginScope = CoroutineScope(pluginJob!! + Dispatchers.Main.immediate)
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        pluginJob?.cancel()
        pluginScope = null
        pluginJob = null
        channel.setMethodCallHandler(null)
        EventEmitter.detachChannel()
    }

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        val args = call.arguments as? List<Any?>
        when (call.method) {
            Method.GET_PLATFORM_VERSION -> result.success("Android ${Build.VERSION.RELEASE}")
            Method.ENGAGE -> engage(args, result)
            Method.SET_DEVICE_ALIAS -> setDeviceAlias(args, result)
            Method.GET_DEVICE_ALIAS -> getDeviceAlias(result)
            Method.IS_PUSH_ENABLED -> isPushEnabled(result)
            Method.OPT_IN -> setPushEnabled(args, result)
            Method.TRIGGER_IN_APP -> triggerInApp(args, result)
            Method.IS_READY -> result.success(runCatching { Appoxee.instance().isReady() }.getOrDefault(false))
            Method.GET_DEVICE_INFO -> getDeviceInfo(result)
            Method.FETCH_INBOX_MESSAGE -> fetchInboxMessage(args, result)
            Method.FETCH_INBOX_MESSAGES -> fetchInboxMessages(result)
            Method.FETCH_INBOX_MESSAGES_WITH_ID -> fetchInboxMessage(args, result)
            Method.GET_FCM_TOKEN -> getFcmToken(result)
            Method.SET_TOKEN -> setToken(args, result)
            Method.START_GEOFENCING -> startGeoFencing(result)
            Method.STOP_GEOFENCING -> stopGeoFencing(result)
            Method.ADD_TAG -> addTag(args, result)
            Method.REMOVE_TAG -> removeTag(args, result)
            Method.GET_TAGS -> getTags(result)
            Method.LOGOUT_WITH_OPT_IN -> logOut(args, result)
            Method.IS_DEVICE_REGISTERED -> isDeviceRegistered(result)
            Method.SET_REMOTE_MESSAGE -> setRemoteMessage(args, result)
            Method.REMOVE_BADGE_NUMBER -> result.success("OK")
            Method.INAPP_MARK_AS_READ -> inAppMark(args, MessageStatus.READ, Method.INAPP_MARK_AS_READ, result)
            Method.INAPP_MARK_AS_UNREAD -> inAppMark(args, MessageStatus.UNREAD, Method.INAPP_MARK_AS_UNREAD, result)
            Method.INAPP_MARK_AS_DELETED -> inAppMark(args, MessageStatus.DELETED, Method.INAPP_MARK_AS_DELETED, result)
            Method.PERSMISSION_REQUEST_POST_NOTIFICATION -> {
                permissionResult = result
                requestPermissionPostNotification()
            }
            Method.SET_CUSTOM_ATTRIBUTES -> setCustomAttributes(args, result)
            Method.GET_CUSTOM_ATTRIBUTES -> getCustomAttributes(args, result)
            else -> result.notImplemented()
        }
    }

    private fun engage(args: List<Any?>?, result: MethodChannel.Result) {
        args?.let { rawArgs ->
            val sdkKey = rawArgs.getOrNull(0) as? String
            val serverIndex = (rawArgs.getOrNull(1) as? Number)?.toInt()
            val appId = rawArgs.getOrNull(2) as? String
            val tenantId = rawArgs.getOrNull(3) as? String

            if (sdkKey.isNullOrEmpty() || serverIndex == null || appId.isNullOrEmpty() || tenantId.isNullOrEmpty()) {
                result.error(Method.ENGAGE, "Invalid engage arguments", null)
                return
            }

            runCatching {
                val options = AppoxeeOptions(getServerByIndex(serverIndex), sdkKey, appId, tenantId).apply {
                    notificationMode = getNotificationMode(rawArgs)
                }
                cachedOptions = options
                Appoxee.engage(application, options)
                Appoxee.instance().setPushBroadcast(PushBroadcastReceiver::class.java)
                result.success("OK")
            }.onFailure {
                result.error(Method.ENGAGE, it.message, null)
            }
            return
        } ?: run {
            result.error(Method.ENGAGE, "Invalid engage arguments", null)
        }
    }

    private fun getNotificationMode(args: List<Any?>?): NotificationMode {
        val modeIndex = (args?.getOrNull(4) as? Number)?.toInt() ?: NotificationMode.BACKGROUND_AND_FOREGROUND.ordinal
        return NotificationMode.entries.getOrElse(modeIndex) { NotificationMode.BACKGROUND_AND_FOREGROUND }
    }

    private fun getServerByIndex(index: Int): AppoxeeOptions.Server {
        return when (index) {
            0 -> AppoxeeOptions.Server.L3
            1 -> AppoxeeOptions.Server.L3_US
            2 -> AppoxeeOptions.Server.EMC
            3 -> AppoxeeOptions.Server.EMC_US
            4 -> AppoxeeOptions.Server.CROC
            5 -> AppoxeeOptions.Server.TEST
            6 -> AppoxeeOptions.Server.TEST_55
            else -> throw IndexOutOfBoundsException("Unsupported server index: $index")
        }
    }

    private fun setDeviceAlias(args: List<Any?>?, result: MethodChannel.Result) {
        args?.let { rawArgs ->
            val alias = rawArgs.getOrNull(0) as? String
            if (alias.isNullOrEmpty()) {
                result.error(Method.SET_DEVICE_ALIAS, "No alias provided", null)
                return
            }
            val resend = rawArgs.getOrNull(1) as? Boolean ?: false
            resolveCall(Method.SET_DEVICE_ALIAS, Appoxee.instance().setAlias(alias, resend), result) { alias }
            return
        } ?: run {
            result.error(Method.SET_DEVICE_ALIAS, "No alias provided", null)
        }
    }

    private fun getDeviceAlias(result: MethodChannel.Result) {
        resolveCall(Method.GET_DEVICE_ALIAS, Appoxee.instance().getAlias(), result) { it }
    }

    private fun isPushEnabled(result: MethodChannel.Result) {
        resolveCall(Method.IS_PUSH_ENABLED, Appoxee.instance().isPushEnabled(), result) { it ?: false }
    }

    private fun setPushEnabled(args: List<Any?>?, result: MethodChannel.Result) {
        val enabled = args?.getOrNull(0) as? Boolean ?: false
        resolveCall(Method.OPT_IN, Appoxee.instance().enablePush(enabled, null), result) { it ?: false }
    }

    private fun triggerInApp(args: List<Any?>?, result: MethodChannel.Result) {
        val hostActivity = activity
        if (hostActivity == null) {
            result.error(Method.TRIGGER_IN_APP, "Activity is not attached", null)
            return
        }
        val event = args?.getOrNull(0) as? String
        resolveCall(Method.TRIGGER_IN_APP, Appoxee.instance().triggerInApp(hostActivity, event.orEmpty()), result) { "" }
    }

    private fun getDeviceInfo(result: MethodChannel.Result) {
        resolveCall<DevicePayload?, Any?>(Method.GET_DEVICE_INFO, Appoxee.instance().getDevice(), result) {
            MappSerializer.devicePayloadToMap(it)
        }
    }

    private fun fetchInboxMessage(args: List<Any?>?, result: MethodChannel.Result) {
        val templateId = (args?.getOrNull(0) as? Number)?.toLong()
        if (templateId == null) {
            // Backward compatibility:
            // Flutter API sends method "fetchInboxMessage" without args to fetch all inbox messages.
            fetchInboxMessages(result)
            return
        }
        resolveCall(Method.FETCH_INBOX_MESSAGE, Appoxee.instance().fetchInboxMessage(templateId), result) {
            it?.let { message -> MappSerializer.messageToJson(message).toString() } ?: "{}"
        }
    }

    private fun fetchInboxMessages(result: MethodChannel.Result) {
        resolveCall(Method.FETCH_INBOX_MESSAGES, Appoxee.instance().fetchInboxMessages(), result) { response ->
            val array = JSONArray()
            response?.messages?.forEach { message -> array.put(MappSerializer.messageToJson(message)) }
            array.toString()
        }
    }

    private fun getFcmToken(result: MethodChannel.Result) {
        runCatching {
            FirebaseMessaging.getInstance().token
                .addOnSuccessListener { token -> result.success(token) }
                .addOnFailureListener { err -> result.error(Method.GET_FCM_TOKEN, err.message, null) }
        }.onFailure {
            result.error(Method.GET_FCM_TOKEN, it.message, null)
        }
    }

    private fun setToken(args: List<Any?>?, result: MethodChannel.Result) {
        args?.let { rawArgs ->
            val token = rawArgs.getOrNull(0) as? String
            if (token.isNullOrBlank()) {
                result.error(Method.SET_TOKEN, "Missing token", null)
                return
            }
            resolveCall(Method.SET_TOKEN, Appoxee.instance().updateFirebaseToken(token), result) { token }
            return
        } ?: run {
            result.error(Method.SET_TOKEN, "Missing token", null)
        }
    }

    private fun startGeoFencing(result: MethodChannel.Result) {
        resolveCall(Method.START_GEOFENCING, Appoxee.instance().startGeofencing(0), result) { it.toString() }
    }

    private fun stopGeoFencing(result: MethodChannel.Result) {
        resolveCall(Method.STOP_GEOFENCING, Appoxee.instance().stopGeofencing(), result) { it.toString() }
    }

    private fun addTag(args: List<Any?>?, result: MethodChannel.Result) {
        args?.let { rawArgs ->
            val tag = rawArgs.getOrNull(0) as? String
            if (tag.isNullOrBlank()) {
                result.error(Method.ADD_TAG, "Missing tag", null)
                return
            }
            resolveCall(Method.ADD_TAG, Appoxee.instance().addTags(setOf(tag)), result) { it ?: false }
            return
        } ?: run {
            result.error(Method.ADD_TAG, "Missing tag", null)
        }
    }

    private fun removeTag(args: List<Any?>?, result: MethodChannel.Result) {
        args?.let { rawArgs ->
            val tag = rawArgs.getOrNull(0) as? String
            if (tag.isNullOrBlank()) {
                result.error(Method.REMOVE_TAG, "Missing tag", null)
                return
            }
            resolveCall(Method.REMOVE_TAG, Appoxee.instance().removeTags(setOf(tag)), result) { it ?: false }
            return
        } ?: run {
            result.error(Method.REMOVE_TAG, "Missing tag", null)
        }
    }

    private fun getTags(result: MethodChannel.Result) {
        resolveCall(Method.GET_TAGS, Appoxee.instance().getTags(), result) { it ?: emptyList() }
    }

    private fun logOut(args: List<Any?>?, result: MethodChannel.Result) {
        val pushEnabled = args?.getOrNull(0) as? Boolean ?: false
        resolveCall(Method.LOGOUT_WITH_OPT_IN, Appoxee.instance().logout(pushEnabled), result) {
            "logged out with 'PushEnabled' status: $pushEnabled"
        }
    }

    private fun isDeviceRegistered(result: MethodChannel.Result) {
        resolveCall(Method.IS_DEVICE_REGISTERED, Appoxee.instance().getDevice(), result) { it != null }
    }

    private fun setRemoteMessage(args: List<Any?>?, result: MethodChannel.Result) {
        args?.let { rawArgs ->
            val payload = rawArgs.getOrNull(0) as? Map<*, *>
            if (payload == null) {
                result.error(Method.SET_REMOTE_MESSAGE, "Missing or invalid arguments for setRemoteMessage", null)
                return
            }

            val data = payload.entries
                .filter { it.key != null && it.value != null }
                .associate { it.key.toString() to it.value.toString() }

            runCatching {
                val remoteMessage = RemoteMessage.Builder("${application.packageName}@fcm")
                    .setData(data)
                    .build()
                MappMessageHandler.handle(remoteMessage, application)
                result.success(null)
            }.onFailure {
                result.error(Method.SET_REMOTE_MESSAGE, it.message, null)
            }
            return
        } ?: run {
            result.error(Method.SET_REMOTE_MESSAGE, "Missing or invalid arguments for setRemoteMessage", null)
        }
    }

    private fun inAppMark(
        args: List<Any?>?,
        status: MessageStatus,
        method: String,
        result: MethodChannel.Result,
    ) {
        args?.let { rawArgs ->
            val templateId = (rawArgs.getOrNull(0) as? Number)?.toLong()
            if (templateId == null) {
                result.error(method, "Missing template id", null)
                return
            }

            Appoxee.instance().fetchInboxMessage(templateId)
                .enqueue(object : MappCallback<InboxMessage?> {
                    override fun onResult(fetchResult: MappResult<InboxMessage?>) {
                        if (!fetchResult.isSuccess() || fetchResult.getData() == null) {
                            result.error(method, fetchResult.getError()?.message ?: "Inbox message not found", null)
                            return
                        }

                        val message = fetchResult.getData() ?: return
                        Appoxee.instance().updateInboxMessageStatus(message, status)
                            .enqueue(object : MappCallback<Boolean> {
                                override fun onResult(updateResult: MappResult<Boolean>) {
                                    if (updateResult.isSuccess()) {
                                        result.success(updateResult.getData() ?: false)
                                    } else {
                                        result.error(
                                            method,
                                            updateResult.getError()?.message ?: "Failed to update inbox status",
                                            null,
                                        )
                                    }
                                }
                            })
                    }
                })
            return
        } ?: run {
            result.error(method, "Missing template id", null)
        }
    }

    private fun setCustomAttributes(args: List<Any?>?, result: MethodChannel.Result) {
        args?.let { rawArgs ->
            val attributes = rawArgs.getOrNull(0) as? Map<*, *>
            if (attributes == null || attributes.isEmpty()) {
                result.error(Method.SET_CUSTOM_ATTRIBUTES, "Empty attributes list!", null)
                return
            }

            val normalized = attributes.entries.associate { entry ->
                entry.key.toString() to entry.value
            }

            resolveCall(Method.SET_CUSTOM_ATTRIBUTES, Appoxee.instance().addCustomAttributes(normalized), result) { it ?: false }
            return
        } ?: run {
            result.error(Method.SET_CUSTOM_ATTRIBUTES, "Empty attributes list!", null)
        }
    }

    private fun getCustomAttributes(args: List<Any?>?, result: MethodChannel.Result) {
        args?.let { rawArgs ->
            val keys = (rawArgs.getOrNull(0) as? List<*>)
                ?.mapNotNull { it?.toString() }
                ?.toSet()
                .orEmpty()

            if (keys.isEmpty()) {
                result.error(Method.GET_CUSTOM_ATTRIBUTES, "Empty attributes keys!", null)
                return
            }

            resolveCall(Method.GET_CUSTOM_ATTRIBUTES, Appoxee.instance().getCustomAttributes(keys), result) { attrs ->
                attrs?.mapValues { (_, value) -> value?.toString().orEmpty() } ?: emptyMap<String, String>()
            }
            return
        } ?: run {
            result.error(Method.GET_CUSTOM_ATTRIBUTES, "Empty attributes keys!", null)
        }
    }

    private fun requestPermissionPostNotification() {
        val currentActivity = activity ?: run {
            permissionResult?.error(Method.PERSMISSION_REQUEST_POST_NOTIFICATION, "Activity is not attached", null)
            return
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            permissionResult?.success(true)
            return
        }

        if (currentActivity.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            permissionResult?.success(true)
            return
        }

        val existingPermissions =
            sharedPrefs.getStringSet(REQUESTED_PERMISSIONS_KEY, ArraySet()) ?: emptySet()

        if (
            currentActivity.shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) ||
            !existingPermissions.contains(Manifest.permission.POST_NOTIFICATIONS)
        ) {
            val updatedSet = ArraySet(existingPermissions)
            updatedSet.add(Manifest.permission.POST_NOTIFICATIONS)
            sharedPrefs.edit().putStringSet(REQUESTED_PERMISSIONS_KEY, updatedSet).apply()
            currentActivity.requestPermissions(
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                POST_NOTIFICATION_PERMISSION_REQUEST_CODE,
            )
        } else {
            permissionResult?.error(
                "PERMISSION_PERMANENTLY_DENIED",
                "Permission is permanently denied. Go to system settings and enable Notification permission",
                null,
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ): Boolean {
        if (requestCode != POST_NOTIFICATION_PERMISSION_REQUEST_CODE) {
            return false
        }

        permissions.forEachIndexed { index, permission ->
            if (permission == Manifest.permission.POST_NOTIFICATIONS) {
                permissionResult?.success(grantResults.getOrNull(index) == PackageManager.PERMISSION_GRANTED)
                return true
            }
        }

        permissionResult?.success(false)
        return true
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        activity = binding.activity
        channel.setMethodCallHandler(this)
        binding.addRequestPermissionsResultListener(this)
    }

    override fun onDetachedFromActivityForConfigChanges() {
        activity = null
        channel.setMethodCallHandler(null)
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        activity = binding.activity
        channel.setMethodCallHandler(this)
        binding.addRequestPermissionsResultListener(this)
    }

    override fun onDetachedFromActivity() {
        activity = null
        channel.setMethodCallHandler(null)
    }

    private fun <T, R> resolveCall(
        method: String,
        call: Call<T>,
        result: MethodChannel.Result,
        mapper: (T?) -> R,
    ) {
        val scope = pluginScope
        if (scope == null) {
            result.error(method, "Plugin is not attached to engine", null)
            return
        }

        scope.launch {
            val response = try {
                call.asSuspend()
            } catch (throwable: Throwable) {
                if (throwable is CancellationException) {
                    return@launch
                }
                result.error(method, throwable.message ?: "Unknown error", null)
                return@launch
            }

            if (response.isSuccess()) {
                result.success(mapper(response.getData()))
            } else {
                result.error(method, response.getError()?.message ?: "Unknown error", null)
            }
        }
    }

    private suspend fun <T> Call<T>.asSuspend(): MappResult<T> = suspendCancellableCoroutine { continuation ->
        enqueue(object : MappCallback<T> {
            override fun onResult(result: MappResult<T>) {
                if (continuation.isActive) {
                    continuation.resume(result)
                }
            }
        })
    }
}
