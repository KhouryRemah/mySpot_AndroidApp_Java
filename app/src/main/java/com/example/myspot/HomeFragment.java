package com.example.myspot;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.time.OffsetTime;

import static android.content.ContentValues.TAG;


public class HomeFragment extends Fragment {
    private FirebaseAuth mAuth;

    private boolean isVisitor=false;
    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_home, container, false);

        mAuth  = FirebaseAuth.getInstance();

        isVisitor=getArguments().getBoolean("isVisitor");
        Bundle bundle=getArguments();
        isVisitor=bundle.getBoolean("isVisitor",false);
        Toast.makeText(getActivity(),"Frag : isVisitor= "+ String.valueOf(isVisitor), Toast.LENGTH_SHORT).show();


        TextView textView = (TextView)view.findViewById(R.id.welcome);
        textView.setText(printWelcomeMessage());
        TextView nameView = (TextView)view.findViewById(R.id.nameOfUser);
        if(isVisitor){
            nameView.setText("Visitor");

        }else{
            nameView.setText(mAuth.getCurrentUser().getDisplayName());
        }

        ImageView findButton=(ImageView)view.findViewById(R.id.map);
        findButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ReachSpot.class);
                intent.putExtra("isVisitor",isVisitor);
                startActivity(intent);
            }
        });

        ImageView infoButton=(ImageView)view.findViewById(R.id.info);
        infoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), InfoOfSpotAc.class);
                intent.putExtra("isVisitor",isVisitor);
                startActivity(intent);
            }
        });

        ImageView planButton=(ImageView)view.findViewById(R.id.plan);
        planButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), AlarmActivity.class);
                intent.putExtra("isVisitor",isVisitor);
                startActivity(intent);
            }
        });
        return view;
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
