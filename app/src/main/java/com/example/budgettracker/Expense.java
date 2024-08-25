package com.example.budgettracker;

public class Expense {
    private String description;
    private int amount;
    private String name;
    private String dateTime; // Add this field for storing day, date, and time

    public Expense() {
        // Default constructor required for Firebase
    }

    public Expense(String description, int amount, String name, String dateTime) {
        this.description = description;
        this.amount = amount;
        this.name = name;
        this.dateTime = dateTime; // Initialize the dateTime
    }

    public String getDescription() {
        return description;
    }

    public int getAmount() {
        return amount;
    }

    public String getName() {
        return name;
    }

    public String getDateTime() {
        return dateTime; // Getter for the dateTime field
    }
}
