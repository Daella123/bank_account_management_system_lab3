package model.exceptions;

/**
 * Exception thrown when a withdrawal on a Checking Account would
 * exceed the account's permitted overdraft limit.
 */
public class OverdraftExceededException extends Exception {

    /** The overdraft limit configured for the account. */
    private final double overdraftLimit;

    /**
     * Constructs an OverdraftExceededException with a detail message
     * and the account's overdraft limit.
     *
     * @param message       human-readable description of the error
     * @param overdraftLimit the maximum overdraft amount allowed
     */
    public OverdraftExceededException(String message, double overdraftLimit) {
        super(message);
        this.overdraftLimit = overdraftLimit;
    }

    /**
     * Returns the overdraft limit of the account.
     *
     * @return the overdraft limit
     */
    public double getOverdraftLimit() {
        return overdraftLimit;
    }
}
