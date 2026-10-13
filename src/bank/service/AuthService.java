package bank.service;

import bank.model.User;
import bank.util.FileStore;
import bank.util.InputValidator;
import bank.util.PasswordHasher;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


public class AuthService {

    private static final int MAX_ATTEMPTS = 3;

    private final Path usersFile;
    private final Map<String, User> users = new HashMap<>();
    private final Map<String, Integer> failedAttempts = new HashMap<>();

    public AuthService(Path usersFile) {
        this.usersFile = usersFile;
        for (String line : FileStore.loadAll(usersFile)) {
            if (line.isBlank()) continue;
            try {
                User u = User.fromFileLine(line);
                users.put(u.getUsername(), u);
            } catch (RuntimeException bad) {
                System.err.println("[warn] Skipping corrupt user record");
            }
        }
    }

    public User register(String username, char[] password) {
        if (!InputValidator.isValidUsername(username)) {
            throw new IllegalArgumentException(
                "Username must be 3-20 characters: letters, digits or underscore only");
        }
        if (users.containsKey(username)) {
            throw new IllegalArgumentException("That username is already taken");
        }
        // Check strength BEFORE hashing — hashBase64 wipes the password array
        if (!InputValidator.isStrongPassword(password)) {
            throw new IllegalArgumentException(
                "Password must be 8+ characters with uppercase, lowercase, digit and special character");
        }
        String salt = PasswordHasher.newSaltBase64();
        String hash = PasswordHasher.hashBase64(password, salt);
        User user = new User(username, salt, hash);
        users.put(username, user);
        persist();
        return user;
    }

    public Optional<User> login(String username, char[] password) {
        if (isLocked(username)) {
            java.util.Arrays.fill(password, '\0');
            throw new IllegalStateException(
                "This account is locked after too many failed attempts. Restart the app to try again.");
        }
        User user = users.get(username);
        boolean ok = user != null
                && PasswordHasher.verifyPassword(password, user.getSaltbase64(), user.getHashbased64());
        if (ok) {
            failedAttempts.remove(username);
            return Optional.of(user);
        }
        failedAttempts.merge(username, 1, Integer::sum);
        return Optional.empty();
    }

    public boolean isLocked(String username) {
        return failedAttempts.getOrDefault(username, 0) >= MAX_ATTEMPTS;
    }

    private void persist() {
        FileStore.saveAll(usersFile, new ArrayList<>(users.values()), User::toFileString);
    }
}