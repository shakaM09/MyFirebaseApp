package com.example.myapplication;

import java.util.ArrayList;

public interface IFirestoreCallback {
    default void onDataReceived(ArrayList<Games> games) {
    }

    default void onOperationComplete(boolean success, String message) {
    }
}