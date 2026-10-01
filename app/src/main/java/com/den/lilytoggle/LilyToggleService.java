package com.den.lilytoggle;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

public class LilyToggleService extends Service {
    private static final String CHANNEL_ID = "lily_toggle";
    private static final int NOTIFICATION_ID = 1001;
    private static final String SCRIPT =
            "/data/adb/modules/lily-toggle/lily-toggle.sh";

    private volatile Process listenerProcess;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        startForeground(NOTIFICATION_ID, buildNotification());
        startListener();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (listenerProcess == null || !listenerProcess.isAlive()) {
            startListener();
        }
        return START_STICKY;
    }

    private synchronized void startListener() {
        if (listenerProcess != null && listenerProcess.isAlive()) {
            return;
        }

        new Thread(() -> {
            try {
                // Do NOT use '&' here. Keep the root shell attached to
                // this Service while lily-toggle.sh runs.
                Process p = new ProcessBuilder(
                        "su", "-c",
                        "/system/bin/sh " + SCRIPT
                ).redirectErrorStream(true).start();

                listenerProcess = p;
                p.waitFor();
            } catch (Exception ignored) {
            } finally {
                listenerProcess = null;
            }
        }, "LilyToggleRoot").start();
    }

    @Override
    public void onDestroy() {
        Process p = listenerProcess;
        if (p != null) {
            p.destroy();
        }
        listenerProcess = null;
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Lily Toggle",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Lily Toggle listener");
            channel.setShowBadge(false);

            NotificationManager nm =
                    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (nm != null) {
                nm.createNotificationChannel(channel);
            }
        }
    }

    private Notification buildNotification() {
        if (Build.VERSION.SDK_INT >= 26) {
            return new Notification.Builder(this, CHANNEL_ID)
                    .setContentTitle("Lily Toggle")
                    .setContentText("Volume Up: Clock ↔ Lily")
                    .setSmallIcon(android.R.drawable.ic_lock_idle_lock)
                    .setOngoing(true)
                    .build();
        }

        return new Notification.Builder(this)
                .setContentTitle("Lily Toggle")
                .setContentText("Volume Up: Clock ↔ Lily")
                .setSmallIcon(android.R.drawable.ic_lock_idle_lock)
                .setOngoing(true)
                .build();
    }
}
