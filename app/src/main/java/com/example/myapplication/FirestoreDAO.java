package com.example.myapplication;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FirestoreDAO {
    private static final String COLLECTION = "Games";
    private final FirebaseFirestore db;
    private ListenerRegistration registration;

    public FirestoreDAO() {
        db = FirebaseFirestore.getInstance();
    }

    public void createDocument(Games game, IFirestoreCallback callback) {
        db.collection(COLLECTION).document(game.getName()).get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        callback.onOperationComplete(false, "A game with this name already exists");
                        return;
                    }
                    db.collection(COLLECTION).document(game.getName()).set(game)
                            .addOnSuccessListener(unused -> callback.onOperationComplete(true, "Game added"))
                            .addOnFailureListener(e -> callback.onOperationComplete(false, "Failed to add game"));
                })
                .addOnFailureListener(e -> callback.onOperationComplete(false, "Failed to add game"));
    }

    public void readAllDocuments(IFirestoreCallback callback) {
        db.collection(COLLECTION).get()
                .addOnSuccessListener(snapshot -> callback.onDataReceived(toList(snapshot)))
                .addOnFailureListener(e -> callback.onDataReceived(new ArrayList<>()));
    }

    public void readDocumentById(String name, IFirestoreCallback callback) {
        db.collection(COLLECTION).document(name).get()
                .addOnSuccessListener(snapshot -> {
                    ArrayList<Games> list = new ArrayList<>();
                    Games game = snapshot.toObject(Games.class);
                    if (game != null) {
                        list.add(game);
                    }
                    callback.onDataReceived(list);
                })
                .addOnFailureListener(e -> callback.onDataReceived(new ArrayList<>()));
    }

    public void readHoursMoreThan(double hours, IFirestoreCallback callback) {
        db.collection(COLLECTION).whereGreaterThan("hours", hours).get()
                .addOnSuccessListener(snapshot -> callback.onDataReceived(toList(snapshot)))
                .addOnFailureListener(e -> callback.onDataReceived(new ArrayList<>()));
    }

    public void readHoursLessThan(double hours, IFirestoreCallback callback) {
        db.collection(COLLECTION).whereLessThan("hours", hours).get()
                .addOnSuccessListener(snapshot -> callback.onDataReceived(toList(snapshot)))
                .addOnFailureListener(e -> callback.onDataReceived(new ArrayList<>()));
    }

    public void readByPriority(int priority, IFirestoreCallback callback) {
        db.collection(COLLECTION).whereEqualTo("priority", priority).get()
                .addOnSuccessListener(snapshot -> callback.onDataReceived(toList(snapshot)))
                .addOnFailureListener(e -> callback.onDataReceived(new ArrayList<>()));
    }

    public void updateDocument(String name, Integer priority, Double hours, String imageBase64, IFirestoreCallback callback) {
        Map<String, Object> updates = new HashMap<>();
        if (priority != null) {
            updates.put("priority", priority);
        }
        if (hours != null) {
            updates.put("hours", hours);
        }
        if (imageBase64 != null) {
            updates.put("imageBase64", imageBase64);
        }
        db.collection(COLLECTION).document(name).update(updates)
                .addOnSuccessListener(unused -> callback.onOperationComplete(true, "Game updated"))
                .addOnFailureListener(e -> callback.onOperationComplete(false, "Game not found or update failed"));
    }

    public void deleteDocument(String name, IFirestoreCallback callback) {
        db.collection(COLLECTION).document(name).get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.exists()) {
                        callback.onOperationComplete(false, "Game not found");
                        return;
                    }
                    db.collection(COLLECTION).document(name).delete()
                            .addOnSuccessListener(unused -> callback.onOperationComplete(true, "Game deleted"))
                            .addOnFailureListener(e -> callback.onOperationComplete(false, "Failed to delete game"));
                })
                .addOnFailureListener(e -> callback.onOperationComplete(false, "Failed to delete game"));
    }

    public void listenToAllDocumentsChange(IFirestoreCallback callback) {
        stopListening();
        registration = db.collection(COLLECTION).addSnapshotListener((snapshot, error) -> {
            if (error != null || snapshot == null) {
                return;
            }
            callback.onDataReceived(toList(snapshot));
        });
    }

    public void stopListening() {
        if (registration != null) {
            registration.remove();
            registration = null;
        }
    }

    private ArrayList<Games> toList(QuerySnapshot snapshot) {
        ArrayList<Games> list = new ArrayList<>();
        for (DocumentSnapshot document : snapshot.getDocuments()) {
            Games game = document.toObject(Games.class);
            if (game != null) {
                list.add(game);
            }
        }
        return list;
    }
}