package com.example.myspot;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import static android.content.ContentValues.TAG;

public class AlarmActivity extends AppCompatActivity {
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private boolean isVisitor=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);

        isVisitor=getIntent().getBooleanExtra("isVisitor",false);

        Toolbar toolbar= findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        showText();
        //setAlarm();


    }
    private void showTextUser(FirebaseAuth mAuth, final FirebaseFirestore db) {
        final String email = mAuth.getCurrentUser().getEmail();
        db.collection("usersNotifications").document(email)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(!task.isSuccessful()){
                    Log.d(TAG," task isn't successful " );
                    return;
                }
                DocumentSnapshot document=task.getResult();
                assert document != null;
                String str="mySpot will send you a Reminder notification to remind you to move your car before 7:00 AM.";
                String off="off";
                if(document.get("alarm")==null || off.equals(document.get("alarm"))){
                    str="You don't have any reminders";
                }
                TextView textView = (TextView)findViewById(R.id.textView);
                textView.setText(str);
            }
        });
    }

    private void showTextVisitor(final FirebaseFirestore db,Task<InstanceIdResult> task){
        final String token = task.getResult().getToken();
        db.collection("visitorsTokens").document(token)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(!task.isSuccessful()){
                    Log.d(TAG," task isn't successful " );
                    return;
                }
                DocumentSnapshot document=task.getResult();
                assert document != null;
                String str="mySpot will send you a Reminder notification to remind you to move your car before 7:00 AM.";
                String off="off";
                if(document.get("alarm")==null || off.equals(document.get("alarm"))){
                    str="You don't have any reminders";
                }
                TextView textView = (TextView)findViewById(R.id.textView);
                textView.setText(str);
            }
        });
    }
    public void showText(){
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
                            showTextUser(mAuth,db);
                        }else{//visitor
                            showTextVisitor(db,task);
                        }
                    }
                });
    }


    public void signOut() {
        //GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if(!isVisitor &&  mAuth.getCurrentUser()!=null){
            MainActivity. mGoogleSignInClient.signOut().addOnCompleteListener(this, new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                }
            });
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(this,"sigining out isVisitor= "+isVisitor,Toast.LENGTH_SHORT).show();
        }
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater=getMenuInflater();//layout
        inflater.inflate(R.menu.alarmmenutop,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item){
        switch (item.getItemId()) {
            case R.id.signOut:
                Toast.makeText(this, "You signed out", Toast.LENGTH_SHORT).show();
                signOut();
                break;
        }
        return true;
    }



}