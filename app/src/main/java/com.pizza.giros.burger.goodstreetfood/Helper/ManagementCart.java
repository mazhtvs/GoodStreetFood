package com.pizza.giros.burger.goodstreetfood.Helper;

import android.content.Context;
import android.widget.Toast;


import com.pizza.giros.burger.goodstreetfood.Domain.FoodDomain;
import com.pizza.giros.burger.goodstreetfood.Interface.ChangeNumberItemsListener;

import java.util.ArrayList;

public class ManagementCart {
    private Context context;
    private TinyDB tinyDB;

    public ManagementCart(Context context) {
        this.context = context;
        this.tinyDB = new TinyDB(context);
    }

    public void insertFood(FoodDomain item) {
        ArrayList<FoodDomain> listFood = getListCard();
        boolean existAlready = false;
        int n = 0;
        for (int i = 0; i < listFood.size(); i++) {
            if (listFood.get(i).getTitle().equals(item.getTitle())) {
                existAlready = true;
                n = i;
                break;
            }
        }

        if (existAlready) {
            if (isSpecialFood(item.getTitle())) {
                // Применяем особую логику для наггетсов, стрипсов и сырых палочек
                listFood.get(n).setNumberInCart(listFood.get(n).getNumberInCart() + item.getNumberInCart());
            } else {
                // Для всех остальных товаров применяем обычную логику
                listFood.get(n).setNumberInCart(item.getNumberInCart());
            }
        } else {
            listFood.add(item);
        }

        tinyDB.putListObject("Список карт", listFood);
        Toast.makeText(context, "Добавлено в корзину", Toast.LENGTH_SHORT).show();
    }

    // Метод для проверки, является ли товар особым (Стрипсы куриные, Наггетсы куриные, Сырные палочки)
    private boolean isSpecialFood(String foodTitle) {
        return "Стрипсы куриные".equals(foodTitle) || "Наггетсы куриные".equals(foodTitle) || "Сырные палочки".equals(foodTitle);
    }
    //
    public ArrayList<FoodDomain> getListCard() {
        return tinyDB.getListObject("Список карт");
    }

    public void plusNumberFood(ArrayList<FoodDomain> listfood, int position, ChangeNumberItemsListener changeNumberItemsListener) {
        FoodDomain food = listfood.get(position);
        if (isSpecialFood(food.getTitle())) {
            food.setNumberInCart(food.getNumberInCart() + 3); // Увеличиваем количество на 3 для специальных блюд
        } else {
            food.setNumberInCart(food.getNumberInCart() + 1); // Увеличиваем количество на 1 для всех остальных блюд
        }
        tinyDB.putListObject("Список карт", listfood);
        changeNumberItemsListener.changed();
    }

    public void MinusNumerFood(ArrayList<FoodDomain> listfood, int position, ChangeNumberItemsListener changeNumberItemsListener) {
        FoodDomain food = listfood.get(position);
        if (isSpecialFood(food.getTitle())) {
            if (food.getNumberInCart() > 3) { // Уменьшаем количество на 3 для специальных блюд
                food.setNumberInCart(food.getNumberInCart() - 3);
            } else {
                listfood.remove(position); // Удаляем из списка, если количество меньше или равно 3
            }
        } else {
            if (food.getNumberInCart() > 1) { // Уменьшаем количество на 1 для всех остальных блюд
                food.setNumberInCart(food.getNumberInCart() - 1);
            } else {
                listfood.remove(position); // Удаляем из списка, если количество равно 1
            }
        }
        tinyDB.putListObject("Список карт", listfood);
        changeNumberItemsListener.changed();
    }

    public int getTotalFee() {
        ArrayList<FoodDomain> listFood = getListCard();
        int fee = 0;
        for (FoodDomain food : listFood) {
            if (isSpecialFood(food.getTitle())) {
                fee += getPriceForSpecialFood(food); // Получаем специальную цену для специальных продуктов
            } else {
                fee += food.getFee() * food.getNumberInCart(); // Обычная логика для всех остальных продуктов
            }
        }
        return fee;
    }
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

    private int getPriceForSpecialFood(FoodDomain food) {
        switch (food.getTitle()) {
            case "Стрипсы куриные":
                return calculatePrice(food.getNumberInCart(), 119, 189, 259, 119);
            case "Наггетсы куриные":
                return calculatePrice(food.getNumberInCart(), 79, 129, 179, 79);
            case "Сырные палочки":
                return calculatePrice(food.getNumberInCart(), 139, 219, 299, 139);
            default:
                return 0;
        }
    }

}
