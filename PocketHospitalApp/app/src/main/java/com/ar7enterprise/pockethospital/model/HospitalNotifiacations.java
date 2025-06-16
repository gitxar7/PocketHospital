package com.ar7enterprise.pockethospital.model;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.ar7enterprise.pockethospital.R;

public class HospitalNotifiacations {

    public static void makeNotfications(Context context, Class cls, String title, String message) {
        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(
                    "n1",
                    "Emergency Alerts",
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            notificationManager.createNotificationChannel(notificationChannel);
        }

        Intent intent = new Intent(context, cls);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Action action = new NotificationCompat.Action.Builder(
                R.drawable.hospital_g,
                "View",
                pendingIntent
        ).build();

        Notification notification = new NotificationCompat.Builder(context, "n1")
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.hospital_g)
                .addAction(action)
                .build();

        notificationManager.notify(1, notification);
    }
}
