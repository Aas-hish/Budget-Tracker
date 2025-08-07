package com.example.budgettracker;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class homeFragment extends Fragment {
    private static final String TAG = "fragment";

    private TextView totalBalanceTextView;
    private DatabaseReference incomeDatabase, expenseDatabase, usersDatabase;
    private FirebaseAuth firebaseAuth;
    private RecyclerView userRecyclerView;
    private UserBalanceAdapter userBalanceAdapter;
    private List<UserBalance> userBalances;

    public homeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: Fragment created");
        firebaseAuth = FirebaseAuth.getInstance();
        usersDatabase = FirebaseDatabase.getInstance().getReference("users");
        incomeDatabase = FirebaseDatabase.getInstance().getReference("incomes");
        expenseDatabase = FirebaseDatabase.getInstance().getReference("expenses");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        Log.d(TAG, "onCreateView: View inflated");

        userRecyclerView = view.findViewById(R.id.userRecyclerView);
        userRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        userBalances = new ArrayList<>();
        userBalanceAdapter = new UserBalanceAdapter(userBalances);
        userRecyclerView.setAdapter(userBalanceAdapter);
        Log.d(TAG, "onCreateView: RecyclerView setup complete");

        totalBalanceTextView = view.findViewById(R.id.totalBalance);

        // Load users with their balances
        loadUsersWithBalances();
        calculateTotalBalance();

        view.findViewById(R.id.addIncomeButton).setOnClickListener(v -> {
            Log.d(TAG, "Add Income button clicked");
            AddIncomeBottomSheet bottomSheet = new AddIncomeBottomSheet();
            bottomSheet.show(getParentFragmentManager(), "AddIncomeBottomSheet");
        });

        view.findViewById(R.id.addExpenseButton).setOnClickListener(v -> {
            Log.d(TAG, "Add Expense button clicked");
            AddExpenseBottomSheet bottomSheet = new AddExpenseBottomSheet();
            bottomSheet.show(getParentFragmentManager(), "AddExpenseBottomSheet");
        });

        view.findViewById(R.id.chat).setOnClickListener(v -> {
            Log.d(TAG, "Chat button clicked");
            startActivity(new Intent(getActivity(), ChatBox.class));
        });

        return view;
    }

    private void calculateTotalBalance() {
        Log.d(TAG, "calculateTotalBalance: Starting calculation");
        incomeDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot incomeSnapshot) {
                Log.d(TAG, "onDataChange: Income data received, count: " + incomeSnapshot.getChildrenCount());
                int totalIncome = 0;
                for (DataSnapshot incomeData : incomeSnapshot.getChildren()) {
                    Integer amount = incomeData.child("amount").getValue(Integer.class);
                    if (amount != null) {
                        totalIncome += amount;
                    }
                }
                Log.d(TAG, "onDataChange: Total income calculated: " + totalIncome);

                int finalTotalIncome = totalIncome;
                expenseDatabase.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot expenseSnapshot) {
                        Log.d(TAG, "onDataChange: Expense data received, count: " + expenseSnapshot.getChildrenCount());
                        int totalExpense = 0;
                        for (DataSnapshot expenseData : expenseSnapshot.getChildren()) {
                            Integer amount = expenseData.child("amount").getValue(Integer.class);
                            if (amount != null) {
                                totalExpense += amount;
                            }
                        }
                        Log.d(TAG, "onDataChange: Total expense calculated: " + totalExpense);

                        int totalBalance = finalTotalIncome - totalExpense;
                        totalBalanceTextView.setText("₹ " + totalBalance);
                        Log.d(TAG, "onDataChange: Total balance set to: " + totalBalance);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "onCancelled: Failed to load expenses: " + error.getMessage());
                        Toast.makeText(getContext(), "Failed to load expenses", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "onCancelled: Failed to load incomes: " + error.getMessage());
                Toast.makeText(getContext(), "Failed to load incomes", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadUsersWithBalances() {
        Log.d(TAG, "loadUsersWithBalances: Starting to load users");
        usersDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange: Users data received, count: " + dataSnapshot.getChildrenCount());
                userBalances.clear();
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    String userId = userSnapshot.getKey();
                    String userName = userSnapshot.child("name").getValue(String.class);

                    if (userId != null && userName != null) {
                        Log.d(TAG, "onDataChange: Found user - ID: " + userId + ", Name: " + userName);
                        // Add user immediately with 0 balance
                        userBalances.add(new UserBalance(userId, userName, 0));
                        // Then calculate their balance
                        calculateUserBalance(userId, userName);
                    }
                }
                // Initial notify here to show users immediately
                userBalanceAdapter.notifyDataSetChanged();
                Log.d(TAG, "onDataChange: User list updated with " + userBalances.size() + " users");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "onCancelled: Failed to load users: " + databaseError.getMessage());
                Toast.makeText(getContext(), "Failed to load users", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void calculateUserBalance(String userId, String userName) {
        Log.d(TAG, "calculateUserBalance: Calculating balance for user: " + userName);
        // Calculate income for this user
        incomeDatabase.orderByChild("name").equalTo(userName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot incomeSnapshot) {
                Log.d(TAG, "onDataChange: Income data for " + userName + ", count: " + incomeSnapshot.getChildrenCount());
                int userIncome = 0;
                for (DataSnapshot incomeData : incomeSnapshot.getChildren()) {
                    Integer amount = incomeData.child("amount").getValue(Integer.class);
                    if (amount != null) {
                        userIncome += amount;
                    }
                }
                Log.d(TAG, "onDataChange: Total income for " + userName + ": " + userIncome);

                // Calculate expenses for this user
                int finalUserIncome = userIncome;
                expenseDatabase.orderByChild("name").equalTo(userName).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot expenseSnapshot) {
                        Log.d(TAG, "onDataChange: Expense data for " + userName + ", count: " + expenseSnapshot.getChildrenCount());
                        int userExpense = 0;
                        for (DataSnapshot expenseData : expenseSnapshot.getChildren()) {
                            Integer amount = expenseData.child("amount").getValue(Integer.class);
                            if (amount != null) {
                                userExpense += amount;
                            }
                        }
                        Log.d(TAG, "onDataChange: Total expense for " + userName + ": " + userExpense);

                        int userBalance = finalUserIncome - userExpense;
                        Log.d(TAG, "onDataChange: Calculated balance for " + userName + ": " + userBalance);
                        updateUserBalance(userId, userBalance);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "onCancelled: Failed to load expenses for " + userName + ": " + error.getMessage());
                        Toast.makeText(getContext(), "Failed to load expenses", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "onCancelled: Failed to load incomes for " + userName + ": " + error.getMessage());
                Toast.makeText(getContext(), "Failed to load incomes", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUserBalance(String userId, int newBalance) {
        Log.d(TAG, "updateUserBalance: Updating balance for user ID: " + userId + " to: " + newBalance);
        for (int i = 0; i < userBalances.size(); i++) {
            if (userBalances.get(i).getUserId().equals(userId)) {
                userBalances.get(i).setBalance(newBalance);
                // Notify specific position changed for better performance
                userBalanceAdapter.notifyItemChanged(i);
                Log.d(TAG, "updateUserBalance: Balance updated at position: " + i);
                break;
            }
        }
    }
}