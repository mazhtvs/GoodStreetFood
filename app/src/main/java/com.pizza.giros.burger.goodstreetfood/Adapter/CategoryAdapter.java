package com.pizza.giros.burger.goodstreetfood.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.pizza.giros.burger.goodstreetfood.Activiy.CategoryDetailActivity;
import com.pizza.giros.burger.goodstreetfood.Domain.CategoryDomain;
import com.pizza.giros.burger.goodstreetfood.Domain.FoodDomain;
import com.pizza.giros.burger.goodstreetfood.R;

import java.util.ArrayList;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {

    ArrayList<CategoryDomain> categorys;
    private CategoryClickListener categoryClickListener;

    public CategoryAdapter(ArrayList<CategoryDomain> categorys) {
        this.categorys = categorys;
    }

    public interface CategoryClickListener {
        void onCategoryClick(CategoryDomain category);
    }

    public void setCategoryClickListener(CategoryClickListener listener) {
        this.categoryClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_cat,parent,false);
        return new ViewHolder(inflate);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final CategoryDomain category = categorys.get(position);
        holder.categoryName.setText(category.getTitle());

        String picUrl = getCategoryImageUrl(position);
        switch (position){
            case 0:
                picUrl = "cat_1";
                holder.mainLayout.setBackground(ContextCompat.getDrawable(holder.itemView.getContext(),R.drawable.category_background1));
                break;
            case 1:
                picUrl = "cat_2";
                holder.mainLayout.setBackground(ContextCompat.getDrawable(holder.itemView.getContext(),R.drawable.category_background2));
                break;
            case 2:
                picUrl = "cat_3";
                holder.mainLayout.setBackground(ContextCompat.getDrawable(holder.itemView.getContext(),R.drawable.category_background3));
                break;
            case 3:
                picUrl = "cat_40";
                holder.mainLayout.setBackground(ContextCompat.getDrawable(holder.itemView.getContext(),R.drawable.category_background4));
                break;
            case 4:
                picUrl = "fri_icon";
                holder.mainLayout.setBackground(ContextCompat.getDrawable(holder.itemView.getContext(),R.drawable.category_background5));
                break;
            case 5:
                picUrl = "cat_50";
                holder.mainLayout.setBackground(ContextCompat.getDrawable(holder.itemView.getContext(),R.drawable.category_background1));
                break;
            case 6:
                picUrl = "cat_60";
                holder.mainLayout.setBackground(ContextCompat.getDrawable(holder.itemView.getContext(),R.drawable.category_background2));
                break;
            case 7:
                picUrl = "sous_icon";
                holder.mainLayout.setBackground(ContextCompat.getDrawable(holder.itemView.getContext(),R.drawable.category_background3));
                break;
        }
        int drawableResourceId = holder.itemView.getContext().getResources().getIdentifier(picUrl, "drawable", holder.itemView.getContext().getPackageName());
        Glide.with(holder.itemView.getContext()).load(drawableResourceId).into(holder.categoryPic);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (categoryClickListener != null) {
                    categoryClickListener.onCategoryClick(category);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return categorys.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView categoryName;
        ImageView categoryPic;
        ConstraintLayout mainLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryName = itemView.findViewById(R.id.categoryName);
            categoryPic = itemView.findViewById(R.id.categoryPic);
            mainLayout = itemView.findViewById(R.id.mainLayout);
        }
    }

    private String getCategoryImageUrl(int position) {
        switch (position) {
            case 0:
                return "cat_1";
            case 1:
                return "cat_2";
            case 2:
                return "cat_3";
            case 3:
                return "cat_40";
            case 4:
                return "cat_50";
            case 5:
                return "cat_60";
            default:
                return "";
        }
    }
}
