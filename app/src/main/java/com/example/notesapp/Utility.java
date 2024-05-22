package com.example.notesapp;

import android.content.Context;
import android.widget.Toast;


import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;


public class Utility {

    static void showToast(Context context,String message){
        Toast.makeText(context,message,Toast.LENGTH_SHORT).show();
    }

    static CollectionReference getCollectionReferenceForNotes(){
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();  // Obtaining the user ID
            // Returning a reference to the user's note collection
            return FirebaseFirestore.getInstance().collection("notes")
                    .document(userId).collection("my_notes");
        } else {
            // Return null or an empty collection if there is no registered user
            return null;
        }
    }


    static String timestamplToString(Timestamp timestamp){
        return new SimpleDateFormat("MM/dd/yyyy").format(timestamp.toDate());
    }


}
