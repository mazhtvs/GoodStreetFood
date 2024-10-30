package com.pizza.giros.burger.goodstreetfood.Activiy;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pizza.giros.burger.goodstreetfood.R;
import com.pizza.giros.burger.goodstreetfood.Adapter.PopularAdapter;
import com.pizza.giros.burger.goodstreetfood.Adapter.PopularAdapterTitle;
import com.pizza.giros.burger.goodstreetfood.Domain.FoodDomain;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class CategoryDetailActivity extends AppCompatActivity {

    private RecyclerView.Adapter adapter2;
    private DatabaseReference databaseReference;
    private ArrayList<FoodDomain> foodlist;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_detail);
        bottomNavigation();

        RecyclerView recyclerView = findViewById(R.id.recyclerViewCategoryDetail);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        // Получение названия категории из Intent
        String categoryName = getIntent().getStringExtra("CATEGORY_NAME");

        // Инициализация Firebase Database
        databaseReference = FirebaseDatabase.getInstance().getReference("categories").child(categoryName);

        foodlist = new ArrayList<>();

        // Загрузка данных из Firebase
        loadCategoryData(categoryName, recyclerView);
    }

    private void loadCategoryData(String categoryName, RecyclerView recyclerView) {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                foodlist.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    FoodDomain food = snapshot.getValue(FoodDomain.class);
                    foodlist.add(food);
                }
                adapter2 = new PopularAdapterTitle(foodlist);
                recyclerView.setAdapter(adapter2);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Обработка ошибок
                Toast.makeText(CategoryDetailActivity.this, "Failed to load data", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void bottomNavigation() {
        FloatingActionButton floatingActionButton = findViewById(R.id.card_btn1);
        LinearLayout homeBtn = findViewById(R.id.home_btn1);
        LinearLayout settingsBtn = findViewById(R.id.settings_btn);
        LinearLayout profileBtn = findViewById(R.id.profile_btn);
        LinearLayout helpBtn = findViewById(R.id.help_btn);

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(CategoryDetailActivity.this, com.pizza.giros.burger.goodstreetfood.Activiy.CardListActivity.class));
            }
        });
        homeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(CategoryDetailActivity.this, MainActivity.class));
            }
        });
        settingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(CategoryDetailActivity.this, SettingsActivity.class));
            }
        });
        profileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(CategoryDetailActivity.this, ProfileActivity.class));
            }
        });
        helpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(CategoryDetailActivity.this, HelpActivity.class));
            }
        });
    }
}