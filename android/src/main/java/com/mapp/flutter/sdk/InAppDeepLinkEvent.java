package com.mapp.flutter.sdk;
import android.net.Uri;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Aleksandar Marinkovic on 2019-05-16.
 * Copyright (c) 2019 MAPP.
 */
public class InAppDeepLinkEvent implements Event {

    private static final String IN_APP_DEEP_LINK_RECEIVED_EVENT = "didReceiveDeepLinkWithIdentifier";//"com.mapp.deep_link_received";

    @Override
    public String getName() {
        return IN_APP_DEEP_LINK_RECEIVED_EVENT;
    }

    @Override
    public Map<String,Object> getBody() {

        Map<String,Object> map = new HashMap<>();
        if (message != null) {
            try {
                String link = message.getQueryParameter("link");
                String messageId = message.getQueryParameter("message_id");
                String event_trigger = message.getQueryParameter("event_trigger");
                map.put("action", messageId);
                map.put("url", link);
                map.put("event_trigger", event_trigger);
            } catch (Exception e) {
                map.put("url", message.toString());
            }
        } else {
            map.put("url", "");
            map.put("action", type);
        }

        return map;
    }

    private final Uri message;
    private final String type;

    public InAppDeepLinkEvent(Uri message, String type) {
        this.message = message;
        this.type = type;
    }
}

