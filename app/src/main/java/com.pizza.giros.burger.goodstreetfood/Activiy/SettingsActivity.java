package com.pizza.giros.burger.goodstreetfood.Activiy;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.firebase.auth.FirebaseAuth;
import com.pizza.giros.burger.goodstreetfood.Helper.TinyDB;
import com.pizza.giros.burger.goodstreetfood.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        bottomNavigation(); // Вызываем метод инициализации нижней навигации
        setupThemeButton(); // Вызываем метод для настройки кнопки выбора темы
        Exit();
        WebView webView = findViewById(R.id.web_view);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // Возвращаем false, чтобы загрузить URL в WebView
                return false;
            }
        });
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setUseWideViewPort(true);
    }

    private void Exit() {
        Button themeBtn = findViewById(R.id.exit);
        themeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Разлогиниваем пользователя
                FirebaseAuth.getInstance().signOut();

                // Очищаем SharedPreferences
                clearSharedPreferences();

                clearTinyDB();

                // Перенаправляем на экран авторизации
                Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Очищаем стек задач
                startActivity(intent);
                finish(); // Завершаем текущую активность
            }
        });
    }
    private void clearSharedPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear(); // Очистить все данные
        editor.apply();
    }
    private void clearTinyDB() {
        TinyDB tinyDB = new TinyDB(this);
        tinyDB.clear(); // Очистить все данные из TinyDB
    }

    private void setupThemeButton() {
        Button themeBtn = findViewById(R.id.theme_btn);
        themeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showThemeSelectionDialog(); // Показываем диалоговое окно выбора темы
            }
        });
    }

    private void showThemeSelectionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Выберите тему");
        String[] themeOptions = {"Светлая", "Темная"};
        builder.setItems(themeOptions, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    setLightTheme();
                    saveThemePreference(false); // Сохраняем выбранную тему в SharedPreferences
                } else {
                    setDarkTheme();
                    saveThemePreference(true); // Сохраняем выбранную тему в SharedPreferences
                }
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private void setLightTheme() {
        // Устанавливаем тему светлой
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        // Перезагружаем активити, чтобы изменения темы вступили в силу
        recreate();
    }

    private void setDarkTheme() {
        // Устанавливаем тему темной
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        // Перезагружаем активити, чтобы изменения темы вступили в силу
        recreate();
    }

    private void saveThemePreference(boolean isDarkTheme) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("is_dark_theme_selected", true); // Указываем, что тема выбрана вручную
        editor.putBoolean("is_dark_theme", isDarkTheme);
        editor.apply();
    }
    public void openWebView(View view) {
        WebView webView = findViewById(R.id.web_view);
        webView.setVisibility(View.VISIBLE);
        webView.loadUrl("https://docs.google.com/document/d/1sUhNAbHmgWVMxnItSQdLbDwe5scPRLD0s8FGySuNuRs/edit?usp=sharing");
    }

    @Override
    public void onBackPressed() {
        WebView webView = findViewById(R.id.web_view);
        webView.setVisibility(View.GONE);
    }
    private void bottomNavigation() {
        FloatingActionButton floatingActionButton = findViewById(R.id.card_btn1);
        LinearLayout homeBtn = findViewById(R.id.home_btn1);
        LinearLayout profileBtn = findViewById(R.id.profile_btn);
        LinearLayout helpBtn = findViewById(R.id.help_btn);

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SettingsActivity.this, com.pizza.giros.burger.goodstreetfood.Activiy.CardListActivity.class));
            }
        });
        homeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SettingsActivity.this, MainActivity.class));
            }
        });
        profileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SettingsActivity.this, ProfileActivity.class));
            }
        });
        helpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SettingsActivity.this, HelpActivity.class));
            }
        });
    }
}
