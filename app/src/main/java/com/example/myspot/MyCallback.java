package com.example.myspot;

import com.google.firebase.firestore.DocumentSnapshot;

public interface MyCallback {
    void onCallback( DocumentSnapshot document);

}
