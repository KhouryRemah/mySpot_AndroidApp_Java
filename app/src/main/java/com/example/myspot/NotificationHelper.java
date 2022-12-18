package com.example.myspot;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.gson.internal.$Gson$Preconditions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static android.content.ContentValues.TAG;

public class NotificationHelper {

    public static void displayNotification(Context context,String title,String body,String channelId){
        Intent intent=new Intent(context,LabelHomePageActivity.class);
        intent.putExtra("fromNotifications",true);
        PendingIntent pendingIntent=PendingIntent.getActivity(context,100,intent
                ,PendingIntent.FLAG_UPDATE_CURRENT);

        Intent serviceIntent=new Intent(context,ForegroundService.class);
        PendingIntent servicePendingIntent=PendingIntent.getBroadcast(context,0
                ,serviceIntent,PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder mBuilder= new NotificationCompat.Builder(context,channelId)
                .setSmallIcon(R.drawable.ic_notifications)
                .setContentTitle(title)
                .setContentText(body)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setPriority(NotificationCompat.PRIORITY_MAX);
        if(channelId.equals(ReachSpot.CHANNEL_ID)){
            mBuilder.addAction(R.drawable.ic_notifications,"Set reminder",servicePendingIntent);
        }
        NotificationManagerCompat notificationMgr=NotificationManagerCompat.from(context);
        notificationMgr.notify(1,mBuilder.build());

        //adding noitification to database
        addNotificationToDb(context,title,body);
    }
    private static void addNotificationToDbUser(FirebaseAuth mAuth, final FirebaseFirestore db
            , final Map<String,Object> notification ){
        final String email = mAuth.getCurrentUser().getEmail();
        DocumentReference dc = db.collection("usersNotifications").document(email);
        dc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(!task.isSuccessful()){
                    Log.d(TAG," task isn't successful " );
                }else{
                    DocumentSnapshot document=task.getResult();
                    assert document != null;
                    if(document.get("array")==null){
                        List<Map<String,Object>> list=new ArrayList<Map<String,Object>>();
                        list.add(notification);
                        Map<String,Object> mapArray=new HashMap<String,Object>();
                        mapArray.put("array",list);
                        db.collection("usersNotifications").document(email)
                                .set(mapArray).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "DocumentSnapshot added ");
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d(TAG,"on failure: " +e.toString() );
                            }
                        });

                    }else{
                        List<Map<String,Object>> list= (List<Map<String, Object>>) document.get("array");
                        assert list != null;
                        list.add(notification);
                        if(list.size()>10){
                            list.remove(0);
                        }
                        db.collection("usersNotifications").document(email)
                                .update("array",list).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "DocumentSnapshot updated ");
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d(TAG,"on failure: " +e.toString() );
                            }
                        });
                    }
                }
            }
        });
        // dc.update("array", FieldValue.arrayUnion(notification));
    }
    private  static  void addNotificationToDbVisitor(final FirebaseFirestore db
            ,final Map<String,Object> notification,Task<InstanceIdResult> task){
        // Get new Instance IonD token
        final String token = task.getResult().getToken();
        Log.d(TAG, token);
        DocumentReference dc = db.collection("visitorsTokens").document(token);
        dc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(!task.isSuccessful()){
                    Log.d(TAG," task isn't successful " );

                }else{
                    DocumentSnapshot document=task.getResult();
                    assert document != null;
                    if(document.get("array")==null){
                        List<Map<String,Object>> list=new ArrayList<Map<String,Object>>();
                        list.add(notification);
                        Map<String,Object> mapArray=new HashMap<String,Object>();
                        mapArray.put("array",list);
                        db.collection("visitorsTokens").document(token)
                                .set(mapArray).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "DocumentSnapshot added ");
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d(TAG,"on failure: " +e.toString() );
                            }
                        });
                    }else{
                        List<Map<String,Object>> list= (List<Map<String, Object>>) document.get("array");
                        assert list != null;
                        list.add(notification);
                        if(list.size()>10){
                            list.remove(0);
                        }
                        db.collection("visitorsTokens").document(token)
                                .update("array",list).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "DocumentSnapshot updated ");
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d(TAG,"on failure: " +e.toString() );
                            }
                        });
                    }
                }
            }
        });
        //dc.update("array", FieldValue.arrayUnion(notification));
    }
    private static void addNotificationToDb(final Context context,final String title, final String body){//for all users
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    public FirebaseAuth mAuth=FirebaseAuth.getInstance();
                    FirebaseFirestore db = FirebaseFirestore.getInstance();

                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "getInstanceId failed", task.getException());
                            return;
                        }
                        final Map<String,Object> notification = new HashMap<>();
                        notification.put("title",title);
                        notification.put("body",body);

                        Date now = new Date(); // java.util.Date, NOT java.sql.Date or java.sql.Timestamp!
                        String format1 = new SimpleDateFormat("dd/MM/yyyy, HH:mm:ss", Locale.ENGLISH).format(now);
                        notification.put("time",format1);

                        if(mAuth.getCurrentUser()!=null){
                            addNotificationToDbUser(mAuth,db,notification);
                        }else{//visitor
                            addNotificationToDbVisitor(db,notification,task);
                        }
                    }
                });
    }


}
