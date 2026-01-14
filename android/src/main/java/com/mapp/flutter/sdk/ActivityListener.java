package com.mapp.flutter.sdk;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import com.appoxee.Appoxee;
import com.appoxee.internal.logger.Logger;
import com.appoxee.internal.logger.LoggerFactory;

import io.flutter.embedding.android.FlutterActivity;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.embedding.engine.FlutterEngineCache;
import io.flutter.embedding.engine.FlutterEngineGroup;
import io.flutter.embedding.engine.plugins.FlutterPlugin;

public class ActivityListener extends Activity {

    private final Logger devLogger = LoggerFactory.getDevLogger();

    private final Handler handler=new Handler(Looper.myLooper());
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        startLauncherActivity(intent);
        handler.postDelayed(this::finish,1000);
    }

    private void startLauncherActivity(Intent intent) {
        Intent launchIntent = getDefaultActivityIntent();
        launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        launchIntent.putExtra("intent",intent);
        startActivity(launchIntent);

/*        Intent richIntent = new Intent("com.mapp.RICH_PUSH");
        Bundle extras = intent.getExtras();
        String data = extras != null ? extras.getString("com.mapp.RICH_PUSH") : null;

        if (extras != null && data != null) {
            richIntent.putExtra("com.mapp.RICH_PUSH", data);
            launchIntent.putExtra("richIntent", richIntent);
            startActivity(launchIntent);
        }*/
    }

    private Intent getDefaultActivityIntent() {
        PackageManager packageManager = getPackageManager();
        return packageManager.getLaunchIntentForPackage(getPackageName());
    }
}
