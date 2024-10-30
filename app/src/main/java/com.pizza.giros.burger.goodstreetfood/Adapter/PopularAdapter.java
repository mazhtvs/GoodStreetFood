package com.pizza.giros.burger.goodstreetfood.Adapter;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.pizza.giros.burger.goodstreetfood.Activiy.ShowDetailActivity;
import com.pizza.giros.burger.goodstreetfood.Activiy.ShowDetailActivity2;
import com.pizza.giros.burger.goodstreetfood.Domain.FoodDomain;

import com.pizza.giros.burger.goodstreetfood.R;

import java.util.ArrayList;

public class PopularAdapter extends RecyclerView.Adapter<PopularAdapter.ViewHolder> {

    ArrayList<FoodDomain> foodDomains;

    public PopularAdapter(ArrayList<FoodDomain> FoodDomains) {
        this.foodDomains = FoodDomains;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_popular,parent,false);

        return new ViewHolder(inflate);
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.tittle.setText(foodDomains.get(position).getTitle());

        // Получаем текущий объект продукта
        FoodDomain food = foodDomains.get(position);

        // Проверяем, является ли продукт "Стрипсы куриные"
        if ("strips".equals(food.getPic())) {
            // Определяем цены для различного количества продуктов
            String price1 = "3 шт. - 119 рублей";
            String price2 = "6 шт. - 189 рублей";
            String price3 = "9 шт. - 259 рублей";

            // Формируем строку для отображения цен
            String fee = price1 + "\n" + price2 + "\n" + price3;

            // Устанавливаем текст стоимости
            holder.fee.setText(fee);
        } else if ("nagets".equals(food.getPic())) {
            // Определяем цены для различного количества продуктов
            String price1 = "3 шт. - 79 рублей";
            String price2 = "6 шт. - 129 рублей";
            String price3 = "9 шт. - 179 рублей";

            // Формируем строку для отображения цен
            String fee = price1 + "\n" + price2 + "\n" + price3;

            // Устанавливаем текст стоимости
            holder.fee.setText(fee);
        } else if ("krispi".equals(food.getPic())) {
            // Определяем цены для различного количества продуктов
            String price1 = "3 шт. - 139 рублей";
            String price2 = "6 шт. - 219 рублей";
            String price3 = "9 шт. - 299 рублей";

            // Формируем строку для отображения цен
            String fee = price1 + "\n" + price2 + "\n" + price3;

            // Устанавливаем текст стоимости
            holder.fee.setText(fee);
        } else {
            // Если продукт не является "Стрипсы куриные" или другими продуктами, отображаем обычную стоимость
            holder.fee.setText(String.valueOf(food.getFee()));
        }

        Glide.with(holder.itemView.getContext())
                .load(food.getPic())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .apply(new RequestOptions().placeholder(R.drawable.goshka).error(R.drawable.goshka))
                .into(holder.pic);

        holder.addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(holder.itemView.getContext(), ShowDetailActivity.class);
                intent.putExtra("object",foodDomains.get(position));
                holder.itemView.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return foodDomains.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView tittle, fee;
        ImageView pic;
        TextView addBtn;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tittle = itemView.findViewById(R.id.tittle);
            fee = itemView.findViewById(R.id.fee);
            pic = itemView.findViewById(R.id.pic);
            addBtn = itemView.findViewById(R.id.addBtn);
        }
    }
}
