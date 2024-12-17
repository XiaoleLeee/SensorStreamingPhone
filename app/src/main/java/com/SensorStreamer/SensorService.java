package com.SensorStreamer;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

/**
 * 用于保活的前台通知
 * @author chen
 * @version 1.0
 */
public class SensorService extends Service {
    private static final String CHANNEL_ID = "sensor_streamer_channel";
    public static  final String ACTION_STOP_FORE = "action_stop_fore";
    public static  final String ACTION_START_FORE = "action_start_fore";
//    通知
    private Notification notification;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null)
            return START_NOT_STICKY;

        if (SensorService.ACTION_STOP_FORE.equals(intent.getAction())) {
            stopForeground(true);
            return START_NOT_STICKY;
        }

        if (SensorService.ACTION_START_FORE.equals(intent.getAction())) {
            startForeground(1, this.notification);
            return START_STICKY;
        }

        return START_NOT_STICKY;
    }

//    创建通知栏
    private Notification createNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle("Sensor Streamer")
                .setContentText("Streaming sensors data...")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        return builder.build();
    }

    @Override
    public void onCreate() {
        super.onCreate();
//        创建通知渠道
        CharSequence name = "Sensor Streamer";
        String description = "Channel for sensor streaming";
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
//        渠道
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
        channel.setDescription(description);
//        管理者
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
//        创建通知栏
        this.notification = createNotification();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
