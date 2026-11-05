package com.example.myapplication;

public interface IUserCallback {
    default void onUserReceived(Users user) {
    }

    default void onOperationComplete(boolean success, String message) {
    }
}
