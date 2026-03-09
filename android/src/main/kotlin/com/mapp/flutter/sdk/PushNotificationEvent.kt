package com.mapp.flutter.sdk

import com.appoxee.shared.MappPush

class PushNotificationEvent(
    private val message: MappPush,
    private val type: String,
) : Event {
    override val name: String
        get() = type

    override val body: Map<String, Any?>
        get() = MappSerializer.mappPushToMap(message, type)
}
