package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents a single financial transaction recorded against an account.
 * <p>
 * Supports serialisation ({@link #toFileString()}) and deserialisation
 * ({@link #fromFileString(String)}) for file persistence.
 * </p>
 */
public class Transaction {
    private static int transactionCounter = 0;

    private final String transactionId;
    private final String accountNumber;
    private final String type;
    private final double amount;
    private final double balanceAfter;
    private final String timestamp;

    /** Standard constructor — auto-increments the transaction ID counter. */
    public Transaction(String accountNumber, String type, double amount, double balanceAfter) {
        transactionCounter++;
        this.transactionId  = String.format("TXN%03d", transactionCounter);
        this.accountNumber  = accountNumber;
        this.type           = type;
        this.amount         = amount;
        this.balanceAfter   = balanceAfter;
        LocalDateTime now   = LocalDateTime.now();
        this.timestamp      = now.format(DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm a"));
    }

    /** Private restoration constructor — does NOT increment the counter. */
    private Transaction(String transactionId, String accountNumber, String type,
                        double amount, double balanceAfter, String timestamp) {
        this.transactionId = transactionId;
        this.accountNumber = accountNumber;
        this.type          = type;
        this.amount        = amount;
        this.balanceAfter  = balanceAfter;
        this.timestamp     = timestamp;
    }

    /** Resets the transaction counter — called after file load to re-sync ID generation. */
    public static void resetCounter(int value) { transactionCounter = value; }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    public String getTransactionId() { return transactionId; }
    public String getAccountNumber() { return accountNumber; }
    public String getType()          { return type; }
    public double getAmount()        { return amount; }
    public double getBalanceAfter()  { return balanceAfter; }
    public String getTimestamp()     { return timestamp; }

    // -------------------------------------------------------------------------
    // Display
    // -------------------------------------------------------------------------

    public void displayTransactionDetails() {
        System.out.println("\nTRANSACTION CONFIRMATION");
        System.out.println("Transaction ID : " + transactionId);
        System.out.println("Account        : " + accountNumber);
        System.out.println("Type           : " + type);
        System.out.printf ("Amount         : $%,.2f%n", amount);
        System.out.printf ("Balance After  : $%,.2f%n", balanceAfter);
        System.out.println("Date/Time      : " + timestamp);
    }

    // -------------------------------------------------------------------------
    // File persistence
    // -------------------------------------------------------------------------

    /**
     * Serialises to a pipe-delimited line.
     * Format: {@code txnId|accNum|type|amount|balanceAfter|timestamp}
     */
    public String toFileString() {
        return String.join("|",
                transactionId,
                accountNumber,
                type,
                String.valueOf(amount),
                String.valueOf(balanceAfter),
                timestamp);
    }

    /**
     * Deserialises a line produced by {@link #toFileString()}.
     *
     * @param line a pipe-delimited transaction line
     * @return a fully restored {@link Transaction}
     * @throws IllegalArgumentException if the line is malformed
     */
    public static Transaction fromFileString(String line) {
        String[] p = line.split("\\|", -1);
        if (p.length < 6) throw new IllegalArgumentException("Malformed transaction line: " + line);
        return new Transaction(
                p[0],                      // transactionId
                p[1],                      // accountNumber
                p[2],                      // type
                Double.parseDouble(p[3]),  // amount
                Double.parseDouble(p[4]),  // balanceAfter
                p[5]                       // timestamp
        );
    }

    @Override
    public String toString() {
        return String.format("%-8s | %-20s | %-18s | $%,.2f | $%,.2f",
                transactionId, timestamp, type, amount, balanceAfter);
    }
}

