package com.mapp.flutter.sdk;

import androidx.annotation.NonNull;

import com.appoxee.Appoxee;
import com.appoxee.push.fcm.MappMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.concurrent.TimeUnit;

public class MappFlutterMessagingService extends MappMessagingService {

    @Override
    public void onCreate() {
        super.onCreate();
        Appoxee.engage(getApplication());
    }

    @Override
    public void onNewToken(String s) {
        provideSdkInitialization();
        super.onNewToken(s);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        provideSdkInitialization();
        super.onMessageReceived(remoteMessage);
    }

    private void provideSdkInitialization() {
        int retries = 0;
        if (!Appoxee.instance().isReady()) {
            while (retries++ < 20 && !Appoxee.instance().isReady()) {
                try {
                    TimeUnit.MILLISECONDS.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
