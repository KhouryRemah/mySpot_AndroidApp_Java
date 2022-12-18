package com.example.myspot;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

public class ReminderBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String title="Reminder";
        String body="You should move your car to another parking area.";
        ForegroundService.updateDatabaseWithAlarm("off");
        NotificationHelper.displayNotification(context,title,body,ReachSpot.CHANNEL_ID2);


    }
}
