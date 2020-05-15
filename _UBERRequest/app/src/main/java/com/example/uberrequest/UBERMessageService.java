package com.example.uberrequest;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.Common.Config;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;


public class UBERMessageService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        String message = remoteMessage.getNotification().getTitle();
        String body = remoteMessage.getNotification().getBody();
        if (remoteMessage.getData().size() > 0) {
            body = remoteMessage.getData().get("openURL");
        }
        showNotification(message, body);
    }

    public void showNotification(String title, String message){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,"BusinessNotification")
                .setContentTitle(title)
                .setSmallIcon(R.drawable.ic_baseline_business_24)
                .setAutoCancel(true)
                .setContentText(message);

        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(this);
        managerCompat.notify(999,builder.build());
    }
}
