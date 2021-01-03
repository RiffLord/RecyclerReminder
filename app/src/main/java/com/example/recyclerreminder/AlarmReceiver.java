package com.example.recyclerreminder;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.provider.Settings;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class AlarmReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "200";

    MediaPlayer mMediaPlayer;

    @Override
    public void onReceive(Context context, Intent intent) {
        mMediaPlayer = MediaPlayer.create(context, Settings.System.DEFAULT_NOTIFICATION_URI);
        mMediaPlayer.start();
        createNotification(context);
    }

    private void createNotification(Context context) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Recycling Reminder")
                .setContentText("Remember to throw out your waste.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);


        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        createNotificationIntent(context, builder);
        notificationManagerCompat.notify((int) System.currentTimeMillis(), builder.build());
    }

    private void createNotificationIntent(Context context, NotificationCompat.Builder builder) {
        Intent viewNotificationIntent = new Intent(context, ReminderActivity.class);
        viewNotificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, viewNotificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);
    }
}
