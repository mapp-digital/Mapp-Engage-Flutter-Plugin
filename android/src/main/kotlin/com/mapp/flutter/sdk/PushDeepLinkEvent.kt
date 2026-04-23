package com.mapp.flutter.sdk

import com.appoxee.shared.MappPush

class PushDeepLinkEvent(
    private val pushData: MappPush,
    private val type: String,
) : Event {

    override val name: String
        get() = type

    override val body: Map<String, Any?>
        get() {
            val deepLink = linkedMapOf<String, Any?>()
            val actionUri = pushData.actionUri?.toString().orEmpty()
            if (actionUri.contains("apx_dpl")) {
                deepLink["url"] = actionUri.replace("apx_dpl", "")
                deepLink["action"] = pushData.id.orEmpty()
                deepLink["event_trigger"] = ""
            }
            return deepLink
        }
}
