package com.example.myspot;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    public static final String EXTRA_MESSAGE = "com.example.mySpot.MESSAGE";
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    public static  GoogleSignInClient mGoogleSignInClient;
    int RC_SIGN_IN = 0;
    private static final String TAG = "FirebaseAuth:";
    private  FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){ // Note: the user already signed in.
            checkIncompleteInformation(currentUser.getEmail());
        }
    }

    public void labelButtonPressed(View view) {
        signIn();
    }

    public void visitorButtonPressed(View view) {
        //signIn();

        Intent intent = new Intent(this, LabelHomePageActivity.class);
        intent.putExtra("isVisitor",true);
        startActivityForResult(intent,1);

    }
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            firebaseAuthWithGoogle(account);
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w("error", "signInResult:failed code=" + e.getStatusCode());
            //Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            Toast.makeText(this, "You didn't sign in", Toast.LENGTH_SHORT).show();

        }
    }
    private void firebaseAuthWithGoogle(final GoogleSignInAccount acct) {//called when the button IHaveLabel is pressed
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) { // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");

                            boolean checkUserFirstTime = task.getResult().getAdditionalUserInfo().isNewUser();
                            if(checkUserFirstTime){
                                //Note: add email address as new document in users collection

                                Map<String,Object> user = new HashMap<>();
                                if(acct.getDisplayName() != null){
                                    user.put("name", acct.getDisplayName());
                                }
                                user.put("labelColor", ""); // this is a must
                                user.put("dorm", "");
                                user.put("phoneNo", "");
                                user.put("accessible", "No");

                                DocumentReference dc= db.collection("users").document(acct.getEmail());
                                dc.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
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
                            }
                            checkIncompleteInformation(acct.getEmail());

                        } else {
                            Toast.makeText(MainActivity.this, "Sorry auth failed.", Toast.LENGTH_SHORT).show();
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                        }
                    }
                });
    }

    public void checkIncompleteInformation(String email){
        DocumentReference dc = db.collection("users").document(email);
        dc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null) {
                        String emptyStr="";
                        if(document.getData()==null){
                            //return;
                        }
                        String strFromDoc=document.getString("labelColor");
                        if( emptyStr.equals(strFromDoc)){
                            Toast.makeText(getBaseContext(),"MUST COMPLETE THE INFORMATION",Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(MainActivity.this, LogInActivity.class);
                            startActivity(intent);                        }
                        else{

                            Intent intent = new Intent(MainActivity.this, LabelHomePageActivity.class);
                            intent.putExtra("isVisitor",false);
                            startActivityForResult(intent,1);
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

}
