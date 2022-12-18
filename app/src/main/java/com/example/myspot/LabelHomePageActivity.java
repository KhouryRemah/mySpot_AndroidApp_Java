package com.example.myspot;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;


public class LabelHomePageActivity extends AppCompatActivity {
    private BottomNavigationView bottomNavigationView;

    private  FirebaseAuth mAuth = FirebaseAuth.getInstance();
    boolean isVisitor=false;
    String currentFragmentString=null;
    boolean fromNotifications=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_label_home_page);

        Toolbar toolbar= findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        //Intent intent = getIntent();
        isVisitor= getIntent().getBooleanExtra("isVisitor",false);
        fromNotifications=getIntent().getBooleanExtra("fromNotifications",false);
        //Toast.makeText(LabelHomePageActivity.this, "LabelAct : isVisitor= "+String.valueOf(isVisitor), Toast.LENGTH_SHORT).show();

        bottomNavigationView=findViewById(R.id.bottomNav);
        if (!isVisitor ) {
            bottomNavigationView.inflateMenu(R.menu.menubottom);
            bottomNavigationView.setOnNavigationItemSelectedListener(bottomNaviMethod);
        } else {
            bottomNavigationView.inflateMenu(R.menu.menubottomvisitor);
            bottomNavigationView.setOnNavigationItemSelectedListener(bottomNaviMethodVisitor);

        }

        Bundle bundle = new Bundle();
        bundle.putBoolean("isVisitor",isVisitor);
        if(fromNotifications){

            NotificationFragment notificationFragment=new NotificationFragment();
            notificationFragment.setArguments(bundle);
            getSupportFragmentManager().beginTransaction().replace(R.id.container,notificationFragment).commit();

        }
        else{
            HomeFragment homeFragment=new HomeFragment();
            homeFragment.setArguments(bundle);
            getSupportFragmentManager().beginTransaction().replace(R.id.container,homeFragment).commit();

        }






    }
    private BottomNavigationView.OnNavigationItemSelectedListener bottomNaviMethod=new
            BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Fragment fragment = new HomeFragment();//Note: default fragment
                    String string = "HOME";
                    switch (item.getItemId()) {
                        case R.id.home:
                            Toast.makeText(LabelHomePageActivity.this, "Home clicked", Toast.LENGTH_SHORT).show();
                            fragment = new HomeFragment();
                            string = "HOME";
                            break;
                        case R.id.notifications:
                            Toast.makeText(LabelHomePageActivity.this, "Notifications clicked", Toast.LENGTH_SHORT).show();
                            fragment = new NotificationFragment();
                            string = "NOTIFICATIONS";
                            break;
                        case R.id.profile:
                            Toast.makeText(LabelHomePageActivity.this, "Profile clicked", Toast.LENGTH_SHORT).show();
                            fragment = new ProfileFragment();
                            string = "PROFILE";
                            break;

                    }
                    if (string.equals(currentFragmentString)){
                    return true;
                    }
                    Bundle bundle = new Bundle();
                    bundle.putBoolean("isVisitor",isVisitor);
                    fragment.setArguments(bundle);
                    currentFragmentString=string;
                    getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment, string).commit();
                    return true;

                }


            };
    private BottomNavigationView.OnNavigationItemSelectedListener bottomNaviMethodVisitor=new
            BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Fragment fragment = new HomeFragment();//Note: default fragment
                    String string = "HOME";
                    switch (item.getItemId()) {
                        case R.id.home:
                            Toast.makeText(LabelHomePageActivity.this, "Home clicked", Toast.LENGTH_SHORT).show();
                            fragment = new HomeFragment();
                            string = "HOME";
                            break;
                        case R.id.notifications:
                            Toast.makeText(LabelHomePageActivity.this, "Notifications clicked", Toast.LENGTH_SHORT).show();
                            string = "NOTIFICATIONS";
                            fragment = new NotificationFragment();
                            break;

                    }
                    if (string.equals(currentFragmentString)){
                        return true;
                    }
                    Bundle bundle = new Bundle();
                    bundle.putBoolean("isVisitor",isVisitor);
                    fragment.setArguments(bundle);
                    currentFragmentString=string;
                    getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment, string).commit();
                    return true;

                }


            };
/*
    @Override
    public void editButtonPressed() {
        final ProfileFragment fragment = (ProfileFragment) getSupportFragmentManager().findFragmentByTag(TAG_FRAGMENT);

        if (fragment.allowBackPressed()) { // and then you define a method allowBackPressed with the logic to allow back pressed or not
            super.onBackPressed();
        }
    }*/

    public void signOut() {
        if (!isVisitor){
            //GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
            MainActivity. mGoogleSignInClient.signOut()
                    .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            // ...
                        }
                    });
            FirebaseAuth.getInstance().signOut();
            //MainActivity.mAuth.signOut();
        }
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }





    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater=getMenuInflater();//layout
        inflater.inflate(R.menu.menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item){
        switch (item.getItemId()) {
            case R.id.map:
                Intent intent = new Intent(this, MapsActivity.class);
                intent.putExtra("parent","LabelHomePageActivity");
                startActivity(intent);
                break;
            case R.id.signOut:
                Toast.makeText(this, "You signed out", Toast.LENGTH_SHORT).show();
                signOut();
                break;

        }
        return true;
    }


    @Override
    public void onBackPressed() {

        if(this.getCallingActivity()!=null){
            Toast.makeText(this, "You have to sign out.", Toast.LENGTH_SHORT).show();
            return;
        }
        else{
            super.onBackPressed();
        }

        /*if (shouldAllowBack()) {
            super.onBackPressed();
        } else {
            doSomething();
        }*/
    }
}