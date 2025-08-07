package com.example.budgettracker;

public class UserBalance {
    private String userId;
    private String userName;
    private int balance;

    public UserBalance(String userId, String userName, int balance) {
        this.userId = userId;
        this.userName = userName;
        this.balance = balance;
    }

    // Getters and setters
    public String getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public int getBalance() {
        return balance;
    }

    public void setBalance(int balance) {
        this.balance = balance;
    }
    @Override
    public String toString() {
        return userName + " - ₹" + balance;
    }
}