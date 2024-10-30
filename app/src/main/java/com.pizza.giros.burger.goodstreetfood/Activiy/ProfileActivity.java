package com.pizza.giros.burger.goodstreetfood.Activiy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.pizza.giros.burger.goodstreetfood.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ProfileActivity extends AppCompatActivity {

    EditText editTextText, editTextPhone, editTextText2, editTextTextEmailAddress2;
    TextView textView7;
    SharedPreferences sharedPreferences;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private static final int PICK_IMAGE_REQUEST = 1;
    ImageView imageView3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        bottomNavigation();
        editTextText = findViewById(R.id.editTextText);
        editTextPhone = findViewById(R.id.editTextPhone);
        editTextText2 = findViewById(R.id.editTextText2);
        editTextTextEmailAddress2 = findViewById(R.id.editTextTextEmailAddress2);
        textView7 = findViewById(R.id.textView7);
        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userId);
            storageReference = FirebaseStorage.getInstance().getReference("profile_images").child(userId + ".jpg");
            // Load profile data
            loadProfileData();
        }
        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);

        // Load saved data
        editTextText.setText(sharedPreferences.getString("editTextText", ""));
        editTextPhone.setText(sharedPreferences.getString("editTextPhone", ""));
        editTextText2.setText(sharedPreferences.getString("editTextText2", ""));
        editTextTextEmailAddress2.setText(sharedPreferences.getString("editTextTextEmailAddress2", ""));
        imageView3 = findViewById(R.id.imageView3);

        // Загрузка изображения из SharedPreferences
        Bitmap savedImage = loadImageFromSharedPreferences();
        if (savedImage != null) {
            imageView3.setImageBitmap(savedImage);
        }

        textView7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveData();
                clearFocusAndNotify();
                saveName();
                saveProfileDataToFirebase();
                if (!isDefaultImage()) {
                    uploadProfileImage();
                }
            }
        });


        imageView3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        Intent intent = getIntent();
        String name = intent.getStringExtra("name");
        String phone = intent.getStringExtra("phone");
        String address = intent.getStringExtra("address");
        String email = intent.getStringExtra("email");
        String profileImageUrl = intent.getStringExtra("profileImageUrl");

        editTextText.setText(name);
        editTextPhone.setText(phone);
        editTextText2.setText(address);
        editTextTextEmailAddress2.setText(email);

        if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
            Picasso.get().load(profileImageUrl).into(imageView3);
        }
    }

    private void saveProfileDataToFirebase() {
        String name = editTextText.getText().toString();
        String phone = editTextPhone.getText().toString();
        String address = editTextText2.getText().toString();
        String email = editTextTextEmailAddress2.getText().toString();

        UserProfile userProfile = new UserProfile(name, phone, address, email);

        if (databaseReference != null) {
            databaseReference.setValue(userProfile)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(ProfileActivity.this, "Данные сохранены", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(ProfileActivity.this, "Данные не сохранились", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    private void uploadProfileImage() {
        if (storageReference == null) {
            Toast.makeText(ProfileActivity.this, "Storage reference is null", Toast.LENGTH_SHORT).show();
            return;
        }

        if (imageView3.getDrawable() != null && !isDefaultImage()) {
            Bitmap bitmap = ((BitmapDrawable) imageView3.getDrawable()).getBitmap();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();

            UploadTask uploadTask = storageReference.putBytes(data);
            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()) {
                        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String downloadUrl = uri.toString();
                                databaseReference.child("profileImageUrl").setValue(downloadUrl);
                            }
                        });
                    } else {
                        Toast.makeText(ProfileActivity.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private boolean isDefaultImage() {
        Drawable drawable = imageView3.getDrawable();
        if (drawable == null) {
            return true;
        }
        if (drawable instanceof BitmapDrawable) {
            Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
            Bitmap defaultBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.lego); // замените R.drawable.lego на ваше изображение по умолчанию
            return bitmap.sameAs(defaultBitmap);
        }
        return false;
    }
    private void loadProfileData() {
        if (databaseReference != null) {
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String name = snapshot.child("name").getValue(String.class);
                        String phone = snapshot.child("phone").getValue(String.class);
                        String address = snapshot.child("address").getValue(String.class);
                        String email = snapshot.child("email").getValue(String.class);
                        String profileImageUrl = snapshot.child("profileImageUrl").getValue(String.class);

                        editTextText.setText(name);
                        editTextPhone.setText(phone);
                        editTextText2.setText(address);
                        editTextTextEmailAddress2.setText(email);

                        if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                            Picasso.get().load(profileImageUrl).into(imageView3);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(ProfileActivity.this, "Failed to load profile data", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public static class UserProfile {
        public String name;
        public String phone;
        public String address;
        public String email;

        public UserProfile() {
            // Default constructor required for calls to DataSnapshot.getValue(UserProfile.class)
        }

        public UserProfile(String name, String phone, String address, String email) {
            this.name = name;
            this.phone = phone;
            this.address = address;
            this.email = email;
        }
    }

    public static boolean isProfileDataComplete(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        String name = sharedPreferences.getString("editTextText", "");
        String phone = sharedPreferences.getString("editTextPhone", "");
        String address = sharedPreferences.getString("editTextText2", "");
        String email = sharedPreferences.getString("editTextTextEmailAddress2", "");

        // Проверяем, заполнены ли все данные профиля
        return !name.isEmpty() && !phone.isEmpty() && !address.isEmpty() && !email.isEmpty();
    }

    private void saveImageToSharedPreferences(Bitmap bitmap) {
        // Преобразование изображения в строку Base64
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        String encodedImage = Base64.encodeToString(byteArray, Base64.DEFAULT);

        // Сохранение строки Base64 в SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("IMAGE", encodedImage);
        editor.apply();
    }
    @Override
    protected void onPause() {
        super.onPause();
        if (imageView3.getDrawable() != null && imageView3.getDrawable() instanceof BitmapDrawable) {
            Bitmap bitmap = ((BitmapDrawable) imageView3.getDrawable()).getBitmap();
            saveImageToSharedPreferences(bitmap);
        }
    }

    private Bitmap loadImageFromSharedPreferences() {
        String encodedImage = sharedPreferences.getString("IMAGE", "");

        if (!encodedImage.isEmpty()) {
            byte[] decodedString = Base64.decode(encodedImage, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        }
        return null;
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);

                // Обрезаем изображение в форму круга
                Bitmap circularBitmap = getRoundedBitmap(bitmap);

                // Отображаем обрезанное изображение в ImageView
                imageView3.setImageBitmap(circularBitmap);

                // Сохраняем обрезанное изображение
                saveImageToSharedPreferences(circularBitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Bitmap getRoundedBitmap(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int diameter = Math.min(width, height);

        Bitmap output = Bitmap.createBitmap(diameter, diameter, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, diameter, diameter);
        final RectF rectF = new RectF(rect);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(Color.BLACK);
        canvas.drawOval(rectF, paint);

        // Сдвигаем изображение, чтобы центрировать его внутри круга
        int left = (diameter - width) / 2;
        int top = (diameter - height) / 2;

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, left, top, paint);

        return output;
    }
    public void saveName() {
        String name = editTextText.getText().toString();

        // Сохраняем имя в SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("USERNAME", name);
        editor.apply();

        // Возвращаем результат в MainActivity
        Intent intent = new Intent();
        intent.putExtra("NAME", name);
        setResult(RESULT_OK, intent);
    }

    private void clearFocusAndNotify() {
        editTextText.clearFocus();
        editTextPhone.clearFocus();
        editTextText2.clearFocus();
        editTextTextEmailAddress2.clearFocus();

    }

    private void saveData() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("editTextText", editTextText.getText().toString());
        editor.putString("editTextPhone", editTextPhone.getText().toString());
        editor.putString("editTextText2", editTextText2.getText().toString());
        editor.putString("editTextTextEmailAddress2", editTextTextEmailAddress2.getText().toString());
        editor.apply();
    }
    private void bottomNavigation() {
        FloatingActionButton floatingActionButton = findViewById(R.id.card_btn1);
        LinearLayout homeBtn = findViewById(R.id.home_btn1);
        LinearLayout settingsBtn = findViewById(R.id.settings_btn);
        LinearLayout helpBtn = findViewById(R.id.help_btn);

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ProfileActivity.this, com.pizza.giros.burger.goodstreetfood.Activiy.CardListActivity.class));
            }
        });
        homeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ProfileActivity.this, MainActivity.class));
            }
        });
        settingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ProfileActivity.this, SettingsActivity.class));
            }
        });
        helpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ProfileActivity.this, HelpActivity.class));
            }
        });
    }
}