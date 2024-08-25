package com.example.budgettracker;

public class Income {
    private String description;
    private int amount;
    private String name;
    private String dateTime; // Add this field

    public Income() {
        // Default constructor required for Firebase
    }

    public Income(String description, int amount, String name, String dateTime) {
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
        return dateTime;
    }
}
