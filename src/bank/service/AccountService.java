package bank.service;

import bank.model.Account;
import bank.model.SavingsAccount;
import bank.model.Transaction;
import bank.util.FileStore;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;








public class AccountService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final Path accountsFile;
    private final Path transactionsFile;
    private final Map<String, Account> accounts = new LinkedHashMap<>();

    public AccountService(Path accountsFile, Path transactionsFile) {
        this.accountsFile = accountsFile;
        this.transactionsFile = transactionsFile;
        for (String line : FileStore.loadAll(accountsFile)) {
            if (line.isBlank()) continue;
            try {
                Account a = Account.fromFileLine(line);
                accounts.put(a.getAccountNumber(), a);
            } catch (RuntimeException bad) {
                System.err.println("[warn] Skipping corrupt account record");
            }
        }
    }

    public Account createAccount(String ownerUsername, String type, BigDecimal openingBalance) {
        String number = generateAccountNumber();
        Account account = "Savings".equalsIgnoreCase(type)
                ? new SavingsAccount(number, ownerUsername, openingBalance)
                : new Account(number, ownerUsername, openingBalance);
        accounts.put(number, account);
        persist();
        return account;
    }

    public List<Account> accountsOf(String ownerUsername) {
        List<Account> mine = new ArrayList<>();
        for (Account a : accounts.values()) {
            if (a.getOwnerUsername().equals(ownerUsername)) mine.add(a);
        }
        return mine;
    }

    /** Same message for "not found" and "not yours" — no information leak. */
    public Account requireOwnedAccount(String accountNumber, String requestingUser) {
        Account a = accounts.get(accountNumber);
        if (a == null || !a.getOwnerUsername().equals(requestingUser)) {
            throw new IllegalArgumentException("No such account under your profile");
        }
        return a;
    }

    public void deposit(String accountNumber, String requestingUser, BigDecimal amount) {
        Account a = requireOwnedAccount(accountNumber, requestingUser);
        a.deposit(amount);
        record(a, Transaction.Type.DEPOSIT, amount);
    }

    public void withdraw(String accountNumber, String requestingUser, BigDecimal amount) {
        Account a = requireOwnedAccount(accountNumber, requestingUser);
        a.withdraw(amount);
        record(a, Transaction.Type.WITHDRAWAL, amount);
    }

    public List<Transaction> historyOf(String accountNumber, String requestingUser) {
        requireOwnedAccount(accountNumber, requestingUser); // authorisation first
        List<Transaction> mine = new ArrayList<>();
        for (String line : FileStore.loadAll(transactionsFile)) {
            if (line.isBlank()) continue;
            try {
                Transaction t = Transaction.fromFileLine(line);
                if (t.getAccountNumber().equals(accountNumber)) mine.add(t);
            } catch (RuntimeException bad) {
                System.err.println("[warn] Skipping corrupt transaction record");
            }
        }
        return mine;
    }

    private void record(Account account, Transaction.Type type, BigDecimal amount) {
        persist();
        Transaction t = new Transaction(
                account.getAccountNumber(), LocalDateTime.now(), amount, account.getBalance(), type);
        FileStore.appendToFile(transactionsFile, t.toFileLine());
    }

    private void persist() {
        FileStore.saveAll(accountsFile, new ArrayList<>(accounts.values()), Account::toFileLine);
    }

    private String generateAccountNumber() {
        String number;
        do {
            number = String.format("%010d", Math.abs(RANDOM.nextLong()) % 10_000_000_000L);
        } while (accounts.containsKey(number));
        return number;
    }
}