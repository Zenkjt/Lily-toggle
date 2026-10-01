package com.den.lilytoggle;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static final String LAUNCHER =
            "/data/adb/modules/lily-toggle/launch.sh";

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);

        new Thread(() -> {
            boolean ok = false;

            try {
                Process p = new ProcessBuilder(
                        "su", "-c", LAUNCHER
                ).redirectErrorStream(true).start();

                int rc = p.waitFor();
                ok = (rc == 0);

            } catch (Exception ignored) {
            }

            final boolean result = ok;

            runOnUiThread(() -> {
                Toast.makeText(
                        this,
                        result ? "Lily Toggle đã chạy" : "Lily Toggle lỗi",
                        Toast.LENGTH_SHORT
                ).show();

                finish();
            });
        }, "LilyToggleLauncher").start();
    }
}
