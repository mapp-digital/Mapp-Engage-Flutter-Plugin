package com.mapp.flutter.sdk;

import java.util.Map;

public interface Event {

    /**
     * The event name.
     * @return The event name.
     */
    String getName();

    /**
     * The event body.
     * @return The event body.
     */
    Map<String, Object> getBody();
}