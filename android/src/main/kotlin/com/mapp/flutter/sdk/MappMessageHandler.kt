package com.mapp.flutter.sdk

import android.content.Context
import com.appoxee.Appoxee
import com.appoxee.shared.MappCallback
import com.appoxee.shared.MappResult
import com.google.firebase.messaging.RemoteMessage

object MappMessageHandler {

    fun canHandle(remoteMessage: RemoteMessage?): Boolean {
        val message = remoteMessage ?: return false
        return message.data.containsKey("p")
    }

    fun handle(remoteMessage: RemoteMessage?, context: Context?) {
        if (remoteMessage == null || context == null) {
            return
        }

        ensureSdkInitialization(context)

        runCatching {
            val sdk = Appoxee.instance()
            if (sdk.isPushMessageFromMapp(remoteMessage)) {
                sdk.handlePushMessage(remoteMessage)
            }
        }
    }

    fun onNewToken(token: String?, context: Context?) {
        if (token.isNullOrBlank() || context == null) {
            return
        }

        ensureSdkInitialization(context)
        runCatching {
            Appoxee.instance().updateFirebaseToken(token).enqueue(object : MappCallback<Boolean> {
                override fun onResult(result: MappResult<Boolean>) = Unit
            })
        }
    }

    fun ensureSdkInitialization(context: Context) {
        runCatching {
            val sdk = Appoxee.instance()
            if (sdk.isReady()) {
                return
            }

            val options = MappSdkPlugin.cachedOptions ?: return
            Appoxee.engage(context.applicationContext, options)
        }
    }
}
