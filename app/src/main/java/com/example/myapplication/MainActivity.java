package com.example.myapplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    private FirestoreDAO fsDAO;
    private ImageView ivGame;
    private EditText etName, etPriority, etHours;
    private Button btnAdd, btnUpdate, btnDelete, btnShowAll;
    private Bitmap selectedBitmap;
    private Uri cameraUri;

    private final ActivityResultLauncher<Uri> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
                if (success && cameraUri != null) {
                    try {
                        setImage(loadScaledBitmap(cameraUri));
                    } catch (IOException | NullPointerException e) {
                        Toast.makeText(MainActivity.this, "Could not load image", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    private final ActivityResultLauncher<String> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    try {
                        setImage(loadScaledBitmap(uri));
                    } catch (IOException | NullPointerException e) {
                        Toast.makeText(MainActivity.this, "Could not load image", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ivGame = findViewById(R.id.ivGame);
        etName = findViewById(R.id.etName);
        etPriority = findViewById(R.id.etPriority);
        etHours = findViewById(R.id.etHours);
        btnAdd = findViewById(R.id.btnAdd);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnDelete = findViewById(R.id.btnDelete);
        btnShowAll = findViewById(R.id.btnShowAll);

        fsDAO = new FirestoreDAO();

        ivGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(MainActivity.this)
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

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = etName.getText().toString().trim();
                String priorityText = etPriority.getText().toString().trim();
                String hoursText = etHours.getText().toString().trim();
                if (name.isEmpty() || priorityText.isEmpty() || hoursText.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (name.contains("/")) {
                    Toast.makeText(MainActivity.this, "Name cannot contain /", Toast.LENGTH_SHORT).show();
                    return;
                }
                int priority = Integer.parseInt(priorityText);
                double hours = Double.parseDouble(hoursText);
                if (priority < 1 || priority > 3) {
                    Toast.makeText(MainActivity.this, "Priority must be 1, 2 or 3", Toast.LENGTH_SHORT).show();
                    return;
                }
                String image = selectedBitmap == null ? "" : bitmapToBase64(selectedBitmap);
                fsDAO.createDocument(new Games(name, priority, hours, image), new IFirestoreCallback() {
                    @Override
                    public void onOperationComplete(boolean success, String message) {
                        Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                        if (success) {
                            clearPage();
                        }
                    }
                });
            }
        });

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = etName.getText().toString().trim();
                if (name.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Enter the game name", Toast.LENGTH_SHORT).show();
                    return;
                }
                String priorityText = etPriority.getText().toString().trim();
                String hoursText = etHours.getText().toString().trim();
                Integer priority = null;
                Double hours = null;
                String image = null;
                if (!priorityText.isEmpty()) {
                    priority = Integer.parseInt(priorityText);
                    if (priority < 1 || priority > 3) {
                        Toast.makeText(MainActivity.this, "Priority must be 1, 2 or 3", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                if (!hoursText.isEmpty()) {
                    hours = Double.parseDouble(hoursText);
                }
                if (selectedBitmap != null) {
                    image = bitmapToBase64(selectedBitmap);
                }
                if (priority == null && hours == null && image == null) {
                    Toast.makeText(MainActivity.this, "Nothing to update", Toast.LENGTH_SHORT).show();
                    return;
                }
                fsDAO.updateDocument(name, priority, hours, image, new IFirestoreCallback() {
                    @Override
                    public void onOperationComplete(boolean success, String message) {
                        Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                        if (success) {
                            clearPage();
                        }
                    }
                });
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = etName.getText().toString().trim();
                if (name.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Enter the game name", Toast.LENGTH_SHORT).show();
                    return;
                }
                fsDAO.deleteDocument(name, new IFirestoreCallback() {
                    @Override
                    public void onOperationComplete(boolean success, String message) {
                        Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                        if (success) {
                            clearPage();
                        }
                    }
                });
            }
        });

        btnShowAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, ShowcaseActivity.class));
            }
        });
    }

    private void clearPage() {
        etName.setText("");
        etPriority.setText("");
        etHours.setText("");
        etName.clearFocus();
        etPriority.clearFocus();
        etHours.clearFocus();
        selectedBitmap = null;
        cameraUri = null;
        ivGame.setImageResource(android.R.drawable.ic_menu_camera);
    }

    private Uri createCameraUri() {
        File dir = new File(getCacheDir(), "images");
        dir.mkdirs();
        File file = new File(dir, "photo_" + System.currentTimeMillis() + ".jpg");
        return FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", file);
    }

    private int readRotation(Uri uri) {
        try (InputStream stream = getContentResolver().openInputStream(uri)) {
            ExifInterface exif = new ExifInterface(stream);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    return 90;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    return 180;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    return 270;
                default:
                    return 0;
            }
        } catch (IOException | NullPointerException e) {
            return 0;
        }
    }

    private void setImage(Bitmap bitmap) {
        selectedBitmap = scaleDown(bitmap);
        ivGame.setImageBitmap(selectedBitmap);
    }

    private Bitmap loadScaledBitmap(Uri uri) throws IOException {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        try (InputStream stream = getContentResolver().openInputStream(uri)) {
            BitmapFactory.decodeStream(stream, null, options);
        }
        int sample = 1;
        while (options.outWidth / sample > 1024 || options.outHeight / sample > 1024) {
            sample *= 2;
        }
        options.inJustDecodeBounds = false;
        options.inSampleSize = sample;
        Bitmap bitmap;
        try (InputStream stream = getContentResolver().openInputStream(uri)) {
            bitmap = BitmapFactory.decodeStream(stream, null, options);
        }
        int rotation = readRotation(uri);
        if (bitmap != null && rotation != 0) {
            Matrix matrix = new Matrix();
            matrix.postRotate(rotation);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        }
        return bitmap;
    }

    private Bitmap scaleDown(Bitmap source) {
        int max = 480;
        int width = source.getWidth();
        int height = source.getHeight();
        float ratio = Math.min(1f, (float) max / Math.max(width, height));
        return Bitmap.createScaledBitmap(source, Math.round(width * ratio), Math.round(height * ratio), true);
    }

    private String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);
        return Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP);
    }
}