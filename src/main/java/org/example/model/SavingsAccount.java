package org.example.model;

import org.example.model.exceptions.InsufficientFundsException;
import org.example.model.exceptions.InvalidAmountException;
import org.example.utils.ValidationUtils;

/**
 * A savings account that enforces a minimum balance requirement on withdrawals.
 * <p>
 * Default settings:  interest rate = 3.5%, minimum balance = $500.00.
 * </p>
 */
public class SavingsAccount extends Account {

    /** Annual interest rate in percent (e.g. 3.5 means 3.5%). */
    private double interestRate;

    /** The minimum balance that must remain after any withdrawal. */
    private double minimumBalance;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    /**
     * Creates a new SavingsAccount with default interest rate and minimum balance.
     *
     * @param customer       the account owner
     * @param initialBalance the opening deposit amount
     */
    public SavingsAccount(Customer customer, double initialBalance) {
        super(customer, initialBalance);
        this.interestRate   = 3.5;
        this.minimumBalance = 500.0;
    }

    /** Deserialization constructor — bypasses counter, restores exact state from file. */
    public SavingsAccount(String accountNumber, Customer customer, double balance, String status,
                          double interestRate, double minimumBalance) {
        super(accountNumber, customer, balance, status);
        this.interestRate   = interestRate;
        this.minimumBalance = minimumBalance;
    }

    // -------------------------------------------------------------------------
    // Getters & Setters
    // -------------------------------------------------------------------------

    /** @return the annual interest rate as a percentage */
    public double getInterestRate() { return interestRate; }

    /** @param interestRate new annual interest rate in percent */
    public void setInterestRate(double interestRate) { this.interestRate = interestRate; }

    /** @return the minimum balance that must remain after each withdrawal */
    public double getMinimumBalance() { return minimumBalance; }

    /** @param minimumBalance the new minimum balance threshold */
    public void setMinimumBalance(double minimumBalance) { this.minimumBalance = minimumBalance; }

    // -------------------------------------------------------------------------
    // Business logic
    // -------------------------------------------------------------------------

    /**
     * Withdraws {@code amount} while ensuring the balance never drops below
     * {@link #minimumBalance}.
     *
     * @param amount the positive amount to withdraw
     * @throws InvalidAmountException     if {@code amount} is zero or negative
     * @throws InsufficientFundsException if the withdrawal would violate the
     *                                    minimum-balance constraint
     */
    @Override
    public void withdraw(double amount)
            throws InvalidAmountException, InsufficientFundsException {
        ValidationUtils.validateAmount(amount);
        if (getBalance() - amount < minimumBalance) {
            throw new InsufficientFundsException(
                    String.format(
                            "Withdrawal denied. Balance after withdrawal ($%,.2f) would be "
                            + "below minimum balance ($%,.2f).",
                            getBalance() - amount, minimumBalance),
                    getBalance());
        }
        setBalance(getBalance() - amount);
    }

    /**
     * Calculates simple annual interest on the current balance.
     *
     * @return the interest amount (not yet applied to the balance)
     */
    public double calculateInterest() {
        return getBalance() * (interestRate / 100.0);
    }

    // -------------------------------------------------------------------------
    // Display
    // -------------------------------------------------------------------------

    @Override
    public void displayAccountDetails() {
        System.out.println("Account Number : " + getAccountNumber());
        System.out.println("Customer       : " + getCustomer().getName()
                + " (" + getCustomer().getCustomerType() + ")");
        System.out.println("Account Type   : " + getAccountType());
        System.out.printf ("Balance        : $%,.2f%n", getBalance());
        System.out.println("Status         : " + getStatus());
        System.out.printf ("Interest Rate  : %.1f%%%n", interestRate);
        System.out.printf ("Minimum Balance: $%,.2f%n", minimumBalance);
    }

    @Override
    public String getAccountType() { return "Savings"; }

    /**
     * Serialises to a pipe-delimited line.
     * Format: {@code SAVINGS|accNum|status|balance|interestRate|minimumBalance|<customer fields>}
     */
    @Override
    public String toFileString() {
        return String.join("|",
                "SAVINGS",
                getAccountNumber(),
                getStatus(),
                String.valueOf(getBalance()),
                String.valueOf(interestRate),
                String.valueOf(minimumBalance),
                getCustomer().toFileString());
    }
}
