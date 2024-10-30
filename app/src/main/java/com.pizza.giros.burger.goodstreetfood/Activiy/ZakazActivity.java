package com.pizza.giros.burger.goodstreetfood.Activiy;

import static com.pizza.giros.burger.goodstreetfood.Activiy.ProfileActivity.isProfileDataComplete;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.pizza.giros.burger.goodstreetfood.Domain.FoodDomain;
import com.pizza.giros.burger.goodstreetfood.R;

import java.util.ArrayList;

public class ZakazActivity extends AppCompatActivity {
    TextView textView25;
    int totalAmount; // Переменная для хранения суммы заказа
    ArrayList<FoodDomain> foodDomains; // Переменная для хранения списка заказанных элементов
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.zakaz);
        textView25 = findViewById(R.id.textView25);

        // Получение суммы заказа из корзины
        totalAmount = getIntent().getIntExtra("TOTAL_AMOUNT", 0);

        // Получение списка заказанных элементов из корзины
        foodDomains = (ArrayList<FoodDomain>) getIntent().getSerializableExtra("foodDomains");

        // Отправка электронного письма при создании активности
        sendOrderConfirmationEmail();

        textView25.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Создание интента для перехода на главную активность
                Intent intent = new Intent(ZakazActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }

    private void sendOrderConfirmationEmail() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                // Проверяем, заполнены ли все данные профиля
                if (isProfileDataComplete(ZakazActivity.this)) {
                    sharedPreferences = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
                    // Получаем информацию о клиенте из SharedPreferences
                    String name = sharedPreferences.getString("editTextText", "");
                    String phone = sharedPreferences.getString("editTextPhone", "");
                    String address = sharedPreferences.getString("editTextText2", "");
                    String email = sharedPreferences.getString("editTextTextEmailAddress2", "");

                    // Формирование тела письма с суммой заказа, списком заказанных элементов и информацией о клиенте
                    StringBuilder emailBodyBuilder = new StringBuilder();
                    emailBodyBuilder.append("Оплачено картой\n");
                    emailBodyBuilder.append("\nСумма заказа: ").append(totalAmount).append(" руб.\n");
                    emailBodyBuilder.append("\nЗаказ:\n");

                    for (FoodDomain food : foodDomains) {
                        emailBodyBuilder.append("- ").append(food.getTitle()).append(": ").append(food.getNumberInCart()).append(" шт.\n");
                    }

                    // Добавляем информацию о клиенте
                    emailBodyBuilder.append("\nИнформация о клиенте:\n");
                    emailBodyBuilder.append("Имя: ").append(name).append("\n");
                    emailBodyBuilder.append("Телефон: ").append(phone).append("\n");
                    emailBodyBuilder.append("Адрес: ").append(address).append("\n");
                    emailBodyBuilder.append("Email: ").append(email).append("\n");

                    String emailBody = emailBodyBuilder.toString();

                    // Генерация уникального номера заказа (например, с использованием временной метки)
                    String orderId = String.valueOf(System.currentTimeMillis());

                    // Формирование темы письма с указанием номера заказа и имени клиента
                    String subject = "Новый заказ № " + orderId + " от " + name;

                    // Отправка электронного письма с обновленной темой
                    MailSender.sendEmail("goodstreetfood96@gmail.com", subject, emailBody);
                }
                return null;
            }
        }.execute();
    }
}


