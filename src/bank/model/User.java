package bank.model;

public final class User {                     

    private final String username;
    private final String saltbase64;
    private final String hashbased64;

    public User(String username, String saltbase64, String hashbased64) {
        if (username == null || saltbase64 == null || hashbased64 == null) {
            throw new IllegalArgumentException("User fields should not be null");
        }
        this.username = username;
        this.saltbase64 = saltbase64;
        this.hashbased64 = hashbased64;
    }                                         

    public String getUsername() {
        return username;
    }

    public String getSaltbase64() {
        return saltbase64;
    }

    public String getHashbased64() {
        return hashbased64;
    }

    public String toFileString() {
        return username + "," + saltbase64 + "," + hashbased64;
    }

    public static User fromFileLine(String line) {
        String[] parts = line.split(",");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Corrupt user record");
        }
        return new User(parts[0], parts[1], parts[2]);
    }

    @Override
    public String toString() {
        return "User{" + username + "}";       
    }
}                                             