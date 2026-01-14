package com.mapp.flutter.sdk;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;

import androidx.annotation.Nullable;

import com.appoxee.internal.logger.Logger;
import com.appoxee.internal.logger.LoggerFactory;

import java.util.Objects;

public class HelperActivity extends Activity {

    private final static String ACTION_IN_APP_DEEPLINK = "com.appoxee.VIEW_DEEPLINK";

    private final Logger devLogger = LoggerFactory.getDevLogger();
    private final HandlerThread thread = new HandlerThread("backgroundThread");
    private Handler handler;

    @Override
    public final void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        devLogger.d("Action: - " + (getIntent() != null ? getIntent().getAction() : "null"));
        thread.start();
        handler = new Handler(thread.getLooper());
        Intent intent = getIntent();
        emit(intent);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        devLogger.d("Action: - " + (intent != null ? intent.getAction() : "null"));
        emit(intent);
    }

    private void emit(Intent intent) {
        if (intent != null) {
            Uri data = intent.getData();
            String action = intent.getAction();
            ComponentName name = intent.getComponent();
            String packageName = getPackageName();
            Event event = new InAppDeepLinkEvent(data, "didReceiveDeepLinkWithIdentifier");

            if (name != null && Objects.equals(name.getPackageName(), packageName)) {
                if (action != null && !action.isEmpty() && data != null) {
                    startMainActivity(intent, packageName);
                    if (event.getBody() != null && !event.getBody().isEmpty())
                        handler.postDelayed(()->{
                            EventEmitter.getInstance().sendEvent(event);
                        },1000);

                }
            }
        }
        handler.postDelayed(this::finish, 2000);
    }

    private void startMainActivity(Intent intent, String packageName) {
        Intent launchIntent = getDefaultActivityIntent();
        ComponentName launcherComponent = launchIntent.getComponent();

        if (Objects.equals(launcherComponent.getPackageName(), packageName)) {
            launchIntent.putExtra("action", intent.getAction());
            launchIntent.setData(intent.getData());
            launchIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(launchIntent);
        }
    }

    private Intent getDefaultActivityIntent() {
        PackageManager packageManager = getPackageManager();
        return packageManager.getLaunchIntentForPackage(getPackageName());
    }
}