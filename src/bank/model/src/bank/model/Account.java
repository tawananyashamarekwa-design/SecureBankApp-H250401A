package bank.model;

import java.math.BigDecimal;

public class Account {

    private final String accountNumber;
    private final String ownerUsername;
    private BigDecimal balance;

    public Account(String accountNumber, String ownerUsername, BigDecimal balance) {
        if (accountNumber == null || ownerUsername == null) {
            throw new IllegalArgumentException("Account fields should not be null");
        }
        if (balance == null || balance.signum() < 0) {
            throw new IllegalArgumentException("Balance should not be null or negative");
        }
        this.accountNumber = accountNumber;
        this.ownerUsername = ownerUsername;
        this.balance = balance;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getOwnerUsername() {
        return ownerUsername;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public String getType() {
        return "Current";
    }

    public void deposit(BigDecimal amount) {
        requirePositiveAmount(amount);
        balance = balance.add(amount);
    }

    public void withdraw(BigDecimal amount) {
        requirePositiveAmount(amount);
        if (balance.compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient funds");
        }
        balance = balance.subtract(amount);
    }

    protected void requirePositiveAmount(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Amount should be positive");
        }
    }

    public String toFileLine() {
        return accountNumber + "," + ownerUsername + "," + getType() + "," + balance.toPlainString();
    }

    public static Account fromFileLine(String line) {
        String[] parts = line.split(",");
        if (parts.length != 4) {
            throw new IllegalArgumentException("Corrupt account record");
        }
        BigDecimal bal = new BigDecimal(parts[3]);
        if ("Savings".equals(parts[2])) {
            return new SavingsAccount(parts[0], parts[1], bal);
        }
        return new Account(parts[0], parts[1], bal);
    }
}