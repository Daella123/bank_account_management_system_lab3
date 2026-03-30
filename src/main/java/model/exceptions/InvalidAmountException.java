package model.exceptions;

/**
 * Exception thrown when an invalid monetary amount is provided.
 * <p>
 * This is typically triggered when a user enters a zero or negative
 * amount for a deposit or withdrawal operation.
 * </p>
 */
public class InvalidAmountException extends Exception {

    /**
     * Constructs an InvalidAmountException with the given detail message.
     *
     * @param message a human-readable description of the error
     */
    public InvalidAmountException(String message) {
        super(message);
    }
}
