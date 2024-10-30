package com.pizza.giros.burger.goodstreetfood.Activiy;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pizza.giros.burger.goodstreetfood.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class HelpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        bottomNavigation();

        TextView textView15 = findViewById(R.id.textView15);
        textView15.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Создаем новый Intent с действием ACTION_SENDTO для отправки письма
                Intent intent = new Intent (Intent.ACTION_VIEW , Uri.parse("mailto:" + "goodstreetfood96@gmail.com"));
                intent.putExtra(Intent.EXTRA_SUBJECT, "Ваша тема");
                intent.putExtra(Intent.EXTRA_TEXT, "Ваш текст");
                startActivity(intent);
            }
        });
    }
    private void bottomNavigation() {
        FloatingActionButton floatingActionButton = findViewById(R.id.card_btn1);
        LinearLayout homeBtn = findViewById(R.id.home_btn1);
        LinearLayout settingsBtn = findViewById(R.id.settings_btn);
        LinearLayout profileBtn = findViewById(R.id.profile_btn);

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HelpActivity.this, com.pizza.giros.burger.goodstreetfood.Activiy.CardListActivity.class));
            }
        });
        profileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HelpActivity.this, ProfileActivity.class));
            }
        });
        homeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HelpActivity.this, MainActivity.class));
            }
        });
        settingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HelpActivity.this, SettingsActivity.class));
            }
        });
    }
}