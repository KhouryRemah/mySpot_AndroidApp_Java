package com.example.myspot;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.time.OffsetTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.ContentValues.TAG;


public class NotificationFragment extends Fragment {
    private ArrayList<String> titlesList=new ArrayList<>();
    private ArrayList<String> bodiesList=new ArrayList<>();
    private ArrayList<String> timesList=new ArrayList<>();
    private boolean isVisitor=false;
    public FirebaseAuth mAuth=FirebaseAuth.getInstance();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view=inflater.inflate(R.layout.fragment_notification, container, false);

        isVisitor=getArguments().getBoolean("isVisitor");
        Bundle bundle=getArguments();
        isVisitor=bundle.getBoolean("isVisitor",false);


        TextView textView = (TextView)view.findViewById(R.id.welcome);
        textView.setText(printWelcomeMessage());
        TextView nameView = (TextView)view.findViewById(R.id.nameOfUser);
        if(isVisitor){
            nameView.setText("Visitor");

        }else{
            nameView.setText(mAuth.getCurrentUser().getDisplayName());
        }

        getNotifications(view);
        return view;

    }
    private void getUserNotification(final View view, FirebaseAuth mAuth, final FirebaseFirestore db ){
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
                        titlesList.add("");
                        timesList.add("");
                        bodiesList.add("You don't have notifications yet");
                    }else{
                        List<Map<String,Object>> list= (List<Map<String, Object>>) document.get("array");
                        assert list != null;
                        int size=list.size();
                        for(int i=size-1 ; i>=0 ; i--){
                            Map<String,Object> map=list.get(i);
                            titlesList.add((String) map.get("title"));
                            timesList.add((String) map.get("time"));
                            bodiesList.add((String) map.get("body"));
                        }
                    }
                    initRecyclerView(view);
                }
            }
        });
    }
    private void getVisitorNotification(final View view,FirebaseFirestore db, Task<InstanceIdResult> task ){
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
                        titlesList.add("");
                        timesList.add("");
                        bodiesList.add("You don't have notifications yet");
                    }else{
                        List<Map<String,Object>> list= (List<Map<String, Object>>) document.get("array");
                        assert list != null;
                        int size=list.size();
                        for(int i=size-1 ; i>=0 ; i--){
                            Map<String,Object> map=list.get(i);
                            titlesList.add((String) map.get("title"));
                            timesList.add((String) map.get("time"));
                            bodiesList.add((String) map.get("body"));
                        }
                    }
                    initRecyclerView(view);
                }
            }
        });
    }
    private void getNotifications(final View view){
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
                        if(mAuth.getCurrentUser()!=null){
                            getUserNotification(view,mAuth,db);
                        }else{//visitor
                            getVisitorNotification(view,db,task);
                        }
                    }
                });
    }
    private void initRecyclerView(View view){

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        RecyclerView.Adapter mAdapter=new MyAdapter(getActivity(),titlesList,bodiesList,timesList);
        recyclerView.setAdapter(mAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        //recyclerView.setHasFixedSize(true);

    }

/*

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true);

        // use a linear layout manager
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        // specify an adapter (see also next example)
        mAdapter = new MyAdapter(myDataset);
        recyclerView.setAdapter(mAdapter);
    }
    // ...
}*/
    public void startAlarmActivity(){
       // Intent intent=new Intent(getActivity(),AlarmActivity.class);
        //startActivity(intent);
    }


    public String printWelcomeMessage(){
        String message ="";
        if(isVisitor){
            message="visitor";
        }else{
            message = mAuth.getCurrentUser().getDisplayName();
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            OffsetTime offset = OffsetTime.now();
            if (offset.getHour() >= 6 && offset.getHour() < 12) {
                message = getResources().getString(R.string.MorningString) +" "+ message;
            }
            else if (offset.getHour() >= 12 && offset.getHour() < 17) {
                message = getResources().getString(R.string.AfternoonString) +" "+ message;
            }
            else if (offset.getHour() >= 17 && offset.getHour() < 20) {
                message = getResources().getString(R.string.EveningString) +" "+ message;
            }
            else if (offset.getHour() >= 20 || offset.getHour() < 6) {
                message = getResources().getString(R.string.NightString) +" "+ message;
            }
        }
        return message;
    }
}