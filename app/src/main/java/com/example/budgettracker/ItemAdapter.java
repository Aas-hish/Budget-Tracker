package com.example.budgettracker;

import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ViewHolder> {
    private List<Item> itemList;

    public ItemAdapter(List<Item> itemList) {
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recycle, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Item item = itemList.get(position);
        holder.textViewDescription.setText(item.getDescription());
        holder.textViewDateTime.setText(item.getDateTime());
        holder.textViewName.setText(item.getName());

        // Format the amount and set the color
        String amountText = item.getAmount();
        String type = item.getType();
        if ("income".equals(type)) {
            holder.textViewAmount.setTextColor(Color.GREEN);
            holder.textViewAmount.setText("+" +" ₹ " + amountText);
        } else if ("expense".equals(type)) {
            holder.textViewAmount.setTextColor(Color.RED);
            holder.textViewAmount.setText("-" +" ₹ "+ amountText);
        } else {
            holder.textViewAmount.setTextColor(Color.BLACK); // Default color
            holder.textViewAmount.setText(amountText);
        }
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewDescription;
        TextView textViewAmount;
        TextView textViewDateTime;
        TextView textViewName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewDescription = itemView.findViewById(R.id.description);
            textViewAmount = itemView.findViewById(R.id.money);
            textViewDateTime = itemView.findViewById(R.id.dateTime);
            textViewName = itemView.findViewById(R.id.name);
        }
    }
}
