package com.example.myapplication;

import com.google.firebase.firestore.FirebaseFirestore;

public class UserDAO {
    private static final String COLLECTION = "Users";
    private final FirebaseFirestore db;

    public UserDAO() {
        db = FirebaseFirestore.getInstance();
    }

    public void createUser(Users user, IUserCallback callback) {
        db.collection(COLLECTION).document(user.getUid()).set(user)
                .addOnSuccessListener(unused -> callback.onOperationComplete(true, "Profile saved"))
                .addOnFailureListener(e -> callback.onOperationComplete(false, "Failed to save profile"));
    }

    public void readUser(String uid, IUserCallback callback) {
        db.collection(COLLECTION).document(uid).get()
                .addOnSuccessListener(snapshot -> {
                    Users user = snapshot.exists() ? snapshot.toObject(Users.class) : null;
                    callback.onUserReceived(user);
                })
                .addOnFailureListener(e -> callback.onOperationComplete(false, "Could not load profile"));
    }
}
