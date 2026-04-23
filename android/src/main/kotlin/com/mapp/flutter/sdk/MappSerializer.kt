package com.mapp.flutter.sdk

import android.os.Build
import com.appoxee.internal.model.response.DevicePayload
import com.appoxee.internal.model.response.inbox.InboxMessage
import com.appoxee.shared.MappPush
import org.json.JSONObject

object MappSerializer {

    fun devicePayloadToMap(deviceInfo: DevicePayload?): Map<String, Any?> {
        return linkedMapOf(
            "id" to deviceInfo?.udidHashed,
            "appVersion" to null,
            "sdkVersion" to null,
            "locale" to null,
            "timezone" to null,
            "deviceModel" to Build.MODEL,
            "manufacturer" to Build.MANUFACTURER,
            "osVersion" to Build.VERSION.RELEASE,
            "resolution" to null,
            "density" to null,
        )
    }

    fun mappPushToMap(data: MappPush, type: String): Map<String, Any?> {
        val map = linkedMapOf<String, Any?>(
            "id" to data.id,
            "title" to data.title,
            "bigText" to data.content,
            "sound" to data.extraFields["sound"],
            "pushNotificationEventType" to type,
            "actionUri" to data.actionUri?.toString(),
            "collapseKey" to data.extraFields["collapse_key"],
            "badgeNumber" to data.extraFields["badge"],
            "silentType" to data.silentType,
            "silentData" to data.silentData,
            "category" to data.category,
        )
        data.extraFields.forEach { (key, value) -> map[key] = value }
        return map
    }

    fun messageToJson(msg: InboxMessage): JSONObject {
        val json = JSONObject()
        runCatching {
            json.put("templateId", msg.templateId)
            json.put("title", msg.content)
            json.put("eventId", msg.eventId)
            msg.expireDate?.let { json.put("expirationDate", it.toString()) }
            msg.iconUrl?.let { json.put("iconURl", it) }
            msg.status?.let { json.put("status", it.toString()) }
            msg.subject?.let { json.put("subject", it) }
            msg.summary?.let { json.put("summary", it) }
            msg.extras.forEach { (key, value) -> json.put(key, value) }
        }
        return json
    }
}
