package com.mapp.flutter.sdk

import android.net.Uri

class InAppDeepLinkEvent(
    private val message: Uri?,
    private val type: String,
) : Event {

    override val name: String
        get() = "didReceiveDeepLinkWithIdentifier"

    override val body: Map<String, Any?>
        get() {
            val map = linkedMapOf<String, Any?>()
            val msg = message
            if (msg != null) {
                runCatching {
                    map["action"] = msg.getQueryParameter("message_id")
                    map["url"] = msg.getQueryParameter("link")
                    map["event_trigger"] = msg.getQueryParameter("event_trigger")
                }.onFailure {
                    map["url"] = msg.toString()
                }
            } else {
                map["url"] = ""
                map["action"] = type
            }
            return map
        }
}
