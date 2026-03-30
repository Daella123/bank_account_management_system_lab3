package org.example.model;

/**
 * A premium bank customer with elevated privileges (fee waiver, higher limits).
 */
public class PremiumCustomer extends Customer {
    private double minimumBalance;

    public PremiumCustomer(String name, int age, String email, String contact, String address) {
        super(name, age, email, contact, address);
        this.minimumBalance = 10_000.0;
    }

    /**
     * Backward-compatible constructor kept for Lab 2 test compatibility.
     * Signature: (name, age, contact, address).
     */
    public PremiumCustomer(String name, int age, String contact, String address) {
        super(name, age, "unknown@bank.com", contact, address);
        this.minimumBalance = 10_000.0;
    }

    /** Deserialization constructor (no counter increment). */
    public PremiumCustomer(String customerId, String name, int age, String email, String contact, String address) {
        super(customerId, name, age, email, contact, address);
        this.minimumBalance = 10_000.0;
    }

    public double getMinimumBalance()               { return minimumBalance; }
    public void   setMinimumBalance(double amount)  { this.minimumBalance = amount; }
    public boolean hasWaivedFees()                  { return true; }

    @Override
    public void displayCustomerDetails() {
        System.out.println("Customer ID        : " + getCustomerId());
        System.out.println("Name               : " + getName());
        System.out.println("Age                : " + getAge());
        System.out.println("Email              : " + getEmail());
        System.out.println("Contact            : " + getContact());
        System.out.println("Address            : " + getAddress());
        System.out.println("Type               : " + getCustomerType());
        System.out.println("Premium Benefits   : Higher limits, Waived fees, Priority service");
        System.out.printf ("Minimum Balance    : $%,.2f%n", minimumBalance);
    }

    @Override
    public String getCustomerType() {
        return "Premium";
    }
}

