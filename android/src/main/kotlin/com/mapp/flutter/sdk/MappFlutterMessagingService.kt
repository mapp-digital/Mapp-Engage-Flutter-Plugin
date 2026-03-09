package com.mapp.flutter.sdk

import com.appoxee.shared.MappMessagingService
import com.google.firebase.messaging.RemoteMessage

class MappFlutterMessagingService : MappMessagingService() {

    override fun onCreate() {
        super.onCreate()
        MappMessageHandler.ensureSdkInitialization(applicationContext)
    }

    override fun onNewToken(token: String) {
        MappMessageHandler.ensureSdkInitialization(applicationContext)
        super.onNewToken(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        MappMessageHandler.ensureSdkInitialization(applicationContext)
        super.onMessageReceived(remoteMessage)
    }
}
