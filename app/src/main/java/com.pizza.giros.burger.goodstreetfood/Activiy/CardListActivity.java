package com.pizza.giros.burger.goodstreetfood.Activiy;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.pizza.giros.burger.goodstreetfood.R;
import com.pizza.giros.burger.goodstreetfood.Adapter.CartListAdapter;
import com.pizza.giros.burger.goodstreetfood.Domain.FoodDomain;
import com.pizza.giros.burger.goodstreetfood.Helper.ManagementCart;
import com.pizza.giros.burger.goodstreetfood.Interface.ChangeNumberItemsListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class CardListActivity extends AppCompatActivity {

    private RecyclerView.Adapter adapter;
    private RecyclerView recyclerViewList;
    private ManagementCart managementCart;
    private TextView totalFeeTxt, taxTxt, deliveryTxt, totalTxt, emptyTxt, textView16;
    private int tax;
    private ScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_list);

        managementCart = new ManagementCart(this);

        initView();
        initList();
        calculateCard();
        bottomNavigation();

        textView16 = findViewById(R.id.textView16);
        textView16.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ProfileActivity.isProfileDataComplete(CardListActivity.this)) {
                    navigateToPaymentActivity(); // Переходим к экрану оплаты, если данные профиля заполнены
                } else {
                    // Выводим сообщение о необходимости заполнения данных профиля
                    Toast.makeText(CardListActivity.this, "Сначала заполните все данные в профиле", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void navigateToPaymentActivity() {
        // Создаем Intent для перехода к активности оплаты
        Intent intent = new Intent(CardListActivity.this, PaymentActivity.class);

        // Получаем общую сумму из метода calculateCard()
        int totalAmount = calculateCard();
        ArrayList<FoodDomain> foodDomains = managementCart.getListCard();
        // Передаем общую сумму через Intent в активность оплаты
        intent.putExtra("TOTAL_AMOUNT", totalAmount);
        intent.putExtra("foodDomains", foodDomains);
        // Запускаем активность оплаты
        startActivity(intent);
    }

    private void initList() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerViewList.setLayoutManager(linearLayoutManager);
        adapter = new CartListAdapter(managementCart.getListCard(), this, new ChangeNumberItemsListener() {
            @Override
            public void changed() {
                calculateCard();
            }
        });

        recyclerViewList.setAdapter(adapter);
        if (managementCart.getListCard().isEmpty()) {
            emptyTxt.setVisibility(View.VISIBLE);
            scrollView.setVisibility(View.GONE);
        } else {
            emptyTxt.setVisibility(View.GONE);
            scrollView.setVisibility(View.VISIBLE);
        }
    }

    private int calculateCard() {
        //double percentTax = 0.02;
        int delivery = 1;

        //tax = (int) (Math.round((managementCart.getTotalFee() * percentTax) * 100) / 100);
        int total = (int) (Math.round((managementCart.getTotalFee() + delivery) * 100) / 100);
        int itemTotal = (int) (Math.round(managementCart.getTotalFee() * 100) / 100);

        totalFeeTxt.setText("₽" + itemTotal);
        //taxTxt.setText("₽" + tax);
        deliveryTxt.setText("₽" + delivery);
        totalTxt.setText("₽" + total);

        // Проверяем, пуст ли список корзины, и обновляем видимость emptyTxt
        if (managementCart.getListCard().isEmpty()) {
            emptyTxt.setVisibility(View.VISIBLE);
            scrollView.setVisibility(View.GONE);
        } else {
            emptyTxt.setVisibility(View.GONE);
            scrollView.setVisibility(View.VISIBLE);
        }

        return total; // Возвращаем значение total
    }
    private void initView() {
        recyclerViewList = findViewById(R.id.recyclerview);
        totalFeeTxt = findViewById(R.id.totalFeeTxt);
        taxTxt = findViewById(R.id.taxTxt);
        deliveryTxt = findViewById(R.id.deliveryTxt);
        totalTxt = findViewById(R.id.totalTxt);
        emptyTxt = findViewById(R.id.emptyTxt);
        scrollView = findViewById(R.id.scrollView4);

    }

    private void bottomNavigation() {
        FloatingActionButton floatingActionButton = findViewById(R.id.card_btn);
        LinearLayout homeBtn = findViewById(R.id.homeBtn);
        LinearLayout settingsBtn = findViewById(R.id.settings_btn);
        LinearLayout profileBtn = findViewById(R.id.profile_btn);
        LinearLayout helpBtn = findViewById(R.id.help_btn);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(CardListActivity.this, CardListActivity.class));
            }
        });

        homeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(CardListActivity.this, MainActivity.class));
            }
        });
        settingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(CardListActivity.this, SettingsActivity.class));
            }
        });
        profileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(CardListActivity.this, ProfileActivity.class));
            }
        });
        helpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(CardListActivity.this, HelpActivity.class));
            }
        });
    }
}