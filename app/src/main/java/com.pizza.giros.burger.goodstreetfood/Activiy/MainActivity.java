package com.pizza.giros.burger.goodstreetfood.Activiy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.pizza.giros.burger.goodstreetfood.Adapter.CategoryAdapter;
import com.pizza.giros.burger.goodstreetfood.Adapter.PopularAdapter;
import com.pizza.giros.burger.goodstreetfood.Domain.CategoryDomain;

import com.pizza.giros.burger.goodstreetfood.Domain.FoodDomain;
import com.pizza.giros.burger.goodstreetfood.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements CategoryAdapter.CategoryClickListener, ImagePreloadCallback {
    TextView textView4;
    TextView textView8;
    ImageView imageView10;

    ImageView imageView7;
    private RecyclerView.Adapter  adapter2;
    private CategoryAdapter adapter;
    private RecyclerView recyclerViewCategoryList, recyclerViewPopularList;
    private SharedPreferences sharedPreferences;
    private FirebaseAuth firebaseAuth;
    private StorageReference storageReference;
    private DatabaseReference databaseReference;
    public static ArrayList<FoodDomain> foodlisted = new ArrayList<>();

    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView4 = findViewById(R.id.textView4);
        textView8 = findViewById(R.id.textView8);
        progressBar = findViewById(R.id.progressBar);
        imageView7 = findViewById(R.id.imageView7);
        recyclerViewCategory();
        recyclerViewPopular();
        bottomNavigation();
        FirebaseApp.initializeApp(this);
        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userId);
            loadProfileData();
        } else {
            // Redirect to login if not authenticated
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        }

        if (foodlisted.isEmpty()) {
            addFoodItems(); // Добавляем элементы в список
        }

        AutoCompleteTextView editTextTextPersonName = findViewById(R.id.editTextTextPersonName);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, getFoodTitles());
        editTextTextPersonName.setAdapter(adapter);

        sharedPreferences = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        imageView10 = findViewById(R.id.imageView10);



        // Запуск предварительной загрузки изображений
        ImagePreloader imagePreloader = new ImagePreloader(this, this);
        imagePreloader.preloadImages();

        textView8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Создайте интент для перехода к CardListActivity
                Intent intent = new Intent(MainActivity.this, CardListActivity.class);
                // Запустите активность для отображения списка карточек
                startActivity(intent);
            }
        });

        editTextTextPersonName.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Получаем выбранный элемент из списка подсказок
                String selectedTitle = (String) parent.getItemAtPosition(position);

                // Находим блюдо в foodlisted по названию
                FoodDomain selectedFood = null;
                for (FoodDomain food : foodlisted) {
                    if (food.getTitle().equals(selectedTitle)) {
                        selectedFood = food;
                        break;
                    }
                }

                // Если блюдо найдено, переходим к его описанию
                if (selectedFood != null) {
                    // Создайте интент для перехода к описанию блюда (ShowDetailActivity)
                    Intent intent = new Intent(MainActivity.this, ShowDetailActivity.class);
                    // Передайте объект FoodDomain через интент
                    intent.putExtra("object", selectedFood);
                    // Запустите активность для отображения описания блюда
                    startActivity(intent);
                }
            }
        });

    }

    private void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
        // Отключить взаимодействие с пользователем
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        );
    }

    private void hideProgressBar() {
        progressBar.setVisibility(View.GONE);
        // Включить взаимодействие с пользователем
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    @Override
    public void onPreloadComplete() {
        runOnUiThread(() -> {
            // Ваш код для продолжения загрузки активности
        });
    }

    @Override
    public void onPreloadFailed() {
        runOnUiThread(() -> {
            // Ваш код для обработки ошибки
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            // Пользователь не залогинен, переход к LoginActivity
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        } else {
            // Загрузите данные профиля и изображения, если пользователь залогинен
            loadProfileData();
        }
    }

    private void loadProfileData() {
        if (databaseReference != null) {
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String name = snapshot.child("name").getValue(String.class);
                        String profileImageUrl = snapshot.child("profileImageUrl").getValue(String.class);

                        textView4.setText("Привет " + name);

                        if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                            Picasso.get().load(profileImageUrl).into(imageView10);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(MainActivity.this, "Failed to load profile data", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (foodlisted.isEmpty()) {
            addFoodItems(); // Добавляем элементы в список
        }
    }
    private void addFoodItems() {
        foodlisted.add(new FoodDomain("Шаурма GOOD", "shwrm", "Лаваш, дзадзыки, соус пири-пири, курица, салат, коул-слоу, картофель фри, томаты", 179));
        foodlisted.add(new FoodDomain("Шаурма Сырный Ранч", "ranch", "Лаваш, сырный соус, лук маринованный, курица, картофель фри, томаты", 199));
        foodlisted.add(new FoodDomain("Шаурма Свин Чили с халапенью", "svin", "Лаваш, соус халапенью, салат коул-слоу, свинина, картофель фри, томаты", 219));
        foodlisted.add(new FoodDomain("Шаурма Дон Креветон", "don", "Лаваш, креветки тигровые, кешью, майонез, картофель фри, огурцы, лист салата, сладкий чили", 269));
        foodlisted.add(new FoodDomain("Бургер Форсаж", "burger", "Булочка бриошь, говяжья котлета, томаты, красный лук, лист салата, сыр гауда, огурцы маринованные, кубанская аджика, кетчуп", 249));
        foodlisted.add(new FoodDomain("Стрипс Бургер", "stripsburger", "Булочка бриошь, стрипсы куриные, томаты, сыр гауда, лист салата, кетчуп, огурцы маринованные, кубанская аджика", 199));
        foodlisted.add(new FoodDomain("Хот-Дог BBQ", "xodog", "Булочка, сосиска, кетчуп, соус BBQ, томаты, маринованные огурцы, криспи лук", 109));
        foodlisted.add(new FoodDomain("Хот-Дог по Чикагски", "chicag", "Булочка, сосиска, салат коул-слоу, томаты, лук, красный, халапенью, горчица, соус BBQ", 109));
        foodlisted.add(new FoodDomain("Гирос Слоу", "giros_slow", "Лепешка, курица, салат коул-слоу, томаты, картофель фри, зелень, соус пири-пири", 219));
        foodlisted.add(new FoodDomain("Гирос Сырный Грек", "giros", "Лепешка, свинина, картофель фри, капуста маринованная, соус чесночный ранч, рататуй", 239));
        foodlisted.add(new FoodDomain("Стрипсы куриные", "strips", "", 119));
        foodlisted.add(new FoodDomain("Наггетсы куриные", "nagets", "", 79));
        foodlisted.add(new FoodDomain("Сырные палочки", "sir", "", 139));
        foodlisted.add(new FoodDomain("Курица", "chiken", "", 79));
        foodlisted.add(new FoodDomain("Свинина", "svinina", "", 99));
        foodlisted.add(new FoodDomain("Халапенью\\Криспи лук", "krispi", "", 39));
        foodlisted.add(new FoodDomain("Пири-пири", "piri_piri", "", 59));
        foodlisted.add(new FoodDomain("Дзадзыки", "dza", "", 59));
        foodlisted.add(new FoodDomain("Халапенью", "holo", "", 59));
        foodlisted.add(new FoodDomain("Сырный", "sirniy", "", 59));
        foodlisted.add(new FoodDomain("Кубанская аджика", "adzhika", "", 59));
        foodlisted.add(new FoodDomain("Чесночный ранч", "chensok", "", 59));
        foodlisted.add(new FoodDomain("Картофель Фри Карбонара", "fri_carbonara", "Картофель фри двойной обжарки, соус карбонара", 209));
        foodlisted.add(new FoodDomain("Картофель Фри По-Баварски", "fri_bov", "Картофель фри двойной обжарки, соус баварские колбаски", 219));
        foodlisted.add(new FoodDomain("Картофель Фри с Креветками", "fri_krev", "Картофель фри двойной обжарки, Филе тигровых креветок, сливки, томаты, перец болгарский, томаты, кетчуп", 249));
        foodlisted.add(new FoodDomain("Картофель фри 150г", "frishka", "", 149));
    }

    private String[] getFoodTitles() {
        // Создаем массив для хранения названий блюд
        String[] titles = new String[foodlisted.size()];

        // Заполняем массив названиями блюд из foodlist
        for (int i = 0; i < foodlisted.size(); i++) {
            titles[i] = foodlisted.get(i).getTitle();
        }

        return titles;
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
                startActivity(new Intent(MainActivity.this, com.pizza.giros.burger.goodstreetfood.Activiy.CardListActivity.class));
            }
        });
        homeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, MainActivity.class));
            }
        });
        settingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            }
        });
        profileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, ProfileActivity.class));
            }
        });
        helpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, HelpActivity.class));
            }
        });
    }

    private void recyclerViewPopular() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerViewPopularList = findViewById(R.id.recyclerView2);
        recyclerViewPopularList.setLayoutManager(linearLayoutManager);

        ArrayList<FoodDomain> foodlist = new ArrayList<>();
        foodlist.add(new FoodDomain("Шаурма GOOD", "https://firebasestorage.googleapis.com/v0/b/good-street-food-323e6.appspot.com/o/food%2Fshwrm.png?alt=media&token=ca843194-7818-4508-baac-9051d9efdf2e", "Лаваш, дзадзыки, соус пири-пири, курица, салат, коул-слоу, картофель фри, томаты", 179));
        foodlist.add(new FoodDomain("Бургер Форсаж", "https://firebasestorage.googleapis.com/v0/b/good-street-food-323e6.appspot.com/o/food%2Fburger.png?alt=media&token=c2548c75-a4ce-48e5-b15f-a479460db907", "Булочка бриошь, говяжья котлета, томаты, красный лук, лист салата, сыр гауда, огурцы маринованные, кубанская аджика, кетчуп", 249));
        foodlist.add(new FoodDomain("Стрипс Бургер", "https://firebasestorage.googleapis.com/v0/b/good-street-food-323e6.appspot.com/o/food%2Fstripsburger.png?alt=media&token=a99d7a67-b33e-45e2-ad8d-dbf438795b3a", "Булочка бриошь, стрипсы куриные, томаты, сыр гауда, лист салата, кетчуп, огурцы маринованные, кубанская аджика", 199));
        foodlist.add(new FoodDomain("Гирос Слоу", "https://firebasestorage.googleapis.com/v0/b/good-street-food-323e6.appspot.com/o/food%2Fgiros.png?alt=media&token=2b04ef3d-29ac-42b5-b959-b7b82f29c89c", "Лепешка, курица, салат коул-слоу, томаты, картофель фри, зелень, соус пири-пири", 219));
        foodlist.add(new FoodDomain("Хот-Дог BBQ", "https://firebasestorage.googleapis.com/v0/b/good-street-food-323e6.appspot.com/o/food%2Fxodog.png?alt=media&token=aa71e142-67b4-4081-a02a-e1d2fbe03215", "Булочка, сосиска, кетчуп, соус BBQ, томаты, маринованные огурцы, криспи лук", 109));
        foodlist.add(new FoodDomain("Фри Карбонара", "https://firebasestorage.googleapis.com/v0/b/good-street-food-323e6.appspot.com/o/food%2Ffri_carbonara.png?alt=media&token=2b0ab0ad-dbb3-49fc-b0fb-cc37a02404ae", "Картофель фри двойной обжарки, соус карбонара", 209));

        adapter2 = new PopularAdapter(foodlist);
        recyclerViewPopularList.setAdapter(adapter2);

    }

    private void recyclerViewCategory() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);

        recyclerViewCategoryList = findViewById(R.id.recyclerView);
        recyclerViewCategoryList.setLayoutManager(linearLayoutManager);

        ArrayList<CategoryDomain> categoryList = new ArrayList<>();
        categoryList.add(new CategoryDomain("Шаурма", "cat_1"));
        categoryList.add(new CategoryDomain("Бургеры", "cat_2"));
        categoryList.add(new CategoryDomain("Хот-дог", "cat_3"));
        categoryList.add(new CategoryDomain("Гирос", "cat_40"));
        categoryList.add(new CategoryDomain("Фри", "fri_icon"));
        categoryList.add(new CategoryDomain("Снеки", "cat_50"));
        categoryList.add(new CategoryDomain("Добавки", "cat_60"));
        categoryList.add(new CategoryDomain("Соусы", "sous_icon"));

        adapter = new CategoryAdapter(categoryList);

        // Установка MainActivity в качестве слушателя кликов на категории
        adapter.setCategoryClickListener(this);

        recyclerViewCategoryList.setAdapter(adapter);
    }

    @Override
    public void onCategoryClick(CategoryDomain category) {
        // Здесь вы можете обработать клик на категорию
        // Например, открыть новую активность или выполнить другие действия
        Intent intent = new Intent(MainActivity.this, CategoryDetailActivity.class);
        intent.putExtra("CATEGORY_NAME", category.getTitle());
        startActivity(intent);
    }
}
