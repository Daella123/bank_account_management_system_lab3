package org.example.utils;

import org.example.model.exceptions.InvalidAmountException;

import java.util.regex.Pattern;

/**
 * Centralised validation utility class.
 * <p>
 * All regex {@link Pattern}s are pre-compiled as static constants for
 * efficiency and reuse across the application.
 * Methods throw unchecked or checked exceptions so callers handle errors
 * consistently.
 * </p>
 */
public final class ValidationUtils {

    // -------------------------------------------------------------------------
    // Pre-compiled regex patterns (Lab 3: use Pattern & Matcher)
    // -------------------------------------------------------------------------

    /** Matches the required account number format: ACC followed by exactly 3 digits. */
    private static final Pattern ACCOUNT_NUMBER_PATTERN = Pattern.compile("ACC\\d{3}");

    /**
     * Standard email pattern.
     * Accepts: local-part@domain.tld (e.g. john.smith@bank.com).
     * Rejects: local-part@domain  (no TLD), or malformed addresses.
     */
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    /** Matches names containing letters only (A-Z, a-z) with no spaces or digits. */
    private static final Pattern NAME_PATTERN = Pattern.compile("^[A-Za-z]+$");

    /** Private constructor — utility class, not instantiable. */
    private ValidationUtils() {}

    // -------------------------------------------------------------------------
    // Validation methods
    // -------------------------------------------------------------------------

    /**
     * Validates that a monetary {@code amount} is strictly positive.
     *
     * @param amount the amount to validate
     * @throws InvalidAmountException if {@code amount} is zero or negative
     */
    public static void validateAmount(double amount) throws InvalidAmountException {
        if (amount <= 0) {
            throw new InvalidAmountException(
                    "Invalid amount. Amount must be greater than 0. Received: " + amount);
        }
    }

    /**
    * Validates that a {@code name} string is non-blank and letters-only.
     *
     * @param name  the name to validate
     * @param field the field label used in the error message (e.g. "Customer Name")
    * @throws IllegalArgumentException if {@code name} is null, blank, or contains non-letter characters
     */
    public static void validateName(String name, String field) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(field + " cannot be empty.");
        }
        if (!NAME_PATTERN.matcher(name).matches()) {
            throw new IllegalArgumentException(field + " must contain only letters (A-Z, a-z). No spaces or numbers are allowed.");
        }
    }

    /**
     * Validates that an {@code age} is within an acceptable range (1–120).
     *
     * @param age the age to validate
     * @throws IllegalArgumentException if {@code age} is outside [1, 120]
     */
    public static void validateAge(int age) {
        if (age < 1 || age > 120) {
            throw new IllegalArgumentException(
                    "Age must be between 1 and 120. Received: " + age);
        }
    }

    /**
     * Validates an account number against the {@code ACC\d{3}} pattern.
     *
     * @param accountNumber the account number to validate
     * @throws IllegalArgumentException if {@code accountNumber} is null, blank, or malformed
     */
    public static void validateAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.isBlank()) {
            throw new IllegalArgumentException("Account number cannot be empty.");
        }
        if (!ACCOUNT_NUMBER_PATTERN.matcher(accountNumber).matches()) {
            throw new IllegalArgumentException(
                    "Account number must follow the format ACC### (e.g. ACC001). "
                    + "Received: " + accountNumber);
        }
    }

    /**
     * Validates an email address using the {@link #EMAIL_PATTERN}.
     * <p>
     * Example valid: {@code john.smith@bank.com}<br>
     * Example invalid: {@code john.smith@bank} (no TLD)
     * </p>
     *
     * @param email the email address to validate
     * @throws IllegalArgumentException if the email is null, blank, or does not match the pattern
     */
    public static void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email address cannot be empty.");
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException(
                    "Invalid email format. Expected format: user@domain.com. Received: " + email);
        }
    }
}
