package com.mapp.flutter.sdk

import android.content.Context
import android.content.Intent
import android.util.Log
import com.appoxee.shared.LocalPushBroadcast
import com.appoxee.shared.MappPush
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.dart.DartExecutor
import io.flutter.plugin.common.MethodChannel

class PushBroadcastReceiver : LocalPushBroadcast() {

    private var appContext: Context? = null

    override fun onReceive(context: Context?, intent: Intent?) {
        appContext = context?.applicationContext
        super.onReceive(context, intent)
    }

    override fun onReceived(push: MappPush) {
        ensureBackgroundChannel(appContext)
        EventEmitter.sendEvent(PushNotificationEvent(push, "handledRemoteNotification"))
    }

    override fun onOpened(push: MappPush) {
        ensureBackgroundChannel(appContext)
        EventEmitter.sendEvent(PushNotificationEvent(push, "handledPushOpen"))
    }

    override fun onSilent(push: MappPush) {
        ensureBackgroundChannel(appContext)
        EventEmitter.sendEvent(PushNotificationEvent(push, "handledPushSilent"))
    }

    override fun onDismissed(push: MappPush) {
        ensureBackgroundChannel(appContext)
        EventEmitter.sendEvent(PushNotificationEvent(push, "handledPushDismiss"))
    }

    override fun onButtonClick(push: MappPush) {
        ensureBackgroundChannel(appContext)
        EventEmitter.sendEvent(PushNotificationEvent(push, "handledPushOpen"))
    }

    override fun onRichPush(push: MappPush) {
        ensureBackgroundChannel(appContext)
        EventEmitter.sendEvent(PushNotificationEvent(push, "handledRichContent"))
    }

    private fun ensureBackgroundChannel(context: Context?) {
        if (EventEmitter.getChannel() != null || context == null) {
            return
        }

        runCatching {
            val flutterEngine = FlutterEngine(context, null)
            val executor = flutterEngine.dartExecutor
            val methodChannel = MethodChannel(executor.binaryMessenger, MappSdkPlugin.MAPP_CHANNEL_NAME)
            methodChannel.setMethodCallHandler { _, _ -> }
            executor.executeDartEntrypoint(DartExecutor.DartEntrypoint.createDefault())
            EventEmitter.attachChannel(methodChannel)
        }.onFailure {
            Log.e("PushBroadcastReceiver", it.message, it)
        }
    }
}
