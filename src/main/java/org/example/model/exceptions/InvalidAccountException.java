package org.example.model.exceptions;

/**
 * Exception thrown when an operation references an account number
 * that does not exist in the system.
 */
public class InvalidAccountException extends Exception {

    /**
     * Constructs an InvalidAccountException with the given detail message.
     *
     * @param message a human-readable description of the error
     */
    public InvalidAccountException(String message) {
        super(message);
    }
}
