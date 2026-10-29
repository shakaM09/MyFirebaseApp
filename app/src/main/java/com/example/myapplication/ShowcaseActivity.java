package com.example.myapplication;

import android.graphics.Bitmap;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.Collections;

public class ShowcaseActivity extends AppCompatActivity {

    private static final int FILTER_ALL = 0;
    private static final int FILTER_MORE = 1;
    private static final int FILTER_LESS = 2;
    private static final int FILTER_P1 = 3;
    private static final int FILTER_P2 = 4;
    private static final int FILTER_P3 = 5;

    private FirestoreDAO fsDAO;
    private LinearLayout llGames;
    private TextView tvEmpty;
    private int currentFilter = FILTER_ALL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_showcase);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        llGames = findViewById(R.id.llGames);
        tvEmpty = findViewById(R.id.tvEmpty);
        Button btnAddDelete = findViewById(R.id.btnAddDelete);
        Button btnAll = findViewById(R.id.btnAll);
        Button btnMore = findViewById(R.id.btnMore);
        Button btnLess = findViewById(R.id.btnLess);
        Button btnP1 = findViewById(R.id.btnP1);
        Button btnP2 = findViewById(R.id.btnP2);
        Button btnP3 = findViewById(R.id.btnP3);

        fsDAO = new FirestoreDAO();

        btnAddDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ShowcaseActivity.this, MainActivity.class));
                finish();
            }
        });
        btnAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setFilter(FILTER_ALL);
            }
        });
        btnMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setFilter(FILTER_MORE);
            }
        });
        btnLess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setFilter(FILTER_LESS);
            }
        });
        btnP1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setFilter(FILTER_P1);
            }
        });
        btnP2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setFilter(FILTER_P2);
            }
        });
        btnP3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setFilter(FILTER_P3);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        fsDAO.listenToAllDocumentsChange(new IFirestoreCallback() {
            @Override
            public void onDataReceived(ArrayList<Games> games) {
                loadCurrentFilter();
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        fsDAO.stopListening();
    }

    private void setFilter(int filter) {
        currentFilter = filter;
        loadCurrentFilter();
    }

    private void loadCurrentFilter() {
        IFirestoreCallback callback = new IFirestoreCallback() {
            @Override
            public void onDataReceived(ArrayList<Games> games) {
                showGames(games);
            }
        };
        switch (currentFilter) {
            case FILTER_MORE:
                fsDAO.readHoursMoreThan(10, callback);
                break;
            case FILTER_LESS:
                fsDAO.readHoursLessThan(10, callback);
                break;
            case FILTER_P1:
                fsDAO.readByPriority(1, callback);
                break;
            case FILTER_P2:
                fsDAO.readByPriority(2, callback);
                break;
            case FILTER_P3:
                fsDAO.readByPriority(3, callback);
                break;
            default:
                fsDAO.readAllDocuments(callback);
                break;
        }
    }

    private void showGames(ArrayList<Games> games) {
        Collections.sort(games, (a, b) -> {
            if (a.getPriority() != b.getPriority()) {
                return Integer.compare(a.getPriority(), b.getPriority());
            }
            return a.getName().compareToIgnoreCase(b.getName());
        });
        llGames.removeAllViews();
        tvEmpty.setVisibility(games.isEmpty() ? View.VISIBLE : View.GONE);
        LayoutInflater inflater = LayoutInflater.from(this);
        for (Games game : games) {
            View item = inflater.inflate(R.layout.item_game, llGames, false);
            ImageView ivItem = item.findViewById(R.id.ivItem);
            TextView tvItemName = item.findViewById(R.id.tvItemName);
            TextView tvItemPriority = item.findViewById(R.id.tvItemPriority);
            TextView tvItemHours = item.findViewById(R.id.tvItemHours);
            tvItemName.setText(game.getName());
            tvItemPriority.setText("Priority: " + game.getPriority());
            tvItemHours.setText("Hours played: " + game.getHours());
            String encoded = game.getImageBase64();
            if (encoded != null && !encoded.isEmpty()) {
                try {
                    byte[] bytes = Base64.decode(encoded, Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    ivItem.setImageBitmap(bitmap);
                } catch (IllegalArgumentException e) {
                    ivItem.setImageResource(android.R.drawable.ic_menu_gallery);
                }
            } else {
                ivItem.setImageResource(android.R.drawable.ic_menu_gallery);
            }
            llGames.addView(item);
        }
    }
}