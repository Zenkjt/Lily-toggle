package com.den.lilytoggle;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import java.io.File;
import java.io.IOException;

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
                String su = findSu();
                Process p = new ProcessBuilder(
                        su, "-c",
                        "exec /system/bin/sh " + SCRIPT
                ).redirectErrorStream(true).start();

                listenerProcess = p;
                p.waitFor();
            } catch (Exception e) {
                writeError(e);
            } finally {
                listenerProcess = null;
            }
        }, "LilyToggleRoot").start();
    }

    private String findSu() throws IOException {
        String[] candidates = {
                "/system/xbin/su",
                "/system/bin/su",
                "/sbin/su"
        };

        for (String path : candidates) {
            if (new File(path).canExecute()) {
                return path;
            }
        }

        // Fallback: let Android resolve su through PATH.
        return "su";
    }

    private void writeError(Exception e) {
        try {
            java.io.FileOutputStream out =
                    new java.io.FileOutputStream("/data/local/tmp/lily-toggle-app.log", true);
            String msg = e.toString() + "\n";
            out.write(msg.getBytes("UTF-8"));
            out.close();
        } catch (Exception ignored) {
        }
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
