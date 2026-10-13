package bank;

import bank.model.Account;
import bank.model.Transaction;
import bank.model.User;
import bank.service.AccountService;
import bank.service.AuthService;
import bank.util.FileStore;
import bank.util.InputValidator;

import java.io.Console;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * Console entry point. Passwords are read with Console.readPassword()
 * (no echo) when a real terminal is available; users only ever see
 * clean messages, never stack traces.
 */
public final class Main {

    private static final Path DATA_DIR = Path.of("data");

    private final Scanner scanner = new Scanner(System.in);
    private final Console console = System.console();
    private final AuthService auth;
    private final AccountService accounts;

    private Main() {
        FileStore.ensureDirectory(DATA_DIR);
        this.auth = new AuthService(DATA_DIR.resolve("users.txt"));
        this.accounts = new AccountService(
                DATA_DIR.resolve("accounts.txt"),
                DATA_DIR.resolve("transactions.txt"));
    }

    public static void main(String[] args) {
        new Main().run();
    }

    private void run() {
        System.out.println("=====================================");
        System.out.println("   SECURE BANKING APPLICATION");
        System.out.println("=====================================");
        while (true) {
            System.out.println();
            System.out.println("1. Register");
            System.out.println("2. Login");
            System.out.println("3. Exit");
            switch (prompt("Choose an option: ")) {
                case "1" -> register();
                case "2" -> login();
                case "3" -> {
                    System.out.println("Goodbye.");
                    return;
                }
                default -> System.out.println("Please choose 1, 2 or 3.");
            }
        }
    }

    private void register() {
        try {
            String username = prompt("Choose a username (3-20 letters/digits/_): ");
            char[] password = promptPassword(
                "Choose a password (8+ chars, upper, lower, digit, special): ");
            auth.register(username, password);
            System.out.println("Registration successful. You can now log in.");
        } catch (IllegalArgumentException e) {
            System.out.println("Registration failed: " + e.getMessage());
        }
    }

    private void login() {
        try {
            String username = prompt("Username: ");
            char[] password = promptPassword("Password: ");
            Optional<User> user = auth.login(username, password);
            if (user.isPresent()) {
                System.out.println("Welcome, " + user.get().getUsername() + "!");
                sessionMenu(user.get());
            } else {
                System.out.println("Invalid username or password.");
            }
        } catch (IllegalStateException locked) {
            System.out.println(locked.getMessage());
        }
    }

    private void sessionMenu(User user) {
        while (true) {
            System.out.println();
            System.out.println("--- Account Menu (" + user.getUsername() + ") ---");
            System.out.println("1. Create a new bank account");
            System.out.println("2. View my accounts & balances");
            System.out.println("3. Deposit");
            System.out.println("4. Withdraw");
            System.out.println("5. Transaction history");
            System.out.println("6. Logout");
            switch (prompt("Choose an option: ")) {
                case "1" -> createAccount(user);
                case "2" -> viewAccounts(user);
                case "3" -> transact(user, true);
                case "4" -> transact(user, false);
                case "5" -> history(user);
                case "6" -> {
                    System.out.println("Logged out.");
                    return;
                }
                default -> System.out.println("Please choose 1-6.");
            }
        }
    }

    private void createAccount(User user) {
        try {
            String type = prompt("Account type - Current or Savings: ").trim();
            if (!type.equalsIgnoreCase("Current") && !type.equalsIgnoreCase("Savings")) {
                System.out.println("Type must be Current or Savings.");
                return;
            }
            BigDecimal opening = InputValidator.parseAmount(prompt("Opening deposit amount: "));
            Account a = accounts.createAccount(user.getUsername(), type, opening);
            System.out.println("Created " + a.getType() + " account " + a.getAccountNumber()
                    + " with balance $" + a.getBalance());
        } catch (IllegalArgumentException e) {
            System.out.println("Could not create account: " + e.getMessage());
        }
    }

    private void viewAccounts(User user) {
        List<Account> mine = accounts.accountsOf(user.getUsername());
        if (mine.isEmpty()) {
            System.out.println("You have no accounts yet. Create one with option 1.");
            return;
        }
        System.out.printf("%-12s %-9s %s%n", "ACCOUNT NO", "TYPE", "BALANCE");
        for (Account a : mine) {
            System.out.printf("%-12s %-9s $%s%n",
                    a.getAccountNumber(), a.getType(), a.getBalance().toPlainString());
        }
    }

    private void transact(User user, boolean deposit) {
        try {
            String number = prompt("Account number: ").trim();
            BigDecimal amount = InputValidator.parseAmount(prompt("Amount: "));
            if (deposit) {
                accounts.deposit(number, user.getUsername(), amount);
                System.out.println("Deposit successful.");
            } else {
                accounts.withdraw(number, user.getUsername(), amount);
                System.out.println("Withdrawal successful.");
            }
            System.out.println("New balance: $"
                    + accounts.requireOwnedAccount(number, user.getUsername()).getBalance());
        } catch (IllegalArgumentException e) {
            System.out.println("Transaction failed: " + e.getMessage());
        }
    }

    private void history(User user) {
        try {
            String number = prompt("Account number: ").trim();
            List<Transaction> txns = accounts.historyOf(number, user.getUsername());
            if (txns.isEmpty()) {
                System.out.println("No transactions yet for this account.");
                return;
            }
            txns.forEach(t -> System.out.println(t.display()));
        } catch (IllegalArgumentException e) {
            System.out.println("Could not load history: " + e.getMessage());
        }
    }

    private String prompt(String label) {
        System.out.print(label);
        return scanner.nextLine();
    }

    private char[] promptPassword(String label) {
        if (console != null) {
            return console.readPassword(label);   // masked in a real terminal
        }
        System.out.print(label);                   // fallback for IDE terminals
        return scanner.nextLine().toCharArray();
    }
}