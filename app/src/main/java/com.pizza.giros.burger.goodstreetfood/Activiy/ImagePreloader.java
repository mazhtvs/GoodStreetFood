package com.pizza.giros.burger.goodstreetfood.Activiy;

import android.content.Context;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pizza.giros.burger.goodstreetfood.Domain.FoodDomain;

import java.util.ArrayList;

public class ImagePreloader {

    private Context context;
    private DatabaseReference databaseReference;
    private ImagePreloadCallback callback;

    public ImagePreloader(Context context, ImagePreloadCallback callback) {
        this.context = context;
        this.callback = callback;
        this.databaseReference = FirebaseDatabase.getInstance().getReference("categories");
    }

    public void preloadImages() {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<String> imageUrls = new ArrayList<>();
                for (DataSnapshot categorySnapshot : dataSnapshot.getChildren()) {
                    for (DataSnapshot foodSnapshot : categorySnapshot.getChildren()) {
                        FoodDomain food = foodSnapshot.getValue(FoodDomain.class);
                        if (food != null && food.getPic() != null) {
                            imageUrls.add(food.getPic());
                        }
                    }
                }
                preloadImages(imageUrls);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onPreloadFailed();
            }
        });
    }

    private void preloadImages(ArrayList<String> imageUrls) {
        if (imageUrls.isEmpty()) {
            callback.onPreloadComplete();
            return;
        }

        for (String url : imageUrls) {
            Glide.with(context)
                    .load(url)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .preload();
        }

        callback.onPreloadComplete();
    }
}
