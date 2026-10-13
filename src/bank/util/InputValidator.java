package bank.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.regex.Pattern;

public final class InputValidator {

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,20}$");
    private static final BigDecimal MAX_AMOUNT = new BigDecimal("1000000.00");

    private InputValidator() {}

    public static boolean isValidUsername(String username) {
        return username != null && USERNAME_PATTERN.matcher(username).matches();
    }

    public static boolean isStrongPassword(char[] password) {
        if (password == null || password.length < 8) {
            return false;
        }
        boolean hasUppercase = false;
        boolean hasLowercase = false;
        boolean hasDigit = false;
        boolean hasSpecialChar = false;

        for (char c : password) {
            if (Character.isUpperCase(c)) {
                hasUppercase = true;
            } else if (Character.isLowerCase(c)) {
                hasLowercase = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            } else if (!Character.isLetterOrDigit(c)) {
                hasSpecialChar = true;
            }
        }
        return hasUppercase && hasLowercase && hasDigit && hasSpecialChar;
    }

    public static BigDecimal parseAmount(String amountStr) {
        try {
            BigDecimal amount = new BigDecimal(amountStr.trim());
            if (amount.compareTo(BigDecimal.ZERO) <= 0 || amount.compareTo(MAX_AMOUNT) > 0) {
                throw new IllegalArgumentException("Amount must be between 0 and " + MAX_AMOUNT);
            }
            return amount.setScale(2, RoundingMode.HALF_UP);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid amount format: " + amountStr, e);
        }
    }
}