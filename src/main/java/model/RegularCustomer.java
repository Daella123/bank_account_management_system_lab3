package model;

/**
 * A standard bank customer with no premium privileges.
 */
public class RegularCustomer extends Customer {

    public RegularCustomer(String name, int age, String email, String contact, String address) {
        super(name, age, email, contact, address);
    }

    /**
     * Backward-compatible constructor kept for Lab 2 test compatibility.
     * Signature: (name, age, contact, address).
     */
    public RegularCustomer(String name, int age, String contact, String address) {
        super(name, age, "unknown@bank.com", contact, address);
    }

    /** Deserialization constructor (no counter increment). */
    public RegularCustomer(String customerId, String name, int age, String email, String contact, String address) {
        super(customerId, name, age, email, contact, address);
    }

    @Override
    public void displayCustomerDetails() {
        System.out.println("Customer ID : " + getCustomerId());
        System.out.println("Name        : " + getName());
        System.out.println("Age         : " + getAge());
        System.out.println("Email       : " + getEmail());
        System.out.println("Contact     : " + getContact());
        System.out.println("Address     : " + getAddress());
        System.out.println("Type        : " + getCustomerType());
    }

    @Override
    public String getCustomerType() {
        return "Regular";
    }
}

