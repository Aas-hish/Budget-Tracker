package com.example.budgettracker;

public class Item {
    private String name;
    private String description;
    private String amount;
    private String dateTime;
    private String type; // "income" or "expense"

    // Default constructor required for calls to DataSnapshot.getValue(Item.class)
    public Item() {
    }

    public Item(String name, String description, String amount, String dateTime, String type) {
        this.name = name;
        this.description = description;
        this.amount = amount;
        this.dateTime = dateTime;
        this.type = type;
    }

    // Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
