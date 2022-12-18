package com.example.myspot;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.protobuf.StringValue;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class InfoOfSpotAc extends AppCompatActivity {

    private String[] buildingsArr={"450","451","452","453","454","455","456","457","458","459"};
    private ArrayList<String> buildingsList=new ArrayList<>(Arrays.asList(buildingsArr));

    private String[] AreaArr={"1","2","2Licensed","3","3Licensed","4","4Licensed","Peripheral plot","inFrontOfBioInformatics"};
    private ArrayList<String> areaList=new ArrayList<>(Arrays.asList(AreaArr));

    AutoCompleteTextView symbol; //it's also for parking areas
    public String userSelection ="";
    EditText edittext;
    String buildingStr="None";
    String areaStr = "None";

    boolean isVisitor=false;


    final Calendar myCalendar = Calendar.getInstance();
    DatePickerDialog.OnDateSetListener date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_of_spot);

        Toolbar toolbar= findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        isVisitor= getIntent().getBooleanExtra("isVisitor",false);


        Button checkButton=findViewById(R.id.check);
        checkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(InfoOfSpotAc.this, ShowTableActivity.class);
                intent.putExtra("isVisitor",isVisitor);
                intent.putExtra("date", edittext.getText().toString());
                intent.putExtra("buildingOrArea",userSelection);
                intent.putExtra("buiNoOrAreaNo",symbol.getText().toString());
                if(userSelection.equals("Building")){
                    buildingStr= symbol.getText().toString();
                    if(!buildingsList.contains(buildingStr)){
                        symbol.setError("The building you entered isn't found");
                        symbol.requestFocus();
                        //Toast.makeText(InfoOfSpotAc.this,"The building you entered isn't found",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if(edittext.getText().toString().equals("")){
                        edittext.setError("You must choose a specific date");
                        edittext.requestFocus();
                       Toast.makeText(InfoOfSpotAc.this,"You must choose a specific date",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    startActivity(intent);
                }else if(userSelection.equals("Area")){
                    areaStr= symbol.getText().toString();
                    if(!areaList.contains(areaStr)){
                        symbol.setError("The Area you entered isn't found");
                        symbol.requestFocus();
                        //Toast.makeText(InfoOfSpotAc.this,"The area you entered isn't found",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if(edittext.getText().toString().equals("")){
                        edittext.setError("You must choose a specific date");
                        edittext.requestFocus();
                        Toast.makeText(InfoOfSpotAc.this,"You must choose a specific date",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    startActivity(intent);
                }else{
                    Toast.makeText(InfoOfSpotAc.this,"You must choose area or building",Toast.LENGTH_SHORT).show();
                }
            }
        });


        symbol=(AutoCompleteTextView)findViewById(R.id.areaOrBuildingNo);

        final ArrayAdapter<String> adapter=new ArrayAdapter<String>(InfoOfSpotAc.this,android.R.layout.select_dialog_item,buildingsList);
        final ArrayAdapter<String> areaAdapter=new ArrayAdapter<String>(this,android.R.layout.select_dialog_item,areaList);

        symbol.setThreshold(1);
        symbol.setAdapter(adapter);


        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.reachRadioGroup);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // checkedId is the RadioButton selected
                RadioButton rb=(RadioButton)findViewById(checkedId);
                if(rb.getId() == R.id.areaRadio){
                    userSelection = "Area";
                    symbol.setAdapter(areaAdapter);
                }else{
                    userSelection="Building";
                    symbol.setAdapter(adapter);
                }
            }
        });

        edittext= (EditText) findViewById(R.id.dateToExpect);

        date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel();
            }

        };

        edittext.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                new DatePickerDialog(InfoOfSpotAc.this , date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });



    }

    private void updateLabel() {
        String myFormat = "dd/MM/yyyy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        edittext.setText(sdf.format(myCalendar.getTime()));
    }


    public void signOut() {
        if(!isVisitor){
            MainActivity. mGoogleSignInClient.signOut()
                    .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            // ...
                        }
                    });
            FirebaseAuth.getInstance().signOut();
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
                intent.putExtra("parent","InfoOfSpotAc");
                intent.putExtra("isVisitor",isVisitor);
                startActivity(intent);
                break;
            case R.id.signOut:
                Toast.makeText(this, "You signed out", Toast.LENGTH_SHORT).show();
                signOut();
                break;

        }
        return true;
    }



}
