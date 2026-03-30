package model;

import org.example.contract.Transactable;
import org.example.model.exceptions.InsufficientFundsException;
import org.example.model.exceptions.InvalidAmountException;
import org.example.model.exceptions.OverdraftExceededException;
import org.example.utils.ValidationUtils;

/**
 * Abstract base class representing a bank account.
 * <p>
 * Holds common fields (account number, customer, balance, status) and
 * provides default {@link #deposit} and {@link #withdraw} implementations.
 * Subclasses override {@link #withdraw} to enforce account-specific rules
 * such as minimum-balance or overdraft constraints.
 * </p>
 */
public abstract class Account implements Transactable {

    /** Auto-incrementing counter used to generate unique account numbers. */
    private static int accountCounter = 0;

    private final String accountNumber;
    private Customer customer;
    private double balance;
    private String status;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    /**
     * Creates a new account, auto-generating a unique account number.
     *
     * @param customer       the account owner
     * @param initialBalance the opening balance
     */
    public Account(Customer customer, double initialBalance) {
        accountCounter++;
        this.accountNumber = String.format("ACC%03d", accountCounter);
        this.customer      = customer;
        this.balance       = initialBalance;
        this.status        = "Active";
    }

    /**
     * Deserialization constructor — does NOT increment the counter.
     * Used exclusively by {@link org.example.service.FilePersistenceService}.
     *
     * @param accountNumber explicit account number loaded from file
     * @param customer      the deserialized customer
     * @param balance       the stored balance
     * @param status        the stored status string
     */
    protected Account(String accountNumber, Customer customer, double balance, String status) {
        this.accountNumber = accountNumber;
        this.customer      = customer;
        this.balance       = balance;
        this.status        = status;
    }

    /** Resets the account counter — called after file load to re-sync ID generation. */
    public static void resetCounter(int value) { accountCounter = value; }

    // -------------------------------------------------------------------------
    // Getters & Setters
    // -------------------------------------------------------------------------

    /** @return the auto-generated account number (e.g. {@code ACC001}) */
    public String getAccountNumber() { return accountNumber; }

    /** @return the customer who owns this account */
    public Customer getCustomer() { return customer; }

    /** @param customer replacement customer reference */
    public void setCustomer(Customer customer) { this.customer = customer; }

    /** @return current account balance */
    public double getBalance() { return balance; }

    /**
     * Sets the balance directly.
     * This is {@code protected} so only subclasses and the service layer
     * can mutate balance — external code must go through {@link #deposit}/{@link #withdraw}.
     *
     * @param balance the new balance value
     */
    protected void setBalance(double balance) { this.balance = balance; }

    /** @return the account status (e.g. {@code "Active"}) */
    public String getStatus() { return status; }

    /** @param status the new status string */
    public void setStatus(String status) { this.status = status; }

    // -------------------------------------------------------------------------
    // Core transaction methods
    // -------------------------------------------------------------------------

    /**
     * Deposits the specified {@code amount} into this account.
     *
     * @param amount the positive amount to deposit
     * @throws InvalidAmountException if {@code amount} is zero or negative
     */
    public void deposit(double amount) throws InvalidAmountException {
        ValidationUtils.validateAmount(amount);
        balance += amount;
    }

    /**
     * Withdraws the specified {@code amount} from this account.
     * <p>
     * The base implementation prevents the balance from going below zero.
     * Subclasses should override this to enforce additional rules.
     * </p>
     *
     * @param amount the positive amount to withdraw
     * @throws InvalidAmountException      if {@code amount} is zero or negative
     * @throws InsufficientFundsException  if the account has insufficient funds
     * @throws OverdraftExceededException  if a subclass enforces overdraft limits
     */
    public void withdraw(double amount)
            throws InvalidAmountException, InsufficientFundsException, OverdraftExceededException {
        ValidationUtils.validateAmount(amount);
        if (balance < amount) {
            throw new InsufficientFundsException(
                    "Insufficient funds. Current balance: $"
                    + String.format("%,.2f", balance),
                    balance);
        }
        balance -= amount;
    }

    // -------------------------------------------------------------------------
    // Transactable interface implementation (legacy / UI compatibility)
    // -------------------------------------------------------------------------

    /**
     * Processes a transaction using a string-based type descriptor.
     * <p>
     * This method is kept for backward-compatibility with the console UI.
     * It wraps the checked-exception methods and returns a boolean result.
     * Prefer the typed {@link #deposit}/{@link #withdraw} methods in new code.
     * </p>
     *
     * @param amount the transaction amount
     * @param type   {@code "DEPOSIT"} or {@code "WITHDRAWAL"} (case-insensitive)
     * @return {@code true} if the transaction succeeded; {@code false} otherwise
     */
    @Override
    public boolean processTransaction(double amount, String type) {
        if (type == null) return false;
        try {
            if (type.equalsIgnoreCase("DEPOSIT")) {
                deposit(amount);
                return true;
            } else if (type.equalsIgnoreCase("WITHDRAWAL")) {
                withdraw(amount);
                return true;
            }
        } catch (Exception e) {
            // Caller reads the return value; exception details are swallowed here
        }
        return false;
    }

    // -------------------------------------------------------------------------
    // Abstract methods
    // -------------------------------------------------------------------------

    /**
     * Prints a formatted summary of this account to {@code System.out}.
     * Subclasses must implement this to show account-type-specific details.
     */
    public abstract void displayAccountDetails();

    /**
     * Returns a short human-readable label for the account type.
     *
     * @return e.g. {@code "Savings"} or {@code "Checking"}
     */
    public abstract String getAccountType();

    /**
     * Serialises this account to a pipe-delimited line for file persistence.
     * Format defined by each subclass.
     *
     * @return a single-line string representing this account
     */
    public abstract String toFileString();
}
