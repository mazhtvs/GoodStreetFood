package com.pizza.giros.burger.goodstreetfood.Activiy;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.View;

import com.google.firebase.FirebaseApp;
import com.pizza.giros.burger.goodstreetfood.R;

public class IntroActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);
        FirebaseApp.initializeApp(this);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isDarkThemeSelected = preferences.getBoolean("is_dark_theme_selected", false);
        if (isDarkThemeSelected) {
            applySavedTheme(); // Применяем сохраненную тему
        } else {
            // Если пользователь не выбрал тему вручную, используем автоматический режим
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        }
        // Создаем Handler для задержки в две секунды
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Переходим к MainActivity
                Intent intent = new Intent(IntroActivity.this, LoginActivity.class);
                startActivity(intent);
                finish(); // Завершаем IntroActivity, чтобы пользователь не мог вернуться к ней по нажатию кнопки "Назад"
            }
        }, 1000);
    }

    // Метод для применения сохраненной темы
    private void applySavedTheme() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isDarkTheme = preferences.getBoolean("is_dark_theme", false);
        if (isDarkTheme) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }
}