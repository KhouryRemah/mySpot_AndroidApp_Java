package com.example.myspot;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.sql.Connection;
import java.time.OffsetTime;
import java.util.ArrayList;
import java.util.List;

import static com.google.android.gms.common.util.CollectionUtils.listOf;


public class ProfileFragment extends Fragment {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String dormStr,labelStr,accessibleStr,phoneNoStr;
    DocumentSnapshot currentDocument2; //Note elias zabbat
    private String _dorm,_label,_accessible,_phoneNo;

    public ProfileFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_profile, container, false);
        mAuth  = FirebaseAuth.getInstance();

        TextView textView = (TextView)view.findViewById(R.id.welcome);
        textView.setText(printWelcomeMessage());
        TextView nameView= view.findViewById(R.id.nameOfUser);
        nameView.setText(mAuth.getCurrentUser().getDisplayName());

        db = FirebaseFirestore.getInstance();

        updateCurrentDocument(view);

        setListenersToSpinners(view);

        ImageButton refreshButton = (ImageButton) view.findViewById(R.id.refreshButton);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateCurrentDocument(view);
            }
        });


        Button submitButton = (Button) view.findViewById(R.id.submitButton);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                EditText phoneView=view.findViewById(R.id.numOfUser);
                phoneNoStr=phoneView.getText().toString();
                if (_phoneNo.equals(phoneNoStr)
                        && _accessible.equals(accessibleStr) && _dorm.equals(dormStr)
                        && _label.equals(labelStr)){
                    Toast.makeText(getActivity(),"Nothing has changed",Toast.LENGTH_SHORT).show();
                }

                else {
                    boolean isValid = phoneNoStr.matches("[0-9]+");
                    if(phoneNoStr.length()!=10 || !isValid){
                        phoneView.setError("Illegal phone number");
                        phoneView.requestFocus();
                        //Toast.makeText(getActivity(),"Illegal phone number",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if(dormStr.equals("Choose one")){
                        Toast.makeText(getActivity(),"Please choose a dorm",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if(labelStr.equals("Choose one")){
                        Toast.makeText(getActivity(),"Please choose a label color",Toast.LENGTH_SHORT).show();
                        return;
                    }

                    currentDocument2.getReference().update("phoneNo",phoneView.getText().toString() //Elias zabbat
                            ,"accessible",accessibleStr,"dorm",dormStr,"labelColor",labelStr );
                    Toast.makeText(getActivity(),"Submitted",Toast.LENGTH_SHORT).show();

                    updateCurrentDocument(view);
                    if(getActivity() instanceof LogInActivity){
                        Intent intent = new Intent(getActivity(), LabelHomePageActivity.class);
                        startActivity(intent);
                    }
                }
            }
        });
        return view;
    }

    public void updateCurrentDocument(final View view ) {

        mAuth = FirebaseAuth.getInstance();
        final FirebaseUser currentUser = mAuth.getCurrentUser();
        //db = FirebaseFirestore.getInstance();
        DocumentReference dc = db.collection("users").document(currentUser.getEmail());
        dc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null) {
                        readFromFirebase(view,document);
                        currentDocument2 = document;
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

    public void readFromFirebase(View view, DocumentSnapshot currentDocument) {

        mAuth  = FirebaseAuth.getInstance();
        final FirebaseUser currentUser = mAuth.getCurrentUser();

        //TextView changeText = (TextView) view.findViewById(R.id.nameOfUser);
        //changeText.setText(currentDocument.getString("name"));
        TextView changeText = (TextView) view.findViewById(R.id.emailOfUser);
        changeText.setText(currentUser.getEmail());
        changeText = (TextView) view.findViewById(R.id.numOfUser);
        if(!currentDocument.getString("phoneNo").equals("")){
            changeText.setText(currentDocument.getString("phoneNo"));
        }
        _phoneNo=currentDocument.getString("phoneNo");

        Spinner spinner = (Spinner) view.findViewById(R.id.dorms);
        List<String> list=new ArrayList<>();
        String temp="";
        if(!temp.equals(currentDocument.getString("dorm"))){
            list.add(currentDocument.getString("dorm"));
        }
        else{
            list.add("Choose one");
        }
        String[] tempArr={"Lower","Upper","Senate","Neve America","Kfar Hasmacha"
                ,"Canada","Eastern","Segel Zutar","Kfar Meshtalmim","Kessel","None"};
        for(int i=0 ; i<11; i++){
            if(!tempArr[i].equals(currentDocument.getString("dorm")))list.add(tempArr[i]);
        }
        ArrayAdapter<String> adapter=new ArrayAdapter<String>(getActivity() ,R.layout.spinneritem,list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        _dorm=currentDocument.getString("dorm");

        Spinner spinner1=(Spinner)view.findViewById(R.id.labelColors);
        List<String> list1=new ArrayList<>();
        String temp1="";
        if(!temp1.equals(currentDocument.getString("labelColor"))){
            list1.add(currentDocument.getString("labelColor"));
        }else{
            list1.add("Choose one");
        }
        String[] tempArr1={"Red","Red & Gold","Red & Green","Green","Blue"
                ,"White","Blue & White","Blue & White Asat","Orange & White","Orange","Motorcycle"};
        for(int i=0 ; i<11; i++){
            if(!tempArr1[i].equals(currentDocument.getString("labelColor")))list1.add(tempArr1[i]);
        }
        ArrayAdapter<String> adapter1=new ArrayAdapter<String>(getActivity() ,R.layout.spinneritem,list1);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner1.setAdapter(adapter1);
        _label=currentDocument.getString("labelColor");

        Spinner spinner2=(Spinner)view.findViewById(R.id.accOfUser);
        List<String> list2=new ArrayList<>();
        list2.add(currentDocument.getString("accessible"));
        String[] tempArr2={"Yes","No"};
        for(int i=0 ; i<2; i++){
            if(!tempArr2[i].equals(currentDocument.getString("accessible")))list2.add(tempArr2[i]);
        }
        ArrayAdapter<String> adapter2=new ArrayAdapter<String>(getActivity() ,R.layout.spinneritem,list2);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner2.setAdapter(adapter2);
        _accessible=currentDocument.getString("accessible");
    }


    public void setListenersToSpinners(View view) {
        Spinner dormsSpinner=view.findViewById(R.id.dorms);
        dormsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                dormStr=parent.getItemAtPosition(position).toString();
                //Toast.makeText(getActivity(),dormStr,Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        Spinner accessibleSpinner=view.findViewById(R.id.accOfUser);
        accessibleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                accessibleStr=parent.getItemAtPosition(position).toString();
                //Toast.makeText(getActivity(),accessibleStr,Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        Spinner labelSpinner=view.findViewById(R.id.labelColors);
        labelSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                labelStr=parent.getItemAtPosition(position).toString();
                //Toast.makeText(getActivity(),labelStr,Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });



    }
/*
    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }*/

    /*@Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }*/

  /*  @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
*/
    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
   /* public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }*/

   public String printWelcomeMessage(){
       String message = mAuth.getCurrentUser().getDisplayName();
       //TextView textView =  (TextView)findViewById(R.id.textView);
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
           //Toast.makeText(getBaseContext(),message,Toast.LENGTH_SHORT).show();
           //this.setTitle("                                        MySpot" );
       }
       return message;

   }

}
