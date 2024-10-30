package com.pizza.giros.burger.goodstreetfood.Activiy;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.pizza.giros.burger.goodstreetfood.Domain.FoodDomain;
import com.pizza.giros.burger.goodstreetfood.Helper.ManagementCart;
import com.pizza.giros.burger.goodstreetfood.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class ShowDetailActivity extends AppCompatActivity {

    private TextView addToCardBtn;
    private TextView titleTxt, feeTxt, descriptionTxt, numberOrderTxt;
    private ImageView plusBtn, minusBtn, picFood;

    private FoodDomain object;

    private int numberOrder = 1;
    //
    private ManagementCart managementCart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_detail);

        managementCart = new ManagementCart(this);

        initView();
        getBundle();
        bottomNavigation();
    }

    private void getBundle() {
        // Получение объекта FoodDomain из Intent
        object = (FoodDomain) getIntent().getSerializableExtra("object");

        Glide.with(this)
                .load(object.getPic())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(picFood);
        titleTxt.setText(object.getTitle());
        descriptionTxt.setText(object.getDescription());

        // Установка начального количества
        numberOrderTxt.setText(String.valueOf(numberOrder));

        // Отображение цены
        feeTxt.setText("₽" + object.getFee());

        // Проверка, является ли объект наггетсами, стрипсами или сырыми палочками
        if ("Наггетсы куриные".equals(object.getTitle()) || "Стрипсы куриные".equals(object.getTitle()) || "Сырные палочки".equals(object.getTitle())) {
            // Для наггетсов, стрипсов и сырых палочек применяем особую логику изменения количества и цены
            applySpecialLogic();
        } else {
            // Для всех остальных элементов применяем логику изменения количества и цены по умолчанию
            applyDefaultLogic();
        }
    }

    // Применение логики изменения количества и цены для наггетсов, стрипсов и сырых палочек
    // Применение логики изменения количества и цены для наггетсов, стрипсов и сырых палочек
    private void applySpecialLogic() {
        numberOrder = 3; // Устанавливаем значение по умолчанию равным 3
        numberOrderTxt.setText(String.valueOf(numberOrder));
        feeTxt.setText(getPriceText(object.getTitle(), numberOrder));

        plusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                numberOrder += 3;
                numberOrderTxt.setText(String.valueOf(numberOrder));
                feeTxt.setText(getPriceText(object.getTitle(), numberOrder));
            }
        });

        minusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (numberOrder > 3) {
                    numberOrder -= 3;
                    numberOrderTxt.setText(String.valueOf(numberOrder));
                    feeTxt.setText(getPriceText(object.getTitle(), numberOrder));
                }
            }
        });
        addToCardBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                object.setNumberInCart(numberOrder);
                managementCart.insertFood(object);
                // Откат назад после добавления товара в корзину
                finish();
            }
        });
    }

    // Применение логики изменения количества и цены по умолчанию
    private void applyDefaultLogic() {
        plusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                numberOrder++;
                numberOrderTxt.setText(String.valueOf(numberOrder));
                feeTxt.setText("₽" + object.getFee() * numberOrder); // Обновляем цену
            }
        });

        minusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (numberOrder > 1) {
                    numberOrder--;
                    numberOrderTxt.setText(String.valueOf(numberOrder));
                    feeTxt.setText("₽" + object.getFee() * numberOrder); // Обновляем цену
                }
            }
        });
        addToCardBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                object.setNumberInCart(numberOrder);
                managementCart.insertFood(object);
                // Откат назад после добавления товара в корзину
                finish();
            }
        });
    }

    // Метод для расчета цены в зависимости от количества
    // Метод для получения текста цены в зависимости от названия блюда и количества
    private String getPriceText(String foodTitle, int quantity) {
        String priceText = "₽";
        int price = 0;

        // Проверка названия блюда и установка соответствующей цены
        switch (foodTitle) {
            case "Стрипсы куриные":
                price = calculatePrice(quantity, 119, 189, 259, 119);
                break;
            case "Наггетсы куриные":
                price = calculatePrice(quantity, 79, 129, 179, 79);
                break;
            case "Сырные палочки":
                price = calculatePrice(quantity, 139, 219, 299, 139);
                break;
        }

        // Форматирование текста цены
        if (price > 0) {
            priceText += price;
        }
        return priceText;
    }

    // Метод для расчета цены в зависимости от количества
    private int calculatePrice(int quantity, int price3, int price6, int price9, int pricePerAdditional) {
        int price = 0;
        if (quantity >= 9) {
            price = price9 + (quantity - 9) / 3 * pricePerAdditional; // Прибавляем цену за каждые 3 дополнительных элемента
        } else if (quantity >= 6) {
            price = price6;
        } else if (quantity >= 3) {
            price = price3;
        }
        return price;
    }
    //Ánh xạ
    private void initView() {
        addToCardBtn = findViewById(R.id.addToCardBtn);
        titleTxt = findViewById(R.id.titleTxt);
        feeTxt = findViewById(R.id.priceTxt);
        descriptionTxt = findViewById(R.id.descriptionTxt);
        numberOrderTxt = findViewById(R.id.numberOrderTxt);
        plusBtn = findViewById(R.id.plusBtn);
        minusBtn = findViewById(R.id.minusBtn);
        picFood = findViewById(R.id.foodPic);
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
                startActivity(new Intent(ShowDetailActivity.this, com.pizza.giros.burger.goodstreetfood.Activiy.CardListActivity.class));
            }
        });
        homeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ShowDetailActivity.this, MainActivity.class));
            }
        });
        settingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ShowDetailActivity.this, SettingsActivity.class));
            }
        });
        profileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ShowDetailActivity.this, ProfileActivity.class));
            }
        });
        helpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ShowDetailActivity.this, HelpActivity.class));
            }
        });
    }
}