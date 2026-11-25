package tradesim.models;

import tradesim.exceptions.InsufficientFundsException;

public class User {
    private final String userId;
    private final String username;
    private double cashBalance;

    public User(String userId, String username, double initialDeposit) {
        if (userId == null || userId.trim().isEmpty() || username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("User ID and username cannot be empty.");
        }
        if (initialDeposit < 0) {
            throw new IllegalArgumentException("Initial deposit cannot be negative.");
        }
        this.userId = userId;
        this.username = username;
        this.cashBalance = initialDeposit;
    }

    public void debit(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Debit amount must be positive.");
        }
        if (this.cashBalance < amount) {
            if (Math.abs(this.cashBalance - amount) < 1e-6) {
                this.cashBalance = 0.0;
                return;
            }
            throw new InsufficientFundsException(
                String.format("Insufficient funds. Required: Rs. %.2f, Available: Rs. %.2f", amount, this.cashBalance)
            );
        }
        this.cashBalance -= amount;
        if (Math.abs(this.cashBalance) < 1e-9) {
            this.cashBalance = 0.0;
        }
    }

    public void credit(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Credit amount must be positive.");
        }
        this.cashBalance += amount;
    }

    public String getUserId() { return userId; }
    public String getUsername() { return username; }
    public double getCashBalance() { return cashBalance; }

    @Override
    public String toString() {
        return String.format("User[%s | %s | Cash: Rs. %,.2f]", userId, username, cashBalance);
    }
}
