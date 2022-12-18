package com.example.myspot;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.time.OffsetTime;

public class ProfileActivity extends AppCompatActivity {
    private BottomNavigationView bottomNavigationView;
    private  FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser currentUser = mAuth.getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        String message = currentUser.getDisplayName() + "profile";
        TextView textView = findViewById(R.id.textView);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            OffsetTime offset = OffsetTime.now();
            if (offset.getHour()>=6 && offset.getHour()<12)textView.setText("Good Morning "+ message);
            else if (offset.getHour()>=12 && offset.getHour()<17)textView.setText("Good Afternoon "+ message);
            else if (offset.getHour()>=17 && offset.getHour()<20)textView.setText("Good Evening "+ message);
            else if (offset.getHour()>=20 || offset.getHour()<6)textView.setText("Good Night "+ message);

        }


        Toolbar toolbar= findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        bottomNavigationView=findViewById(R.id.bottomNav);
        bottomNavigationView.setOnNavigationItemSelectedListener(bottomNaviMethod);
    }
    private BottomNavigationView.OnNavigationItemSelectedListener bottomNaviMethod=new
            BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    switch(item.getItemId()){
                        case R.id.home:
                            Toast.makeText(ProfileActivity.this,"Home clicked",Toast.LENGTH_SHORT).show();
                            break;
                        case R.id.notifications:
                            Toast.makeText(ProfileActivity.this,"Notifications clicked",Toast.LENGTH_SHORT).show();
                            break;
                        case R.id.profile:
                            Toast.makeText(ProfileActivity.this,"Profile clicked",Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(getBaseContext(), ProfileActivity.class);
                            startActivity(intent);
                            break;
                    }
                    return false;
                }
            };






    public void signOut() {
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
            case R.id.profile:
                Toast.makeText(this, "Going to profile", Toast.LENGTH_SHORT).show();
                break;
            case R.id.signOut:
                Toast.makeText(this, "You signed out", Toast.LENGTH_SHORT).show();
                signOut();
                break;

        }
        return true;
    }

}
