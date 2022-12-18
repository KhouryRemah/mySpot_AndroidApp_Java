package com.example.myspot;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.protobuf.StringValue;

import java.security.PublicKey;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static android.content.ContentValues.TAG;
import static java.lang.Integer.parseInt;


public class ReachSpot extends AppCompatActivity {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private String[] buildingsArr={"450","451","452","453","454","455","456","457","458","459"};
    private String[] AreaArr={"1","2","2Licensed","3","3Licensed","4","4Licensed","Peripheral plot","inFrontOfBioInformatics"};
    private ArrayList<String> buildingsList=new ArrayList<>(Arrays.asList(buildingsArr));
    private ArrayList<String> areaList=new ArrayList<>(Arrays.asList(AreaArr));
    private String userSelection = "";
    String nearestParkingArea;
    AutoCompleteTextView symbol; //it may be area also
    String buildingStr="None";
    String areaStr = "None";
    boolean isVisitor=false;

    //Notifications:
    private String notiTime;
    public static final String CHANNEL_ID = "my_notification_channel";
    private static final String CHANNEL_NAME = "my notification channel";
    private static final String CHANNEL_DESC = "my notification channel description";
    public static final String CHANNEL_ID2 = "my_notification_channel2";
    private static final String CHANNEL_NAME2 = "my notification channel2";
    private static final String CHANNEL_DESC2 = "my notification channel description2";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reach_spot);
        symbol=(AutoCompleteTextView)findViewById(R.id.building);

        final ArrayAdapter<String> symbolsAdapter=new ArrayAdapter<String>(this,android.R.layout.select_dialog_item,buildingsList);
        final ArrayAdapter<String> areaAdapter=new ArrayAdapter<String>(this,android.R.layout.select_dialog_item,areaList);

        symbol.setThreshold(1);
        symbol.setAdapter(symbolsAdapter);

        Toolbar toolbar= findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        isVisitor=getIntent().getBooleanExtra("isVisitor",isVisitor);
        //Toast.makeText(this,"act : isVisitor= "+ String.valueOf(isVisitor), Toast.LENGTH_SHORT).show();


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
                    symbol.setAdapter(symbolsAdapter);
                }
            }
        });
        createNotificationChannel();
    }

    private void createNotificationChannel(){
        //notifications:
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.O){
            NotificationChannel channel=new NotificationChannel(CHANNEL_ID,CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(CHANNEL_DESC);
            channel.enableVibration(true);
            NotificationManager manager= getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);

            NotificationChannel channel2=new NotificationChannel(CHANNEL_ID2,CHANNEL_NAME2, NotificationManager.IMPORTANCE_HIGH);
            channel2.setDescription(CHANNEL_DESC2);
            channel2.enableVibration(true);
            NotificationManager manager2= getSystemService(NotificationManager.class);
            manager2.createNotificationChannel(channel2);
        }
    }
    private void sendImmediateNotification(String parkingArea){
        String title="Please pay attention";
        String body="You are going to parking area with symbol " + parkingArea +
                " in Technion, you can park here until 7:00 AM.\nPress \"Set reminder\"" +
                " if you want us to remind you to move your car to another parking area before 7:00 AM." ;
        NotificationHelper.displayNotification(this,title,body,CHANNEL_ID);
    }

    public void reachSpotButton (View view){

        if(userSelection.equals("Building")){
            buildingStr= symbol.getText().toString();
            if(!buildingsList.contains(buildingStr)){
                //Toast.makeText(ReachSpot.this,"The building you entered isn't found",Toast.LENGTH_LONG).show();
                symbol.setError("The building you entered isn't found!");
                symbol.requestFocus();
                return;
            }
            listOfNearestParkingAreas(buildingStr);
        }else if(userSelection.equals("Area")){
            areaStr= symbol.getText().toString();
            if(!areaList.contains(areaStr)){
                //Toast.makeText(ReachSpot.this,"The area you entered isn't found",Toast.LENGTH_LONG).show();
                symbol.setError("The area you entered isn't found");
                symbol.requestFocus();
                return;
            }
            areaIsChosenDirectly(areaStr);
            //Toast.makeText(ReachSpot.this,"ARIA is chosen directly done",Toast.LENGTH_LONG).show();

        }else{
            Toast.makeText(ReachSpot.this,"You must choose area or building",Toast.LENGTH_LONG).show();
        }

    }

    private void listOfNearestParkingAreas(String buildingStr){
        //db = FirebaseFirestore.getInstance();
        DocumentReference dc = db.collection("buildings").document(buildingStr);
        nearestParkingArea = "None";
        dc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null) {
                        ArrayList<String> parkingAreas = (ArrayList<String>)document.get("parkingAreas");
                        assert (parkingAreas != null);
                        selectTheNearestLegalSpot(parkingAreas,0);
                    }
                    else{
                        Log.d("LOGGER", "No such document");
                    }
                }
                else {
                    Log.d("LOGGER", "get failed with ", task.getException());
                }
            }
        });
    }

