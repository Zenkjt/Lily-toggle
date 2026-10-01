package com.den.lilytoggle;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);

        startService(new Intent(this, LilyToggleService.class));

        new Handler(getMainLooper()).postDelayed(() -> {
            Toast.makeText(this, "Lily Toggle đang chạy", Toast.LENGTH_SHORT).show();
            finish();
        }, 250);
    }
}
