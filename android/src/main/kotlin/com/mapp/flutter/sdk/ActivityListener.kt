package com.mapp.flutter.sdk

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper

class ActivityListener : Activity() {

    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startLauncherActivity(intent)
        handler.postDelayed({ finish() }, 1000)
    }

    private fun startLauncherActivity(intent: Intent?) {
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName) ?: return
        launchIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
        launchIntent.putExtra("intent", intent)
        startActivity(launchIntent)
    }
}
