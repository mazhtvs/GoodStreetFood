package com.pizza.giros.burger.goodstreetfood;

import android.app.Application;
import android.util.Log;

import androidx.multidex.MultiDex;

import com.pizza.giros.burger.goodstreetfood.Activiy.ImagePreloadCallback;
import com.pizza.giros.burger.goodstreetfood.Activiy.ImagePreloader;

public class MyApp extends Application implements ImagePreloadCallback {

    @Override
    public void onCreate() {
        super.onCreate();
        MultiDex.install(this);
        // Запуск предварительной загрузки изображений
        ImagePreloader imagePreloader = new ImagePreloader(this, this);
        imagePreloader.preloadImages();
    }

    @Override
    public void onPreloadComplete() {
        // Изображения успешно загружены
        Log.d("MyApp", "Image preloading complete");
    }

    @Override
    public void onPreloadFailed() {
        // Ошибка при загрузке изображений
        Log.d("MyApp", "Image preloading failed");
    }
}

