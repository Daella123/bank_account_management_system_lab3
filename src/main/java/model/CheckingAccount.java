package model;

import org.example.model.exceptions.InvalidAmountException;
import org.example.model.exceptions.OverdraftExceededException;
import org.example.utils.ValidationUtils;

/**
 * A checking account that supports overdraft withdrawals up to a configurable limit.
 * <p>
 * Default settings: overdraft limit = $1,000.00, monthly fee = $10.00.
 * Premium customers have their monthly fee waived automatically.
 * </p>
 */
public class CheckingAccount extends Account {

    /** Maximum amount by which the balance is allowed to go negative. */
    private double overdraftLimit;

    /** Monthly maintenance fee charged to non-premium customers. */
    private double monthlyFee;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    /**
     * Creates a new CheckingAccount with default overdraft limit and monthly fee.
     *
     * @param customer       the account owner
     * @param initialBalance the opening deposit amount
     */
    public CheckingAccount(Customer customer, double initialBalance) {
        super(customer, initialBalance);
        this.overdraftLimit = 1000.0;
        this.monthlyFee     = 10.0;
    }

    /** Deserialization constructor — bypasses counter, restores exact state from file. */
    public CheckingAccount(String accountNumber, Customer customer, double balance, String status,
                           double overdraftLimit, double monthlyFee) {
        super(accountNumber, customer, balance, status);
        this.overdraftLimit = overdraftLimit;
        this.monthlyFee     = monthlyFee;
    }

    // -------------------------------------------------------------------------
    // Getters & Setters
    // -------------------------------------------------------------------------

    /** @return the maximum overdraft amount permitted */
    public double getOverdraftLimit() { return overdraftLimit; }

    /** @param overdraftLimit new overdraft limit */
    public void setOverdraftLimit(double overdraftLimit) { this.overdraftLimit = overdraftLimit; }

    /** @return the monthly maintenance fee */
    public double getMonthlyFee() { return monthlyFee; }

    /** @param monthlyFee new monthly fee */
    public void setMonthlyFee(double monthlyFee) { this.monthlyFee = monthlyFee; }

    // -------------------------------------------------------------------------
    // Business logic
    // -------------------------------------------------------------------------

    /**
     * Withdraws {@code amount}, allowing the balance to go negative as far as
     * {@link #overdraftLimit}, but no further.
     *
     * @param amount the positive amount to withdraw
     * @throws InvalidAmountException      if {@code amount} is zero or negative
     * @throws OverdraftExceededException  if the withdrawal would exceed the overdraft limit
     */
    @Override
    public void withdraw(double amount)
            throws InvalidAmountException, OverdraftExceededException {
        ValidationUtils.validateAmount(amount);
        if (getBalance() + overdraftLimit < amount) {
            throw new OverdraftExceededException(
                    String.format(
                            "Withdrawal denied. Amount ($%,.2f) exceeds available funds "
                            + "including overdraft limit ($%,.2f). "
                            + "Current balance: $%,.2f.",
                            amount, overdraftLimit, getBalance()),
                    overdraftLimit);
        }
        setBalance(getBalance() - amount);
    }

    /**
     * Applies the monthly maintenance fee, unless the customer is a
     * {@link PremiumCustomer} with fee waiver active.
     */
    public void applyMonthlyFee() {
        if (isFeeWaived()) return;
        setBalance(getBalance() - monthlyFee);
    }

    /**
     * Determines whether the monthly fee is waived for this account's customer.
     *
     * @return {@code true} if the customer is premium and fees are waived
     */
    public boolean isFeeWaived() {
        return (getCustomer() instanceof PremiumCustomer premiumCustomer)
                && premiumCustomer.hasWaivedFees();
    }

    // -------------------------------------------------------------------------
    // Display
    // -------------------------------------------------------------------------

    @Override
    public void displayAccountDetails() {
        System.out.println("Account Number  : " + getAccountNumber());
        System.out.println("Customer        : " + getCustomer().getName()
                + " (" + getCustomer().getCustomerType() + ")");
        System.out.println("Account Type    : " + getAccountType());
        System.out.printf ("Balance         : $%,.2f%n", getBalance());
        System.out.println("Status          : " + getStatus());
        System.out.printf ("Overdraft Limit : $%,.2f%n", overdraftLimit);
        if (isFeeWaived()) {
            System.out.println("Monthly Fee     : WAIVED (Premium Customer)");
        } else {
            System.out.printf("Monthly Fee     : $%.2f%n", monthlyFee);
        }
    }

    @Override
    public String getAccountType() { return "Checking"; }

    /**
     * Serialises to a pipe-delimited line.
     * Format: {@code CHECKING|accNum|status|balance|overdraftLimit|monthlyFee|<customer fields>}
     */
    @Override
    public String toFileString() {
        return String.join("|",
                "CHECKING",
                getAccountNumber(),
                getStatus(),
                String.valueOf(getBalance()),
                String.valueOf(overdraftLimit),
                String.valueOf(monthlyFee),
                getCustomer().toFileString());
    }
}
