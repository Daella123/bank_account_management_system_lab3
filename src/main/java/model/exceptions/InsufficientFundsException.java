package model.exceptions;

/**
 * Exception thrown when a withdrawal cannot be completed due to
 * insufficient account balance (and no overdraft facility available).
 */
public class InsufficientFundsException extends Exception {

    /** The current balance at the time of the failed transaction. */
    private final double currentBalance;

    /**
     * Constructs an InsufficientFundsException with a detail message
     * and the account's current balance for informational purposes.
     *
     * @param message        human-readable description of the error
     * @param currentBalance the balance in the account when the error occurred
     */
    public InsufficientFundsException(String message, double currentBalance) {
        super(message);
        this.currentBalance = currentBalance;
    }

    /**
     * Returns the account balance that was available when the exception occurred.
     *
     * @return the insufficient balance amount
     */
    public double getCurrentBalance() {
        return currentBalance;
    }
}
