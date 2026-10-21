package com.example.myapplication;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.Firebase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collection;

public class MainActivity extends AppCompatActivity {

    FirestoreDAO fsDAO;
    private EditText etName,etNumber,etElo;
    private Button btnInsert,btnSelect,btnShow,btnUpdate,btnDelete;
    private TextView tvcontent;

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

        tvcontent = findViewById(R.id.tvcontent);
        etName= findViewById(R.id.fullname);
        etNumber=findViewById(R.id.tz);
        etElo = findViewById(R.id.elo);
        btnInsert=findViewById(R.id.btnInsert);
        btnSelect=findViewById(R.id.btnSelect);
        btnShow=findViewById(R.id.btnShow);
        btnUpdate=findViewById(R.id.btnUpdate);
        btnDelete=findViewById(R.id.btnDelete);

        fsDAO = new FirestoreDAO();


        btnInsert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fsDAO.createDocument(etName.getText().toString(),
                        etNumber.getText().toString(),
                        Integer.parseInt(etElo.getText().toString()));
            }
        });
        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tvcontent.setText("");
                fsDAO.readAllDocuments( new IFirestoreCallback() {
                    @Override
                    public void onDataReceived(ArrayList<Players> players) {
                        for (int i =0; i< players.size();i++) {
                            tvcontent.setText(tvcontent.getText().toString() + players.get(i).toString());
                        }
                    }
                });
            }
        });
        btnShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tvcontent.setText("");
                fsDAO.readDocumentById(etNumber.getText().toString(), new IFirestoreCallback() {
                    @Override
                    public void onDataReceived(ArrayList<Players> players) {
                            tvcontent.setText(players.get(0).toString());
                    }
                });
            }
        });

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fsDAO.updateDocument(etNumber.getText().toString(),Integer.parseInt(etElo.getText().toString()));
            }
        });
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fsDAO.deleteDocument(etNumber.getText().toString());
            }
        });

    }
    @Override
    protected void onStart(){
        super.onStart();
        fsDAO.listenToAllDocumentsChange();
    }
    @Override
    protected void onStop(){
        super.onStop();
        fsDAO.stopListening();
    }
}