package com.example.myapplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.widget.ImageView;
import android.widget.PopupMenu;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileBarHelper {

    private static final int MENU_NAME = 1;
    private static final int MENU_SIGN_OUT = 2;

    public static void attach(AppCompatActivity activity, ImageView ivProfile) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) {
            goToAuth(activity);
            return;
        }
        String[] displayName = {firebaseUser.getEmail()};
        new UserDAO().readUser(firebaseUser.getUid(), new IUserCallback() {
            @Override
            public void onUserReceived(Users user) {
                if (user == null) {
                    return;
                }
                displayName[0] = user.getFirstName() + " " + user.getLastName();
                Bitmap bitmap = ImageUtils.base64ToBitmap(user.getImageBase64());
                if (bitmap != null) {
                    ivProfile.setImageBitmap(bitmap);
                }
            }
        });
        ivProfile.setOnClickListener(view -> {
            PopupMenu popup = new PopupMenu(activity, view);
            popup.getMenu().add(0, MENU_NAME, 0, displayName[0]).setEnabled(false);
            popup.getMenu().add(0, MENU_SIGN_OUT, 1, "Sign out");
            popup.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == MENU_SIGN_OUT) {
                    FirebaseAuth.getInstance().signOut();
                    goToAuth(activity);
                }
                return true;
            });
            popup.show();
        });
    }

    private static void goToAuth(AppCompatActivity activity) {
        Intent intent = new Intent(activity, AuthActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intent);
        activity.finish();
    }
}
