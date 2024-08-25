package com.example.budgettracker;


import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
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

public class AddIncomeBottomSheet extends BottomSheetDialogFragment {

    private EditText descriptionEditText, amountEditText;
    private DatabaseReference incomeDatabase;
    private DatabaseReference usersDatabase;
    private FirebaseAuth mAuth;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_add_income_bottomsheet, container, false);

        descriptionEditText = view.findViewById(R.id.descriptionEditText);
        amountEditText = view.findViewById(R.id.amountEditText);
        Button addIncomeButton = view.findViewById(R.id.addIncomeButton);

        incomeDatabase = FirebaseDatabase.getInstance().getReference("incomes");
        usersDatabase = FirebaseDatabase.getInstance().getReference("users");
        mAuth = FirebaseAuth.getInstance();


        addIncomeButton.setOnClickListener(v -> {
            String description = descriptionEditText.getText().toString().trim();
            String amountStr = amountEditText.getText().toString().trim();
            FirebaseUser currentUser = mAuth.getCurrentUser();

            if (validateInput(description, amountStr) && currentUser != null) {
                fetchUsernameAndAddIncome(currentUser.getUid(), description, amountStr);
            }
        });

        return view;
    }

    private void fetchUsernameAndAddIncome(String uid, String description, String amountStr) {
        usersDatabase.child(uid).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String username = snapshot.getValue(String.class);

                if (username != null && !username.isEmpty()) {
                    try {
                        int amount = Integer.parseInt(amountStr);

                        // Get current date and time
                        String dateTime = new SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm:ss", Locale.getDefault()).format(new Date());

                        // Store income in the database
                        String id = incomeDatabase.push().getKey();
                        if (id != null) {
                            Income income = new Income(description, amount, username, dateTime);
                            incomeDatabase.child(id).setValue(income)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(getContext(), "Income Added: " + description + " - Rs " + amount, Toast.LENGTH_SHORT).show();
                                        dismiss(); // Close the bottom sheet after adding income
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(getContext(), "Failed to add income: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
