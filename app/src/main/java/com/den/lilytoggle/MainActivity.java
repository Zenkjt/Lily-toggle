package com.den.lilytoggle;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import java.io.InputStream;

public class MainActivity extends Activity {
    private static final String SCRIPT =
            "/data/adb/modules/lily-toggle/lily-toggle.sh";

    private Process listenerProcess;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);

        startListener();

        new Handler(getMainLooper()).postDelayed(() -> {
            Toast.makeText(this, "Lily Toggle đã chạy", Toast.LENGTH_SHORT).show();

            // Do not finish(). Keep this Activity/process alive while the
            // root shell runs. The window is transparent, so the launcher
            // remains visually available underneath it.
        }, 250);
    }

    private void startListener() {
        if (listenerProcess != null && listenerProcess.isAlive()) {
            return;
        }

        new Thread(() -> {
            try {
                Process p = new ProcessBuilder(
                        "su", "-c",
                        "/system/bin/sh " + SCRIPT
                ).redirectErrorStream(true).start();

                listenerProcess = p;

                // Drain output so the child cannot block on a full pipe.
                InputStream in = p.getInputStream();
                byte[] buffer = new byte[256];
                while (p.isAlive()) {
                    while (in.available() > 0) {
                        in.read(buffer);
                    }
                    Thread.sleep(100);
                }
            } catch (Exception ignored) {
            }
        }, "LilyToggleRoot").start();
    }

    @Override
    protected void onDestroy() {
        // Deliberately do NOT destroy the root listener here.
        // The listener should survive the Activity window lifecycle.
        super.onDestroy();
    }
}
