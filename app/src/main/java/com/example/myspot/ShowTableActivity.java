package com.example.myspot;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.protobuf.StringValue;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ShowTableActivity extends AppCompatActivity {

    private static int day;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    public String userSelection ="";
    public String userDate="";
    public String userText ="";
    Map<String,Object> map=new HashMap<>();
    //public int day;
    private boolean isVisitor=false;
    private String[] hourArray = {"00:00","01:00","02:00","03:00","04:00","05:00"
            ,"06:00","07:00","08:00","09:00","10:00","11:00","12:00","13:00"
            ,"14:00","15:00","16:00","17:00","18:00","19:00","20:00","21:00","22:00","23:00"};
    ArrayList<String> hourList = new ArrayList<>(Arrays.asList(hourArray));
    TableLayout table;
    private String[] areasResults;
    private String[] validOrNotResults;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_table);
        Intent intent = getIntent();

        TextView loadingMsg=(TextView) findViewById(R.id.loadingMsg);
        loadingMsg.setVisibility(View.VISIBLE);

        isVisitor=intent.getBooleanExtra("isVisitor",false);
        userSelection= intent.getStringExtra("buildingOrArea");
        userDate=intent.getStringExtra("date");
        userText=intent.getStringExtra("buiNoOrAreaNo");

        table = (TableLayout) findViewById(R.id.ourTable);
        table.setVisibility(View.INVISIBLE);
        validOrNotResults = new String[24];
        areasResults = new String[24];

       /* Toast.makeText(this,userSelection,Toast.LENGTH_SHORT).show();
        Toast.makeText(ShowTableActivity.this,String.valueOf(userDate),Toast.LENGTH_SHORT).show();
        Toast.makeText(this,userText,Toast.LENGTH_SHORT).show();*/
        if(userSelection.equals("Area")){
            map.put("area",userText);//Note to change to userText
            map.put("vacation",String.valueOf(itsVacation(userDate)));//Note to change to itsVacation(userDate)
            map.put("day",String.valueOf(day));//Note to change to day , day is updated in itsVacation
            map.put("hour","-1");
            //Toast.makeText(this,map.get("day").toString(),Toast.LENGTH_SHORT).show();
            expectSpotForHour(map,0,true,null);//TOODO
            //expectForArea(map,00,true,null);//TOODO
        }
        else{
         //building
            map.put("area","null");//Note to change to userText
            map.put("vacation",String.valueOf(itsVacation(userDate)));//Note to change to itsVacation(userDate)
            map.put("day",String.valueOf(day));//Note to change to day , day is updated in itsVacation
            map.put("hour","-1");
            expectSpotForHour(map,0,false,userText);
        }
    }

