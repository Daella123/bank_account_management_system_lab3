package model;

/**
 * Abstract base class representing a bank customer.
 * <p>
 * Holds common customer fields and provides serialisation support
 * for file persistence via {@link #toFileString()}.
 * </p>
 */
public abstract class Customer {
    private static int customerCounter = 0;

    private final String customerId;
    private String name;
    private int age;
    private String email;
    private String contact;
    private String address;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    public Customer(String name, int age, String email, String contact, String address) {
        customerCounter++;
        this.customerId = String.format("CUST%03d", customerCounter);
        this.name    = name;
        this.age     = age;
        this.email   = email;
        this.contact = contact;
        this.address = address;
    }

    /** Package-private constructor used during file deserialization (no counter increment). */
    Customer(String customerId, String name, int age, String email, String contact, String address) {
        this.customerId = customerId;
        this.name    = name;
        this.age     = age;
        this.email   = email;
        this.contact = contact;
        this.address = address;
    }

    /** Resets the customer counter — called during file load to re-sync the ID sequence. */
    public static void resetCounter(int value) {
        customerCounter = value;
    }

    // -------------------------------------------------------------------------
    // Getters & Setters
    // -------------------------------------------------------------------------

    public String getCustomerId()    { return customerId; }
    public String getName()          { return name; }
    public int    getAge()           { return age; }
    public String getEmail()         { return email; }
    public String getContact()       { return contact; }
    public String getAddress()       { return address; }

    public void setName(String name)       { this.name    = name; }
    public void setAge(int age)            { this.age     = age; }
    public void setEmail(String email)     { this.email   = email; }
    public void setContact(String contact) { this.contact = contact; }
    public void setAddress(String address) { this.address = address; }

    // -------------------------------------------------------------------------
    // Serialization
    // -------------------------------------------------------------------------

    /**
     * Serialises this customer to a pipe-delimited string for file persistence.
     * Format: {@code customerType|customerId|name|age|email|contact|address}
     */
    public String toFileString() {
        return String.join("|",
                getCustomerType(),
                customerId,
                name,
                String.valueOf(age),
                email,
                contact,
                address);
    }

    // -------------------------------------------------------------------------
    // Abstract methods
    // -------------------------------------------------------------------------

    public abstract void displayCustomerDetails();
    public abstract String getCustomerType();
}
