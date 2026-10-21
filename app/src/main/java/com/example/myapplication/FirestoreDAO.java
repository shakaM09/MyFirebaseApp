package com.example.myapplication;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FirestoreDAO {
    private static final String COLLECTION_NAME = "players";


    public void createDocument(String name, String number, int elo) {
        Map<String, Object> playerRecord = new HashMap<>();
        playerRecord.put("fullName", name);
        playerRecord.put("number", number);
        playerRecord.put("elo", elo);
        DocumentReference docRef = FirebaseFirestore.getInstance().
                collection(COLLECTION_NAME).document(number);


        docRef.set(playerRecord).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.d("DAO_FIREBASE","SUCCESSFUL WRITE!");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("DAO_FIREBASE",e.getMessage().toString());
            }
        });

    }
    public void readDocumentById(String docID, IFirestoreCallback iFirestoreCallback){
        DocumentReference docRef = FirebaseFirestore.getInstance().collection(COLLECTION_NAME).document(docID);

        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Players player = documentSnapshot.toObject(Players.class);
                Log.d("DAO_FIREBASE","SUCCESSFUL READ!" + player);
                ArrayList<Players> players= new ArrayList<>();
                players.add(player);
                iFirestoreCallback.onDataReceived(players);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("DAO_FIREBASE","onFailure");
            }
        });
    }
    public void readAllDocuments(IFirestoreCallback iFirestoreCallback){
        CollectionReference colRef= FirebaseFirestore.getInstance().collection(COLLECTION_NAME);
        colRef.whereGreaterThan("elo",1000).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                ArrayList<Players> players= new ArrayList<>();
                for(DocumentSnapshot docSS : queryDocumentSnapshots.getDocuments()){
                   Players player = docSS.toObject(Players.class);
                    Log.d("DAO_FIREBASE", "SUCCESSFUL READ!" + player);
                   players.add(player);
                }
                iFirestoreCallback.onDataReceived(players);
            }
        });
    }

    public void updateDocument(String documentId, int newElo) {

        DocumentReference docRef = FirebaseFirestore.getInstance().collection(COLLECTION_NAME).document(documentId);
        docRef.update("elo",newElo).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.d("DAO_FIREBASE","SUCCESSFUL UPDATE!");
            }
        });

    }
    public void deleteDocument(String documentId){
        DocumentReference docRef= FirebaseFirestore.getInstance().collection(COLLECTION_NAME).document(documentId);
        docRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.d("DAO_FIREBASE","SUCCESSFUL DELETE!");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("DAO_FIREBASE", e.getMessage());
            }
        });
    }
    public void listenToADocument(String docId){
        DocumentReference docRef= FirebaseFirestore.getInstance().collection(COLLECTION_NAME).document(docId);
        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if(value != null && value.exists()){
                    Players player = value.toObject(Players.class);
                    Log.d("CHANGED_DAO", player.toString());
                }
            }
        });

    }
    private ListenerRegistration listenerRegistration;
    public void listenToAllDocumentsChange(){
        CollectionReference colRef = FirebaseFirestore.getInstance().collection(COLLECTION_NAME);
        colRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if(error != null || value == null)
                    return;

                for(DocumentChange docC : value.getDocumentChanges()){
                   Players player = docC.getDocument().toObject(Players.class);

                    Log.d("DAO_MULTIPLE_CHANGES", docC.getType() + ":" + player.toString());
                }
            }
        });
    }
    public void stopListening() {
        if (listenerRegistration != null) {
            listenerRegistration.remove();
            listenerRegistration = null;
        }
    }


}
