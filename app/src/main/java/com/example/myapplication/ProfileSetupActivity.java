package com.example.myapplication;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Locale;

public class ProfileSetupActivity extends AppCompatActivity {

    private UserDAO userDAO;
    private ImageView ivProfile;
    private EditText etFirstName, etLastName, etBirthday;
    private Button btnSave;
    private Bitmap selectedBitmap;
    private Uri cameraUri;

    private final ActivityResultLauncher<Uri> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
                if (success && cameraUri != null) {
                    try {
                        setImage(ImageUtils.loadScaledBitmap(ProfileSetupActivity.this, cameraUri));
                    } catch (IOException | NullPointerException e) {
                        Toast.makeText(ProfileSetupActivity.this, "Could not load image", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    private final ActivityResultLauncher<String> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    try {
                        setImage(ImageUtils.loadScaledBitmap(ProfileSetupActivity.this, uri));
                    } catch (IOException | NullPointerException e) {
                        Toast.makeText(ProfileSetupActivity.this, "Could not load image", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile_setup);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ivProfile = findViewById(R.id.ivProfile);
        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etBirthday = findViewById(R.id.etBirthday);
        btnSave = findViewById(R.id.btnSave);

        userDAO = new UserDAO();

        ivProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(ProfileSetupActivity.this)
                        .setTitle("Add a picture")
                        .setItems(new String[]{"Camera", "Gallery"}, (dialog, which) -> {
                            if (which == 0) {
                                cameraUri = createCameraUri();
                                cameraLauncher.launch(cameraUri);
                            } else {
                                galleryLauncher.launch("image/*");
                            }
                        })
                        .show();
            }
        });

        etBirthday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar calendar = Calendar.getInstance();
                DatePickerDialog dialog = new DatePickerDialog(ProfileSetupActivity.this,
                        (picker, year, month, day) -> etBirthday.setText(String.format(Locale.US, "%02d/%02d/%04d", day, month + 1, year)),
                        calendar.get(Calendar.YEAR) - 18, calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
                dialog.getDatePicker().setMaxDate(System.currentTimeMillis());
                dialog.show();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String firstName = etFirstName.getText().toString().trim();
                String lastName = etLastName.getText().toString().trim();
                String birthday = etBirthday.getText().toString().trim();
                if (firstName.isEmpty() || lastName.isEmpty() || birthday.isEmpty()) {
                    Toast.makeText(ProfileSetupActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (selectedBitmap == null) {
                    Toast.makeText(ProfileSetupActivity.this, "Please add a profile picture", Toast.LENGTH_SHORT).show();
                    return;
                }
                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                if (firebaseUser == null) {
                    Intent intent = new Intent(ProfileSetupActivity.this, AuthActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    return;
                }
                btnSave.setEnabled(false);
                Users user = new Users(firebaseUser.getUid(), firebaseUser.getEmail(), firstName, lastName, birthday,
                        ImageUtils.bitmapToBase64(selectedBitmap));
                userDAO.createUser(user, new IUserCallback() {
                    @Override
                    public void onOperationComplete(boolean success, String message) {
                        Toast.makeText(ProfileSetupActivity.this, message, Toast.LENGTH_SHORT).show();
                        if (success) {
                            Intent intent = new Intent(ProfileSetupActivity.this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        } else {
                            btnSave.setEnabled(true);
                        }
                    }
                });
            }
        });
    }

    private void setImage(Bitmap bitmap) {
        selectedBitmap = ImageUtils.scaleDown(bitmap, 320);
        ivProfile.setImageBitmap(selectedBitmap);
    }

    private Uri createCameraUri() {
        File dir = new File(getCacheDir(), "images");
        dir.mkdirs();
        File file = new File(dir, "profile_" + System.currentTimeMillis() + ".jpg");
        return FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", file);
    }
}
