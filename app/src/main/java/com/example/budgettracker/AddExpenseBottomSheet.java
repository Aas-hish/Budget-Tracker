package com.example.budgettracker;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AddExpenseBottomSheet extends BottomSheetDialogFragment {

    private EditText descriptionEditText, amountEditText;
    private Button addExpenseButton;
    private DatabaseReference expenseDatabase;
    private DatabaseReference usersDatabase;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_add_expense_botton_sheet, container, false);

        descriptionEditText = view.findViewById(R.id.descriptionEditText);
        amountEditText = view.findViewById(R.id.amountEditText);
        addExpenseButton = view.findViewById(R.id.addExpenseButton);

        expenseDatabase = FirebaseDatabase.getInstance().getReference("expenses");
        usersDatabase = FirebaseDatabase.getInstance().getReference("users");
        mAuth = FirebaseAuth.getInstance();

        addExpenseButton.setOnClickListener(v -> {
            String description = descriptionEditText.getText().toString().trim();
            String amountStr = amountEditText.getText().toString().trim();
            FirebaseUser currentUser = mAuth.getCurrentUser();

            if (validateInput(description, amountStr) && currentUser != null) {
                fetchUsernameAndAddExpense(currentUser.getUid(), description, amountStr);
            }
        });

        return view;
    }

    private void fetchUsernameAndAddExpense(String uid, String description, String amountStr) {
        usersDatabase.child(uid).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String username = snapshot.getValue(String.class);

                if (username != null && !username.isEmpty()) {
                    try {
                        int amount = Integer.parseInt(amountStr);

                        // Get current date and time
                        String dateTime = new SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm:ss", Locale.getDefault()).format(new Date());

                        // Store expense in the database
                        String id = expenseDatabase.push().getKey();
                        if (id != null) {
                            Expense expense = new Expense(description, amount, username, dateTime);
                            expenseDatabase.child(id).setValue(expense)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(getContext(), "Expense Added: " + description + " - Rs " + amount, Toast.LENGTH_SHORT).show();
                                        dismiss(); // Close the bottom sheet after adding expense
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(getContext(), "Failed to add expense: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        }
                    } catch (NumberFormatException e) {
                        amountEditText.setError("Please enter a valid amount");
                    }
                } else {
                    Toast.makeText(getContext(), "Failed to retrieve username", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to retrieve username: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean validateInput(String description, String amount) {
        if (TextUtils.isEmpty(description)) {
            descriptionEditText.setError("Description is required");
            return false;
        }
        if (TextUtils.isEmpty(amount)) {
            amountEditText.setError("Amount is required");
            return false;
        }
        try {
            int amountValue = Integer.parseInt(amount);
            if (amountValue <= 0) {
                amountEditText.setError("Amount must be greater than 0");
                return false;
            }
        } catch (NumberFormatException e) {
            amountEditText.setError("Please enter a valid amount");
            return false;
        }
        return true;
    }
}
