package com.den.lilytoggle;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class MainActivity extends Activity {
    private static final String LAUNCHER =
            "/data/adb/modules/lily-toggle/launch.sh";

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);

        new Thread(() -> {
            boolean ok = false;
            try {
                /*
                 * Ask Magisk su to execute the module-side launcher.
                 * The launcher itself detaches the actual listener from
                 * this Android process.
                 */
                Process p = new ProcessBuilder(
                        "su", "-c", LAUNCHER
                ).redirectErrorStream(true).start();

                BufferedReader br = new BufferedReader(
                        new InputStreamReader(p.getInputStream()));

                String line;
                StringBuilder output = new StringBuilder();
                while ((line = br.readLine()) != null) {
                    output.append(line).append('\n');
                }

                int rc = p.waitFor();
                ok = (rc == 0);

            } catch (Exception ignored) {
            }

            final boolean result = ok;
            new Handler(getMainLooper()).post(() -> {
                Toast.makeText(
                        this,
                        result ? "Lily Toggle đã chạy" : "Lily Toggle lỗi",
                        Toast.LENGTH_SHORT
                ).show();

                /*
                 * The user explicitly accepts the current screen remaining
                 * in front. Keep the Activity alive; the listener is detached
                 * by launch.sh and does not depend on this Activity.
                 */
            });
        }, "LilyToggleLauncher").start();
    }
}
