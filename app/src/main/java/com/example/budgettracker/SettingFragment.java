package com.example.budgettracker;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SettingFragment extends Fragment {

    private RecyclerView recyclerView;
    private ItemAdapter itemAdapter;
    private TextView emptyMessage;
    private List<Item> itemList;
    private DatabaseReference incomesReference;
    private DatabaseReference expensesReference;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting, container, false);

        // Initialize RecyclerView
        recyclerView = view.findViewById(R.id.recyclerView);
        emptyMessage = view.findViewById(R.id.emptyMessage);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize item list and adapter
        itemList = new ArrayList<>();
        itemAdapter = new ItemAdapter(itemList);
        recyclerView.setAdapter(itemAdapter);

        // Initialize Firebase Database references for incomes and expenses
        incomesReference = FirebaseDatabase.getInstance().getReference("incomes");
        expensesReference = FirebaseDatabase.getInstance().getReference("expenses");

        // Retrieve data from both incomes and expenses nodes
        retrieveDataFromNode(incomesReference);
        retrieveDataFromNode(expensesReference);

        return view;
    }

    private void retrieveDataFromNode(DatabaseReference reference) {
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Item> newItems = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    if (reference.equals(incomesReference)) {
                        Income income = snapshot.getValue(Income.class);
                        if (income != null) {
                            Item item = new Item(income.getName(), income.getDescription(),
                                    String.valueOf(income.getAmount()), income.getDateTime(), "income");
                            newItems.add(item);
                        }
                    } else if (reference.equals(expensesReference)) {
                        Expense expense = snapshot.getValue(Expense.class);
                        if (expense != null) {
                            Item item = new Item(expense.getName(), expense.getDescription(),
                                    String.valueOf(expense.getAmount()), expense.getDateTime(), "expense");
                            newItems.add(item);
                        }
                    }
                }


                itemList.addAll(newItems); // Add all new items
                sortItemsByDateTime(); // Sort the items by date and time
                itemAdapter.notifyDataSetChanged(); // Notify the adapter to update the RecyclerView

                // Show or hide the "Nothing to show" message based on whether the list is empty
                if (itemList.isEmpty()) {
                    emptyMessage.setVisibility(View.VISIBLE);
                } else {
                    emptyMessage.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle possible errors
            }
        });
    }

    private void sortItemsByDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm:ss", Locale.getDefault());
        Collections.sort(itemList, new Comparator<Item>() {
            @Override
            public int compare(Item item1, Item item2) {
                try {
                    Date date1 = sdf.parse(item1.getDateTime());
                    Date date2 = sdf.parse(item2.getDateTime());
                    if (date1 != null && date2 != null) {
                        return date2.compareTo(date1); // Sort in descending order
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                return 0;
            }
        });
    }
}
