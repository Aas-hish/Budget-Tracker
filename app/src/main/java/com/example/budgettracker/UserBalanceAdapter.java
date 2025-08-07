package com.example.budgettracker;


import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
public class UserBalanceAdapter extends RecyclerView.Adapter<UserBalanceAdapter.UserViewHolder> {
    private List<UserBalance> userBalances;

    public UserBalanceAdapter(List<UserBalance> userBalances) {
        this.userBalances = userBalances;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user_balance, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        UserBalance userBalance = userBalances.get(position);
        holder.userNameTextView.setText(userBalance.getUserName());
        holder.userBalanceTextView.setText(String.format("₹ %d", userBalance.getBalance()));
    }

    @Override
    public int getItemCount() {
        return userBalances.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        public TextView userNameTextView;
        public TextView userBalanceTextView;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            userNameTextView = itemView.findViewById(R.id.userName);
            userBalanceTextView = itemView.findViewById(R.id.userBalance);

            // Debug check
            if (userNameTextView == null || userBalanceTextView == null) {
                throw new RuntimeException("TextView IDs not found in layout!");
            }
        }
    }
}