//area= if true its by area not by building , if false : buildingStr containt the chosen building
    //if false: we will the area field int the map not just the hour field (as the area func behaves )
    //call it with hour 0;
    @SuppressLint("ResourceAsColor")
    private void expectSpotForHour(final Map<String,Object> map , final int  hour, final boolean area, final String buildingStr){
        if(hour>23){

            TextView loadingMsg=(TextView) findViewById(R.id.loadingMsg);
            loadingMsg.setText(" ");
            loadingMsg.setVisibility(View.GONE);
            //Toast.makeText(ShowTableActivity.this ,"DONE "+buildingStr,Toast.LENGTH_SHORT).show();
            //here we will print the table

            TextView beforeTableMsg=(TextView)findViewById(R.id.tableMsg);
            beforeTableMsg.setText("Expected status of the date:");
            loadingMsg.setVisibility(View.VISIBLE);

            if(buildingStr == null){//area
                TableRow row1=new TableRow(this);
                TextView hourView1=new TextView(this);
                hourView1.setText("Hour");
                TextView resultView1=new TextView(this);
                resultView1.setText("           Full or not");
                resultView1.setGravity(Gravity.CENTER);
                hourView1.setGravity(Gravity.CENTER);
                resultView1.setTypeface(null, Typeface.BOLD);
                hourView1.setTypeface(null, Typeface.BOLD);
                row1.addView(hourView1);
                row1.addView(resultView1);
                row1.setLayoutParams(new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT));
                row1.setPadding(50,50,50,5);
                row1.setDividerPadding(2);
                table.addView(row1);
                for(int i = 0; i < 24; i++){
                    TableRow row=new TableRow(this);
                    TextView hourView=new TextView(this);
                    hourView.setText(hourList.get(i)+" :");
                    TextView resultView=new TextView(this);
                    resultView.setText("            "+validOrNotResults[i]);
                    resultView.setGravity(Gravity.CENTER);
                    hourView.setGravity(Gravity.CENTER);
                    row.addView(hourView);
                    row.addView(resultView);
                    row.setLayoutParams(new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT));
                    row.setPadding(50,5,50,5);
                    //row.setDividerPadding(2);
                    table.addView(row);
                }
            }else{//building
                TableRow row1=new TableRow(this);
                TextView hourView1=new TextView(this);
                hourView1.setText("Hour");
                TextView resultView1=new TextView(this);
                resultView1.setText("      Nearest parking area with\n     valid spots");
                resultView1.setGravity(Gravity.CENTER);
                hourView1.setGravity(Gravity.CENTER);
                resultView1.setLines(2);
                resultView1.setMinLines(2);
                resultView1.setSingleLine(false);
                resultView1.setHorizontallyScrolling(false);
                resultView1.setTypeface(null, Typeface.BOLD);
                hourView1.setTypeface(null, Typeface.BOLD);
                row1.addView(hourView1);
               // row1.addView(line);
                row1.addView(resultView1);
                row1.setLayoutParams(new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT));
                row1.setPadding(50,50,50,5);
               // row1.setDividerPadding(2);

                table.addView(row1);
                for(int i = 0; i < 24; i++){
                    TableRow row=new TableRow(this);
                    TextView hourView=new TextView(this);
                    hourView.setText(hourList.get(i)+" :");
                    TextView resultView=new TextView(this);
                    resultView.setText("    "+areasResults[i]);
                    resultView.setGravity(Gravity.CENTER);
                    hourView.setGravity(Gravity.CENTER);
                    row.addView(hourView);
                    //row.addView(line);
                    row.addView(resultView);
                    row.setLayoutParams(new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT));
                    row.setPadding(50,5,50,5);
                    row.setDividerPadding(2);
                    table.addView(row);
                }
            }
            table.setVisibility(View.VISIBLE);
            return;
        }
        map.put("hour",String.valueOf(hour));
        db = FirebaseFirestore.getInstance();
        if (area==true) {
            expectForArea(map,hour,area,buildingStr);
        }else {
            DocumentReference dc=db.collection("buildings").document(buildingStr);
            dc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()){
                        DocumentSnapshot documentSnapshot=task.getResult();
                        if(documentSnapshot!=null){
                            final ArrayList<String> parkingAreas = (ArrayList<String>)documentSnapshot.get("parkingAreas");
                            assert (parkingAreas != null);
                            DocumentReference dc1=db.collection("trainingSet").document("array_doc");
                            dc1.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if(task.isSuccessful()){
                                        DocumentSnapshot documentSnapshot1=task.getResult();
                                        if(documentSnapshot1!=null){
                                            ArrayList<Map<String, Object>> examplesArray = (ArrayList<Map<String, Object>>) documentSnapshot1.get("array");
                                            //Toast.makeText(ShowTableActivity.this,"examplesArray Size= "+examplesArray.size(),Toast.LENGTH_SHORT).show();
                                            assert (examplesArray != null);
                                            for(int i=0 ; i<parkingAreas.size(); i++){
                                                map.put("area",parkingAreas.get(i));
                                                examplesArray = (ArrayList<Map<String, Object>>) documentSnapshot1.get("array");
                                                //Toast.makeText(ShowTableActivity.this,"examplesArray Size= "+examplesArray.size(),Toast.LENGTH_SHORT).show();
                                                assert (examplesArray != null);
                                                //Toast.makeText(ShowTableActivity.this,"examplesArray Size2= "+examplesArray.size(),Toast.LENGTH_SHORT).show();

                                                ArrayList<Map<String, Object>> kList = getKList(examplesArray, map);//Note to Change


                                                //Toast.makeText(ShowTableActivity.this,"klist Size= "+kList.size(),Toast.LENGTH_LONG).show();
                                                /*Toast.makeText(ShowTableActivity.this,
                                                        "kList \n"+String.valueOf(1)+"area: "+kList.get(1).get("area")
                                                                     +"\n day: "+kList.get(j).get("day")+
                                                                    "\n hour: "+kList.get(j).get("hour"),Toast.LENGTH_SHORT).show() ;
                                                */
                                                /*for(int j=0;j<kList.size();j++){

                                                    Toast.makeText(ShowTableActivity.this,
                                                            "kList \n"+String.valueOf(j)+"area: "+kList.get(j).get("area")
                                                                     +"\n day: "+kList.get(j).get("day")+
                                                                    "\n hour: "+kList.get(j).get("hour"),Toast.LENGTH_SHORT).show() ;
                                                }*/
                                                //if(parkingAreas)
                                                if(expectValid(kList)){
                                                   //Note Toast or to print in document ;
                                                    areasResults[hour] = parkingAreas.get(i);
                                                    expectSpotForHour(map,hour+1,area,buildingStr);
                                                   return ;
                                                }
                                            }
                                            //Note None , no valid spot for this hour , check for the next hour
                                            areasResults[hour] = "None";

                                            expectSpotForHour(map,hour+1,area,buildingStr);
                                        }
                                    }
                                }
                            });
                        }
                    }
                }
            });

        }

    }
    //buildingStr should be passed as null and removed from the function
    private void expectForArea(final Map<String,Object> map ,final int  hour,final boolean area,final String buildingStr){
        DocumentReference dc = db.collection("trainingSet").document("array_doc");
        dc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null) {
                        ArrayList<Map<String, Object>> examplesArray = (ArrayList<Map<String, Object>>) document.get("array");
                        assert (examplesArray != null);

                        //Toast.makeText(ShowTableActivity.this,"examplesArray "+hour+" length "+examplesArray.size(),Toast.LENGTH_SHORT).show();//TOODO

                        ArrayList<Map<String, Object>> kList = getKList(examplesArray, map);//Note to Change


                        if (expectValid(kList) == true) {
                            validOrNotResults[hour] = "Not full";
                            //Toast.makeText(ShowTableActivity.this, "true " + String.valueOf(hour), Toast.LENGTH_SHORT).show();

                        } else {
                                validOrNotResults[hour] = "full";
                        //    Toast.makeText(ShowTableActivity.this, "false " + String.valueOf(hour), Toast.LENGTH_SHORT).show();

                        }
                        expectSpotForHour(map, hour + 1, area,buildingStr);
                    } else {
                        Log.d("LOGGER", "No such document");
                    }
                } else {
                    Log.d("LOGGER", "get failed with ", task.getException());
                }
            }
        });
    }
    private boolean expectValid(ArrayList<Map<String,Object>>  kList){
        int validNum=0;
        for (int i=0;i<kList.size() ;i++){
            if (kList.get(i).get("validSpots").equals("true")){
                validNum++;
            }
        }
        if(validNum> (kList.size()/2)){
            return true;
        }
        return false ;
    }

    private ArrayList<Map<String,Object>> getKList(ArrayList<Map<String,Object>>  examplesArray,Map<String,Object> map){//TOODO
        ArrayList<Map<String,Object>> kList=new ArrayList<>();
        int k=3;
        double min;
/*
        Toast.makeText(ShowTableActivity.this,
                "map: area "+map.get("area")+"day "+map.get("day")
                        +"hour "+map.get("hour")+"vac "+map.get("vacation")
                        +"validSpots " +map.get("validSpots"),Toast.LENGTH_SHORT).show() ;*/
        Map<String,Object>  minMap;


        assert (examplesArray.size()>=k);
        while (k>0){
            min=2;
            minMap =new HashMap<>();
            for(int i=0 ; i<examplesArray.size() ; i++){
                double distance=calculateDistance(map,examplesArray.get(i));

                if ( k==3 && map.get("hour").equals("20")) {
                        Toast.makeText(ShowTableActivity.this,
                                "examplesArray \n"+String.valueOf(i)+"area: "+examplesArray.get(i).get("area")
                                        +"\n day: "+examplesArray.get(i).get("day")+
                                        "\n hour: "+examplesArray.get(i).get("hour")+
                                        "\n distance: "+ distance,Toast.LENGTH_SHORT).show() ;
                }

                /*if(k==3 &&hour==20){
                     Toast.makeText(ShowTableActivity.this,
                        "examplesArray "+String.valueOf(i)+"area: "+examplesArray.get(i).get("area")
                                +"\n day: "+examplesArray.get(i).get("day")+
                                "\n hour: "+examplesArray.get(i).get("hour")+" distance: "
                               +String.valueOf(distance),Toast.LENGTH_SHORT).show() ;


                }*/

                if (distance<min){
                    min=distance;
                    minMap=examplesArray.get(i);

                }
            }
            kList.add(minMap);
            examplesArray.remove(minMap);
            k--;
        }
/*
        if (map.get("hour").equals("20")) {
            for (int i = 0; i < kList.size(); i++) {

                Toast.makeText(ShowTableActivity.this,
                        "kList \n"+String.valueOf(i)+"area: "+kList.get(i).get("area")
                                +"\n day: "+kList.get(i).get("day")+
                                "\n hour: "+kList.get(i).get("hour"),Toast.LENGTH_SHORT).show() ;

            }
        }*/
        return kList;
    }
    private double calculateDistance(Map<String,Object> map1,Map<String,Object> map2){
        double sum=0;
       // String area=map2.get("area").toString();

        if(map1.get("hour").equals("20") && map2.get("area").equals("2")){
            Toast.makeText(ShowTableActivity.this,
                    "map1 \n area: "+map1.get("area")
                            +"\n day: "+map1.get("day")+
                            "\n hour: "+map1.get("hour")
                            +"\n vacation: "+map1.get("vacation")+
                    "\nmap2 \n area: "+map2.get("area")
                            +"\n day: "+map2.get("day")+
                            "\n hour: "+map2.get("hour")
                            +"\n vacation: "+map2.get("vacation"),Toast.LENGTH_SHORT).show() ;
            Toast.makeText(ShowTableActivity.this,
                    "\n area: "+map1.get("area").equals(map2.get("area"))
                            +"\n day: "+map1.get("day").equals(map2.get("day"))+
                            "\n hour: "+map1.get("hour").equals(map2.get("hour"))
                            +"\n vacation: "+map1.get("vacation").equals(map2.get("vacation"))
                           ,Toast.LENGTH_SHORT).show() ;

        }

        if(map1.get("area").equals(map2.get("area"))){
            sum=sum+3;
        }

/*
        int day1=Integer.parseInt(map1.get("day").toString());
        String khara=map2.get("day").toString();
        int day2=Integer.parseInt(khara);
         boolean con=map2.containsKey("day");
        //int day2=(int)map2.get("day");*/
        if(map1.get("day").equals(map2.get("day"))){
            sum=sum+2;

        }
        if(map1.get("hour").equals(map2.get("hour"))){
            sum=sum+1;
        }
        if( map1.get("vacation").equals(map2.get("vacation"))){
            sum=sum+2;
        }
        /*
        sum=sum+ (map1.get("day").equals(map2.get("day")) ? 2: 0);
        sum=sum+ (map1.get("hour").equals(map2.get("hour")) ? 1: 0);
        sum=sum+ (map1.get("vacation").equals(map2.get("vacation")) ? 2: 0);*/
       // Toast.makeText(ShowTableActivity.this,"area: "+map2.get("area")+" "+String.valueOf(sum),Toast.LENGTH_SHORT).show();
        sum =1-(sum/8);
        return sum;
    }

    //userDate=day/month format
    // we didnt consider jewish ceremonies
    public static boolean itsVacation(String userDate){
        String myFormat = "dd/MM/yyyy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        try {
            Date date1 = sdf.parse(userDate);
            Date summerStart= sdf.parse("01/08/2020");
            Date summerEnd= sdf.parse("20/10/2020");
            Date examsSpringStart= sdf.parse("01/07/2020");
            Date examsSpringEnd= sdf.parse("20/10/2020");
            Date examWinterStart= sdf.parse("26/01/2021");
            Date examWinterEnd= sdf.parse("17/03/2021");
            Calendar cal=Calendar.getInstance();
            cal.setTime(date1);
            day=cal.get(Calendar.DAY_OF_WEEK);
            //Toast.makeText(ShowTableActivity.this,String.valueOf(day),Toast.LENGTH_SHORT).show();
            //if (date1.equals(summerStart)) Toast.makeText(ShowTableActivity.this,"yoofi",Toast.LENGTH_SHORT).show();
            if((date1.compareTo(summerStart)>=0 && summerEnd.compareTo(date1)>=0 )
                || (date1.compareTo(examsSpringStart)>=0 && examsSpringEnd.compareTo(date1)>=0)
                || (date1.compareTo(examWinterStart)>=0 && examWinterEnd.compareTo(date1)>=0)
                || day==6 || day==7 ){

                return true;
            }
        }catch(ParseException e){
            e.printStackTrace();
        }
        return false;
    }

    public void goBack(View view){
            super.onBackPressed();


    }

}
