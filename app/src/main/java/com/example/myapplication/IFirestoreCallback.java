package com.example.myapplication;

import java.util.ArrayList;

public interface IFirestoreCallback {
    void onDataReceived(ArrayList<Players> students);
}
