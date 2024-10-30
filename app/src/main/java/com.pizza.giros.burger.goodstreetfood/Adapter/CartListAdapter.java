package com.pizza.giros.burger.goodstreetfood.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.pizza.giros.burger.goodstreetfood.Domain.FoodDomain;
import com.pizza.giros.burger.goodstreetfood.Helper.ManagementCart;
import com.pizza.giros.burger.goodstreetfood.Interface.ChangeNumberItemsListener;
import com.pizza.giros.burger.goodstreetfood.R;


import java.util.ArrayList;

public class CartListAdapter extends RecyclerView.Adapter<CartListAdapter.ViewHolder> {
    private ArrayList<FoodDomain> foodDomains;
    private ManagementCart managementCart;
    private ChangeNumberItemsListener changeNumberItemsListener;

    public CartListAdapter(ArrayList<FoodDomain> FoodDomains, Context context, ChangeNumberItemsListener changeNumberItemsListener) {

        this.foodDomains = FoodDomains;
        managementCart = new ManagementCart(context);
        this.changeNumberItemsListener = changeNumberItemsListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_card, parent, false);

        return new ViewHolder(inflate);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        FoodDomain foodDomain = foodDomains.get(position);
        holder.title.setText(foodDomains.get(position).getTitle());
        holder.feeEachItem.setText(String.valueOf(foodDomains.get(position).getFee()));
        holder.num.setText(String.valueOf(foodDomains.get(position).getNumberInCart()));

        // Проверяем, является ли блюдо специальным
        if (isSpecialFood(foodDomains.get(position).getTitle())) {
            // Если блюдо специальное, используем метод getPriceText
            holder.totalEachItem.setText(getPriceText(foodDomains.get(position).getTitle(), foodDomains.get(position).getNumberInCart()));
        } else {
            // Если блюдо не специальное, используем ваш исходный подход
            holder.totalEachItem.setText(String.valueOf(Math.round((foodDomains.get(position).getNumberInCart() * foodDomains.get(position).getFee()) * 100) / 100));
        }

        Glide.with(holder.itemView.getContext())
                .load(foodDomain.getPic())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.pic);

        holder.plusItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                managementCart.plusNumberFood(foodDomains, position, new ChangeNumberItemsListener() {
                    @Override
                    public void changed() {
                        notifyDataSetChanged();
                        changeNumberItemsListener.changed();
                    }
                });
            }
        });

        holder.minusItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                managementCart.MinusNumerFood(foodDomains, position, new ChangeNumberItemsListener() {
                    @Override
                    public void changed() {
                        notifyDataSetChanged();
                        changeNumberItemsListener.changed();
                    }
                });
            }
        });
    }

    // Метод для проверки, является ли блюдо специальным
    private boolean isSpecialFood(String foodTitle) {
        return "Стрипсы куриные".equals(foodTitle) || "Наггетсы куриные".equals(foodTitle) || "Сырные палочки".equals(foodTitle);
    }

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


    @Override
    public int getItemCount() {
        return foodDomains.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, feeEachItem;
        ImageView pic, plusItem, minusItem;
        TextView totalEachItem, num;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title2Txt);
            feeEachItem = itemView.findViewById(R.id.feeEachItem);
            pic = itemView.findViewById(R.id.picCard);
            totalEachItem = itemView.findViewById(R.id.totalEachItem);
            num = itemView.findViewById(R.id.numberItemTxt);
            plusItem = itemView.findViewById(R.id.plusCardBtn);
            minusItem = itemView.findViewById(R.id.minusCardBtn);
        }
    }
}
