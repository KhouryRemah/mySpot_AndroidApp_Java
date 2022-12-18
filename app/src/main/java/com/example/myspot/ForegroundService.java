package com.example.myspot;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.security.Provider;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static android.content.ContentValues.TAG;
import static android.content.Context.ALARM_SERVICE;
import static androidx.core.content.ContextCompat.getSystemService;

public class ForegroundService extends BroadcastReceiver {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth mAuth = FirebaseAuth.getInstance();

    @Override
    public void onReceive(Context context, Intent intent) {
        setAlarm(context);
        updateDatabaseWithAlarm("on");

    }
    private static void updateAlarmToDbUser1(FirebaseAuth mAuth, final FirebaseFirestore db, final String onOrof) {
        final String email = mAuth.getCurrentUser().getEmail();
        db.collection("usersNotifications").document(email)
                .update("alarm", onOrof);
    }
    private static void updateAlarmToDbUser(FirebaseAuth mAuth, final FirebaseFirestore db, final String onOrof){
        final String email = mAuth.getCurrentUser().getEmail();
        DocumentReference dc = db.collection("usersNotifications").document(email);
        final Map<String,Object> map= new HashMap<>();
        map.put("alarm",onOrof);

        dc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(!task.isSuccessful()){
                    Log.d(TAG," task isn't successful " );
                }else{
                    DocumentSnapshot document=task.getResult();
                    assert document != null;
                    if(document.getData()==null){
                     db.collection("usersNotifications").document(email)
                        .set(map)/*.addOnSuccessListener(new OnSuccessListener<Void>() {
                         @Override
                         public void onSuccess(Void aVoid) {
                             Log.d(TAG, "DocumentSnapshot added ");
                         }
                     }).addOnFailureListener(new OnFailureListener() {
                         @Override
                         public void onFailure(@NonNull Exception e) {
                             Log.d(TAG,"on failure: " +e.toString() );
                         }
                     })*/;
                    }else{
                        db.collection("usersNotifications").document(email)
                                .update("alarm",onOrof);
                    }

                }
            }
        });
    }

    private static void updateAlarmToDbVisitor(final FirebaseFirestore db,Task<InstanceIdResult> task, String onOrof){
        final String token = task.getResult().getToken();
        db.collection("visitorsTokens").document(token).update("alarm",onOrof);

    }
    public static void updateDatabaseWithAlarm(final String onOrof){
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    public FirebaseAuth mAuth= FirebaseAuth.getInstance();
                    public FirebaseFirestore db = FirebaseFirestore.getInstance();

                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "getInstanceId failed", task.getException());
                            return;
                        }
                        if(mAuth.getCurrentUser()!=null){
                            updateAlarmToDbUser1(mAuth,db,onOrof);
                        }else{//visitor
                            updateAlarmToDbVisitor(db,task,onOrof);
                        }
                    }
                });
    }

    private void setAlarm(Context context){
        Calendar now=Calendar.getInstance();
        long nowInMillis=now.getTimeInMillis();

        Calendar cal=Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        cal.set(Calendar.HOUR_OF_DAY,18);
        cal.set(Calendar.MINUTE,11);
        cal.set(Calendar.SECOND,0);
        long timeInMillis=cal.getTimeInMillis();

        if(timeInMillis-nowInMillis<0){
            //if its in past, add one day
            timeInMillis += 86400000;
        }

        Intent intent= new Intent(context,ReminderBroadcastReceiver.class);
        PendingIntent pendingIntent=PendingIntent.getBroadcast(context,200,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager= (AlarmManager) context.getSystemService(ALARM_SERVICE);

        Toast.makeText(context,"Reminder have been set to hour "+cal.getTime(),Toast.LENGTH_SHORT).show();
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis,pendingIntent);
    }

}
