package com.mapp.flutter.sdk;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.MainThread;

import com.appoxee.internal.logger.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import io.flutter.plugin.common.MethodChannel;

public class EventEmitter {
    private static volatile EventEmitter instance;

    private final List<Event> pendingEvents = new ArrayList<>();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private volatile MethodChannel channel;


    public static EventEmitter getInstance() {
        if (instance == null) {
            synchronized (EventEmitter.class) {
                if (instance == null) {
                    instance = new EventEmitter();
                }
            }
        }
        return instance;
    }

    public EventEmitter attachChannel(MethodChannel channel) {
        this.channel = channel;
        synchronized (pendingEvents) {
            sendPendingEvents();
        }
        return this;
    }

    /**
     * Sends an event to the Dart layer.
     *
     * @param event The event.
     */
    public synchronized void sendEvent(Event event) {
        pendingEvents.add(event);
        sendPendingEvents();
    }

    private void sendPendingEvents() {
        final List<Event> events = new ArrayList<>(pendingEvents);
        for (Event event : events) {
            mainHandler.post(() -> emit(event));
        }
    }

    /**
     * Helper method to emit data.
     *
     * @param event The event.
     * @return {@code true} if the event was emitted, otherwise {@code false}.
     */
    @MainThread
    private boolean emit(Event event) {
        try {
            if (channel != null) {
                if (event != null && event.getBody() != null && !event.getBody().isEmpty()) {
                    channel.invokeMethod(event.getName(), event.getBody());
                }
                pendingEvents.remove(event);
                return true;
            }
            LoggerFactory.getDevLogger().w("CHANNEL IS NULL!!!");
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public synchronized void detachChannel() {
        channel = null;
    }

    public MethodChannel getChannel() {
        return channel;
    }
}
