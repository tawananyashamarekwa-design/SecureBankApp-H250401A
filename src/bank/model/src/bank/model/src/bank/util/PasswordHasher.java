package bank.util;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.security.spec.KeySpec;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public final class PasswordHasher {

    private static final int SALT_LENGTH = 16;
    private static final int ITERATIONS = 65536;
    private static final int KEY_LENGTH = 256;
    private static final SecureRandom secureRandom = new SecureRandom();

    private PasswordHasher() {}

    public static String newSaltBase64() {
        byte[] salt = new byte[SALT_LENGTH];
        secureRandom.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    public static String hashBase64(char[] password, String saltBase64) {
        try {
            byte[] salt = Base64.getDecoder().decode(saltBase64);
            KeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] hash = factory.generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Error hashing password", e);
        } finally {
            java.util.Arrays.fill(password, '\0');
        }
    }

    public static boolean verifyPassword(char[] password, String saltBase64, String expectedHashBase64) {
        String computed = hashBase64(password, saltBase64);
        return MessageDigest.isEqual(
                Base64.getDecoder().decode(computed),
                Base64.getDecoder().decode(expectedHashBase64));
    }
}