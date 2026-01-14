package com.mapp.flutter.sdk;

import androidx.annotation.NonNull;

import com.appoxee.push.PushData;

import java.util.HashMap;
import java.util.Map;

public class PushDeepLinkEvent implements Event{
    private final PushData pushData;
    private final String type;

    public PushDeepLinkEvent(PushData pushData, String type) {
        this.pushData = pushData;
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
        Map<String, Object> deepLink = new HashMap<>();
        String actionUri=pushData.actionUri.toString();
        if(actionUri.contains("apx_dpl")){
            deepLink.put("url", actionUri.replaceAll("apx_dpl",""));
            deepLink.put("action",String.valueOf(pushData.id));
            deepLink.put("event_trigger","");
        }
/*        String url = pushData.extraFields.containsKey("apx_dpl") ? pushData.extraFields.get("apx_dpl") : null;
        if(url!=null && !url.isEmpty()) {
            deepLink.put("action", String.valueOf(pushData.id));
            deepLink.put("url", url);
            deepLink.put("event_trigger", "");
        }*/
        return deepLink;
    }
}
