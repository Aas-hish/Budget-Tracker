package com.example.budgettracker;

import android.os.Bundle;
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

    private TextView totalBalanceTextView;
    private DatabaseReference incomeDatabase, expenseDatabase;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    private RecyclerView userRecyclerView;
    private UserAdapter userAdapter;
    private List<String> userNames;


    public homeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("users");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        userRecyclerView = view.findViewById(R.id.userRecyclerView);
        userRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        userNames = new ArrayList<>();
        userAdapter = new UserAdapter(userNames);
        userRecyclerView.setAdapter(userAdapter);

        loadUserNames();

        totalBalanceTextView = view.findViewById(R.id.totalBalance);
        incomeDatabase = FirebaseDatabase.getInstance().getReference("incomes");
        expenseDatabase = FirebaseDatabase.getInstance().getReference("expenses");

        // Retrieve and calculate total balance
        retrieveAndCalculateTotalBalance();

        view.findViewById(R.id.addIncomeButton).setOnClickListener(v -> {
            AddIncomeBottomSheet bottomSheet = new AddIncomeBottomSheet();
            bottomSheet.show(getParentFragmentManager(), "AddIncomeBottomSheet");
        });

        view.findViewById(R.id.addExpenseButton).setOnClickListener(v -> {
            AddExpenseBottomSheet bottomSheet = new AddExpenseBottomSheet();
            bottomSheet.show(getParentFragmentManager(), "AddExpenseBottomSheet");
        });

        return view;
    }
    private void loadUserNames() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userNames.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String name = snapshot.child("name").getValue(String.class);
                    if (name != null) {
                        userNames.add(name);
                    }
                }
                userAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "Failed to load user names", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void retrieveAndCalculateTotalBalance() {
        incomeDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int totalIncome = 0;
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Income income = dataSnapshot.getValue(Income.class);
                    if (income != null) {
                        totalIncome += income.getAmount();
                    }
                }

                // Calculate total expenses
                int finalTotalIncome = totalIncome;
                expenseDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        int totalExpenses = 0;
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Expense expense = dataSnapshot.getValue(Expense.class);
                            if (expense != null) {
                                totalExpenses += expense.getAmount();
                            }
                        }

                        // Calculate the total balance
                        int totalBalance = finalTotalIncome - totalExpenses;
                        totalBalanceTextView.setText("₹  " + totalBalance);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Handle possible errors.
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle possible errors.
            }
        });
    }
}
