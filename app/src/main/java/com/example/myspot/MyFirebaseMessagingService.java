package com.example.myspot;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        RemoteMessage.Notification notification=remoteMessage.getNotification();
        if(notification!=null){
            String title=notification.getTitle();
            String body=notification.getBody();
            NotificationHelper.displayNotification(getApplicationContext(),title,body,ReachSpot.CHANNEL_ID);
        }


    }
}