// we just found a solution to a huge bug because of the async of firebase!!!!
    private void selectTheNearestLegalSpot(final ArrayList<String> parkingAreasList,final int it){
        if (it >= parkingAreasList.size()){
            Toast.makeText(getBaseContext(),"We couldn't find any suitable parking area",Toast.LENGTH_SHORT).show();
            return;
        }
        //LAbel COlOR + VALID SPOT NUM + HOURS (yzeed htra2a 3ltlfon)

        //mAuth = FirebaseAuth.getInstance();
        String email=" ";
        if(!isVisitor){
            final FirebaseUser currentUser = mAuth.getCurrentUser();
            assert (currentUser != null && currentUser.getEmail() != null);
            email = currentUser.getEmail();
            //Toast.makeText(getBaseContext(),email,Toast.LENGTH_SHORT).show();
        }
        else{
            email="visitor";
        }
        //Toast.makeText(getBaseContext(),email+ "  "+String.valueOf(it),Toast.LENGTH_SHORT).show();


        readDoc("users",email , new MyCallback() {
            @Override
            public void onCallback(DocumentSnapshot document) {
                final String userLabelColor = document.getString("labelColor");
                final String parkingArea = (String)parkingAreasList.get(it);
                readDoc("parkingAreas", parkingArea, new MyCallback() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onCallback(DocumentSnapshot document) {
                        try {

                            if ((labelColorCheck(userLabelColor, document.getString("labelColor"))
                                    || specificHoursCheck(userLabelColor, (Map<String, String>) document.get("specificHours")))
                                    && validSpotsCheck(((Long) document.getLong("validSpotsNum")).intValue())) {

                                String day= String.valueOf(Calendar.getInstance().get(Calendar.DAY_OF_WEEK));
                                String currHour=String.valueOf(Calendar.getInstance().get(Calendar.HOUR_OF_DAY)); //24 format
                                String date=DateTimeFormatter.ofPattern("dd/MM/yyyy").format(LocalDateTime.now());
                                String vacation= String.valueOf(ShowTableActivity.itsVacation(date));

                                addTrainingSet(parkingArea,day,currHour,vacation,"true"); //increase it also when there is no valid spots.

                                if(shouldNotify(userLabelColor
                                        ,(Map<String, String>) document.get("specificHours")
                                        ,document.getString("labelColor"))){

                                    sendImmediateNotification(parkingArea);
                                }
                                reachSpecificParkingArea(parkingArea);
                            } else {
                                selectTheNearestLegalSpot(parkingAreasList, it + 1);
                            }
                        }catch (ParseException e){
                            Toast.makeText(getBaseContext(),"parse error!",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

    public boolean labelColorCheck(String userLabelColor, String parkingAreaLabelColor){
        String allColors = "All";
        if(userLabelColor.equals(parkingAreaLabelColor) || allColors.equals(parkingAreaLabelColor)){
            return true;
        }
        return false;
    }

    public boolean validSpotsCheck(int validSpotsNum){
        if(validSpotsNum > 0){
            return true;
        }
        return false;
    }

    public boolean shouldNotify(String userLabelColor,Map<String,String> specificHoursMap,String parkingAreaLabelColor) throws ParseException {
        if(labelColorCheck(userLabelColor, parkingAreaLabelColor)){
            return false;
        }
        //the user label is in specific hours.
        Calendar rightNow=Calendar.getInstance();
        int currDay=rightNow.get(Calendar.DAY_OF_WEEK);
        if (currDay==6){
            return false;
        }
        String allHoursStr=specificHoursMap.get("All");
        String labelHoursStr=specificHoursMap.get(userLabelColor);
        if(allHoursStr==null && labelHoursStr==null){
            return false;//wont get there! we will call this method only when the methods
                        //labelColorCheck or specificHourCheck returns true;
        }

        String time="";
        if(labelHoursStr!=null){
            time=labelHoursStr;
        }
        else if (allHoursStr!=null){
            time =allHoursStr;
        }

        String[] twoTimes=time.split("-");
        String argEndTime = twoTimes[1];
        notiTime=twoTimes[1];


        java.util.Date endTime = new SimpleDateFormat("HH:mm:ss").parse(argEndTime);
        Calendar endCalendar = Calendar.getInstance();
        endCalendar.setTime(endTime);


        java.util.Date morningTime = new SimpleDateFormat("HH:mm:ss").parse("07:00:00");
        Calendar morningCal=Calendar.getInstance();

        morningCal.setTime(morningTime);

        if(endTime.compareTo(morningTime)==0){
            return true;
        }
        return false;
    }
    public boolean specificHoursCheck(String userLabelColor,Map<String,String> map )throws ParseException {
        Calendar rightNow=Calendar.getInstance();
        int currDay=rightNow.get(Calendar.DAY_OF_WEEK);

        if (currDay==6 || currDay==7){
            return true;
        }
        String allHoursStr=map.get("All");
        String labelHoursStr=map.get(userLabelColor);
        if(allHoursStr==null && labelHoursStr==null){
            return false;
        }
        int currHour=rightNow.get(Calendar.HOUR_OF_DAY); //24 format
        int currMinutes=rightNow.get(Calendar.MINUTE);
        int currSeconds = rightNow.get(Calendar.SECOND);

        String time="";
        if(labelHoursStr!=null){
            time=labelHoursStr;
        }
        else if (allHoursStr!=null){
            time =allHoursStr;
        }

        //String time="11:30:00-2:00:00";
        String[] twoTimes=time.split("-");
        String argStartTime = twoTimes[0];
        String argEndTime = twoTimes[1];
        String argCurrentTime = currHour + ":" + currMinutes + ":" + currSeconds;
        //String argCurrentTime = "2:00:01";
        boolean valid = false;

        // Start Time
        java.util.Date startTime = new SimpleDateFormat("HH:mm:ss").parse(argStartTime);
        Calendar startCalendar = Calendar.getInstance();
        startCalendar.setTime(startTime);
        // Current Time
        java.util.Date currentTime = new SimpleDateFormat("HH:mm:ss").parse(argCurrentTime);
        Calendar currentCalendar = Calendar.getInstance();
        currentCalendar.setTime(currentTime);
        // End Time
        java.util.Date endTime = new SimpleDateFormat("HH:mm:ss").parse(argEndTime);
        Calendar endCalendar = Calendar.getInstance();
        endCalendar.setTime(endTime);

        if (currentTime.compareTo(endTime) < 0) {
            currentCalendar.add(Calendar.DATE, 1);
            currentTime = currentCalendar.getTime();
        }
        if (startTime.compareTo(endTime) < 0) {
            startCalendar.add(Calendar.DATE, 1);
            startTime = startCalendar.getTime();
        }
        if (currentTime.before(startTime)) {
            System.out.println(" Time is Lesser ");
            valid = false;
        } else {
            if (currentTime.after(endTime)) {
                endCalendar.add(Calendar.DATE, 1);
                endTime = endCalendar.getTime();
            }
            System.out.println("Comparing , Start Time /n " + startTime);
            System.out.println("Comparing , End Time /n " + endTime);
            System.out.println("Comparing , Current Time /n " + currentTime);
            if (currentTime.before(endTime)) {
                System.out.println("RESULT, Time lies b/w");
                valid = true;
            } else {
                valid = false;
                System.out.println("RESULT, Time does not lies b/w");
            }

        }
        return valid;
}




    public void reachSpecificParkingArea(String parkingArea) {
        DocumentReference dc = db.collection("parkingAreas").document(parkingArea);
        dc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null) {
                        GeoPoint geoPoint = document.getGeoPoint("location");
                        String name = document.getString("name");
                        assert (name != null && geoPoint != null);
                        String latitude = String.valueOf(geoPoint.getLatitude());
                        String longLatitude = String.valueOf(geoPoint.getLongitude());
                        //String uri = "waze://?ll="+latitude+","+longLatitude+"&navigate=yes";

                        String uri = "";
                        Intent intent;//Note: search for safe intent action view
                        try {//Note bnf3 on activity result mashe ll 3mara
                            uri = String.format(Locale.ENGLISH,"geo:" + latitude + "," +longLatitude + "?q=" + latitude+","+longLatitude +" ("+name+")");
                            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        } catch (ActivityNotFoundException ex) {
                            try {
                                Intent unrestrictedIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                                startActivity(unrestrictedIntent);
                            } catch (ActivityNotFoundException innerEx) {
                                //getSnackbar(getResources().getString(R.string.map_install_application), Snackbar.LENGTH_LONG).show();
                                Toast.makeText(getBaseContext(),"Please install a map application",Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                    else{
                        Log.d("LOGGER", "No such document");
                    }
                }
                else {
                    Log.d("LOGGER", "get failed with ", task.getException());
                }
            }
        });
    }

    public void areaIsChosenDirectly(final String parkingArea) {
        //LAbel COlOR + VALID SPOT NUM + HOURS (yzeed htra2a 3ltlfon)
        //FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String email=" ";
        if(!isVisitor){
            FirebaseUser currentUser = mAuth.getCurrentUser();
            assert (currentUser != null && currentUser.getEmail() != null);
            email = currentUser.getEmail();
        } else{     email="visitor";     }
        readDoc("users",email, new MyCallback() {
            @Override
            public void onCallback(DocumentSnapshot document) {
                final String userLabelColor = document.getString("labelColor");
                //Toast.makeText(getBaseContext(),"label color: "+userLabelColor,Toast.LENGTH_LONG).show();

                readDoc("parkingAreas", parkingArea, new MyCallback() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onCallback(DocumentSnapshot document) {
                        try {
                            if (!(labelColorCheck(userLabelColor, document.getString("labelColor"))
                                    || specificHoursCheck(userLabelColor, (Map<String, String>) document.get("specificHours")))){
                                Toast.makeText(getBaseContext(),"Your label color is not suitable!",Toast.LENGTH_LONG).show();
                                return;
                            }


                            String day= String.valueOf(Calendar.getInstance().get(Calendar.DAY_OF_WEEK));
                            String currHour=String.valueOf(Calendar.getInstance().get(Calendar.HOUR_OF_DAY)); //24 format
                            String date=DateTimeFormatter.ofPattern("dd/MM/yyyy").format(LocalDateTime.now());
                            String vacation= String.valueOf(ShowTableActivity.itsVacation(date));

                            if(!(validSpotsCheck(((Long) document.getLong("validSpotsNum")).intValue()))){
                                addTrainingSet(parkingArea,day,currHour,vacation,"false"); //increase it also when there is no valid spots.
                                //increaseCounterDb(String area,String day,String hour,String Vacation,String ValidSpots){

                                Toast.makeText(getBaseContext(),"The parking area is full!",Toast.LENGTH_LONG).show();

                                return;
                            }
                            addTrainingSet(parkingArea,day,currHour,vacation,"true"); //increase it also when there is no valid spots.
                            if(shouldNotify(userLabelColor
                                    ,(Map<String, String>) document.get("specificHours")
                                    ,document.getString("labelColor"))){

                                sendImmediateNotification(parkingArea);
                            }

                            reachSpecificParkingArea(parkingArea);

                        }catch (ParseException e){
                            Toast.makeText(getBaseContext(),"parse error!",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }
    private void addTrainingSet(final String area, final String day, final String hour, final String vacation, final String validSpots){
        db.collection("trainingSet").document("counter")
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.d(TAG, "task isnt successful");
                    return;
                }
                DocumentSnapshot document=task.getResult();
                assert (document!=null);
                double counter= document.getDouble("counterValue");
                counter++;
                if(counter%10==0){//add new trainingSet
                    final Map<String,Object> exampleMap=new HashMap<>();
                    exampleMap.put("area",area);
                    exampleMap.put("day",day);
                    exampleMap.put("hour",hour);
                    exampleMap.put("vacation",vacation);
                    exampleMap.put("validSpots",validSpots);

                    db.collection("trainingSet").document("array_doc")
                            .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (!task.isSuccessful()) {
                                Log.d(TAG, "task isnt successful");
                                return;
                            }
                            DocumentSnapshot document=task.getResult();
                            assert (document!=null);
                            List<Map<String,Object>> list= (List<Map<String, Object>>) document.get("array");
                            if(list.size()>20){
                                list.remove(0);
                            }
                            list.add(exampleMap);
                            Map<String,Object> mapOfList=new HashMap<>();
                            mapOfList.put("array",list);
                            db.collection("trainingSet").document("array_doc")
                                    .set(mapOfList);
                        }
                    });
                }
                Map<String,Object> map=new HashMap<>();
                map.put("counterValue",counter);
                db.collection("trainingSet").document("counter").set(map);
            }
        });
    }
    public void readDoc(String collectionStr, final String documentStr, final MyCallback myCallback){
        //db = FirebaseFirestore.getInstance();
        DocumentReference dc = db.collection(collectionStr).document(documentStr);
        dc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if (task.isSuccessful()) {
                    //Toast.makeText(getBaseContext(),"task successful",Toast.LENGTH_LONG).show();

                    DocumentSnapshot document = task.getResult();
                    if (document != null) {
                        myCallback.onCallback(document);
                    }
                    else{
                        Log.d("LOGGER", "No such document");
                    }
                }
                else {
                    //Toast.makeText(getBaseContext(),"LOGGER BUG:"+ documentStr,Toast.LENGTH_LONG).show();
                    //Toast.makeText(getBaseContext(),"LOGGER BUG:"+"get failed with "+String.valueOf(task.getException()),Toast.LENGTH_LONG).show();
                    Log.d("LOGGER", "get failed with ", task.getException());
                };;
            }
        });
    }

    public void signOut() {
        //GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if(!isVisitor){
            MainActivity. mGoogleSignInClient.signOut().addOnCompleteListener(this, new OnCompleteListener<Void>() {
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
                intent.putExtra("parent","ReachSpot");
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
