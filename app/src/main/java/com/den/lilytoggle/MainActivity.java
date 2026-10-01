package com.den.lilytoggle;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class MainActivity extends Activity {
    private static final String SCRIPT =
            "/data/adb/modules/lily-toggle/lily-toggle.sh";

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);

        new Thread(() -> {
            boolean running = isListenerRunning();
            if (!running) {
                startListener();
            }
            new Handler(getMainLooper()).post(() -> {
                Toast.makeText(this,
                        running ? "Lily Toggle đang chạy" : "Lily Toggle đã khởi động",
                        Toast.LENGTH_SHORT).show();
                finish();
            });
        }).start();
    }

    private boolean isListenerRunning() {
        try {
            Process p = Runtime.getRuntime().exec(
                    new String[]{"su", "-c",
                            "pidof lily-toggle.sh 2>/dev/null || " +
                            "ps -A | grep '[l]ily-toggle.sh' >/dev/null"});
            return p.waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }

    private void startListener() {
        try {
            Runtime.getRuntime().exec(
                    new String[]{"su", "-c",
                            "nohup /system/bin/sh " + SCRIPT +
                            " >/data/local/tmp/lily-toggle.log 2>&1 </dev/null &"});
        } catch (Exception ignored) {
        }
    }
}
