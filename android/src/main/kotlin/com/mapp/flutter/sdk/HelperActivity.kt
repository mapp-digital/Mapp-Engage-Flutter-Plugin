package com.mapp.flutter.sdk

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Log

class HelperActivity : Activity() {

    private val thread = HandlerThread("backgroundThread")
    private lateinit var handler: Handler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("HelperActivity", "Action: - ${intent?.action}")
        thread.start()
        handler = Handler(thread.looper)
        emit(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.d("HelperActivity", "Action: - ${intent.action}")
        emit(intent)
    }

    private fun emit(incomingIntent: Intent?) {
        val action = incomingIntent?.action
        val data: Uri? = incomingIntent?.data
        val packageName = packageName
        val namePackage = incomingIntent?.component?.packageName

        if (namePackage == packageName && !action.isNullOrEmpty() && data != null) {
            startMainActivity(incomingIntent, packageName)
            val event = InAppDeepLinkEvent(data, "didReceiveDeepLinkWithIdentifier")
            if (event.body.isNotEmpty()) {
                handler.postDelayed({ EventEmitter.sendEvent(event) }, 1000)
            }
        }

        handler.postDelayed({ finish() }, 2000)
    }

    private fun startMainActivity(intent: Intent, packageName: String) {
        val launchIntent = packageManager.getLaunchIntentForPackage(this.packageName) ?: return
        val launcherComponent = launchIntent.component ?: return
        if (launcherComponent.packageName == packageName) {
            launchIntent.putExtra("action", intent.action)
            launchIntent.data = intent.data
            launchIntent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            startActivity(launchIntent)
        }
    }
}
