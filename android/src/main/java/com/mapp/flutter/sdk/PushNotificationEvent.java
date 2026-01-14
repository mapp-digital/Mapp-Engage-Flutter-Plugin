package com.mapp.flutter.sdk;

import androidx.annotation.NonNull;

import com.appoxee.push.PushData;

import java.util.Map;

public class PushNotificationEvent implements Event {
    private final PushData message;
    private final String type;

    public PushNotificationEvent(@NonNull PushData message, String type) {
        this.message = message;
        this.type = type;
    }

    @NonNull
    @Override
    public String getName() {
        return type;
    }

    @NonNull
    @Override
    public Map<String, Object> getBody() {
        return MappSerializer.pushDataToMap(message, type);
    }
}