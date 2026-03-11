package com.mapp.flutter.multi_provider

import android.content.Intent
import android.os.Bundle
import com.mapp.flutter.sdk.MappSdkPlugin
import io.flutter.embedding.android.FlutterFragmentActivity

class MainActivity : FlutterFragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MappSdkPlugin.handleIntent(this, intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        MappSdkPlugin.handleIntent(this, intent)
    }
}
