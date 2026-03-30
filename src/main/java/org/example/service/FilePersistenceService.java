package org.example.service;

import org.example.model.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles saving and loading of {@link Account} and {@link Transaction}
 * data to/from plain-text pipe-delimited files using Java NIO.
 * <p>
 * File format (accounts.txt):
 * <pre>
 *   SAVINGS|ACC001|Active|5250.0|3.5|500.0|Regular|CUST001|Mpamo Avy|35|mpamo@bank.com|+250-789...|KG 480, Kigali
 *   CHECKING|ACC002|Active|3450.0|1000.0|10.0|Regular|CUST002|...
 * </pre>
 * File format (transactions.txt):
 * <pre>
 *   TXN001|ACC001|DEPOSIT|1500.0|6750.0|25-03-2026 10:30 AM
 * </pre>
 * </p>
 */
public class FilePersistenceService {

    // -------------------------------------------------------------------------
    // SAVE
    // -------------------------------------------------------------------------

    /**
     * Saves all accounts to {@code path} using Java NIO {@link Files#write}.
     * Each account is serialised via {@link Account#toFileString()}.
     *
     * @param accounts the list of accounts to persist
     * @param path     the destination file path
     */
    public void saveAccounts(List<Account> accounts, Path path) {
        System.out.println("\nSAVING ACCOUNT DATA");
        System.out.println("-------------------");
        try {
            ensureParentExists(path);
            List<String> lines = accounts.stream()
                    .map(Account::toFileString)
                    .collect(Collectors.toList());
            Files.write(path, lines,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
            System.out.println("✓ Accounts saved to " + path.getFileName() + " (" + lines.size() + " records)");
        } catch (IOException e) {
            System.out.println("✗ Failed to save accounts: " + e.getMessage());
        }
    }

    /**
     * Saves all transactions to {@code path} using Java NIO {@link Files#write}.
     * Each transaction is serialised via {@link Transaction#toFileString()}.
     *
     * @param transactions the list of transactions to persist
     * @param path         the destination file path
     */
    public void saveTransactions(List<Transaction> transactions, Path path) {
        try {
            ensureParentExists(path);
            List<String> lines = transactions.stream()
                    .map(Transaction::toFileString)
                    .collect(Collectors.toList());
            Files.write(path, lines,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
            System.out.println("✓ Transactions saved to " + path.getFileName() + " (" + lines.size() + " records)");
        } catch (IOException e) {
            System.out.println("✗ Failed to save transactions: " + e.getMessage());
        }
    }

    /**
     * Convenience method: saves both accounts and transactions, then prints
     * the confirmation banner required by the lab spec.
     *
     * @param accounts     list of all accounts
     * @param transactions list of all transactions
     * @param accountsPath path to accounts.txt
     * @param txPath       path to transactions.txt
     */
    public void saveAll(List<Account> accounts, List<Transaction> transactions,
                        Path accountsPath, Path txPath) {
        saveAccounts(accounts, accountsPath);
        saveTransactions(transactions, txPath);
        System.out.println("File save completed successfully.");
    }

    // -------------------------------------------------------------------------
    // LOAD
    // -------------------------------------------------------------------------

    /**
     * Loads accounts from {@code path} using {@link Files#lines()}.
     * <p>
     * Expected line format:
     * {@code TYPE|accNum|status|balance|param1|param2|customerType|custId|name|age|email|contact|address}
     * </p>
     *
     * @param path the source file path
     * @return list of deserialized accounts (empty if file missing or unreadable)
     */
    public List<Account> loadAccounts(Path path) {
        List<Account> result = new ArrayList<>();
        if (!Files.exists(path)) return result;

        System.out.println("\nLOADING ACCOUNT DATA");
        System.out.println("--------------------");
        try {
            List<String> lines = Files.lines(path)
                    .filter(l -> !l.isBlank())
                    .collect(Collectors.toList());

            for (String line : lines) {
                try {
                    Account account = parseAccount(line);
                    result.add(account);
                } catch (Exception e) {
                    System.out.println("  ⚠ Skipping malformed line: " + e.getMessage());
                }
            }
            System.out.println("✓ Loaded " + result.size() + " account(s) from " + path.getFileName());

            // Re-sync counter so new accounts get the next id
            resyncAccountCounter(result);

        } catch (IOException e) {
            System.out.println("✗ Failed to load accounts: " + e.getMessage());
        }
        return result;
    }

    /**
     * Loads transactions from {@code path} using {@link Files#lines()}.
     *
     * @param path the source file path
     * @return list of deserialized transactions (empty if file missing or unreadable)
     */
    public List<Transaction> loadTransactions(Path path) {
        List<Transaction> result = new ArrayList<>();
        if (!Files.exists(path)) return result;

        try {
            List<String> lines = Files.lines(path)
                    .filter(l -> !l.isBlank())
                    .filter(l -> !l.stripLeading().startsWith("#"))
                    .collect(Collectors.toList());

            for (String line : lines) {
                try {
                    result.add(Transaction.fromFileString(line));
                } catch (Exception e) {
                    System.out.println("  ⚠ Skipping malformed transaction line: " + e.getMessage());
                }
            }

            System.out.println("✓ Loaded " + result.size() + " transaction(s) from " + path.getFileName());

            // Re-sync counter
            resyncTransactionCounter(result);

        } catch (IOException e) {
            System.out.println("✗ Failed to load transactions: " + e.getMessage());
        }
        return result;
    }

    // -------------------------------------------------------------------------
    // Parsing helpers
    // -------------------------------------------------------------------------

    /**
     * Parses a single account line.
     * Format: {@code TYPE|accNum|status|balance|p1|p2|custType|custId|name|age|email|contact|address}
     *
     * @param line a pipe-delimited account line
     * @return a fully reconstructed Account subtype
     */
    private Account parseAccount(String line) {
        // Total tokens: TYPE accNum status balance p1 p2 custType custId name age email contact address
        //               0    1      2      3       4  5  6        7      8    9   10    11      12
        String[] p = line.split("\\|", -1);
        if (p.length < 13) throw new IllegalArgumentException("Expected 13+ fields, got " + p.length + ": " + line);

        String type          = p[0];
        String accNum        = p[1];
        String status        = p[2];
        double balance       = Double.parseDouble(p[3]);
        double param1        = Double.parseDouble(p[4]);
        double param2        = Double.parseDouble(p[5]);
        String custType      = p[6];
        String custId        = p[7];
        String name          = p[8];
        int    age           = Integer.parseInt(p[9]);
        String email         = p[10];
        String contact       = p[11];
        String address       = p[12];

        Customer customer = buildCustomer(custType, custId, name, age, email, contact, address);

        return switch (type) {
            case "SAVINGS"  -> new SavingsAccount(accNum, customer, balance, status, param1, param2);
            case "CHECKING" -> new CheckingAccount(accNum, customer, balance, status, param1, param2);
            default         -> throw new IllegalArgumentException("Unknown account type: " + type);
        };
    }

    /**
     * Builds a Customer from the deserialized fields.
     *
     * @param custType 'Regular' or 'Premium'
     * @param custId   stored customer ID
     * @param name     customer name
     * @param age      customer age
     * @param email    customer email
     * @param contact  phone / contact string
     * @param address  postal address
     * @return the appropriate Customer subtype
     */
    private Customer buildCustomer(String custType, String custId,
                                   String name, int age, String email,
                                   String contact, String address) {
        return switch (custType) {
            case "Premium" -> new PremiumCustomer(custId, name, age, email, contact, address);
            default        -> new RegularCustomer(custId, name, age, email, contact, address);
        };
    }

    // -------------------------------------------------------------------------
    // Counter re-sync
    // -------------------------------------------------------------------------

    /**
     * Finds the highest numeric suffix in account numbers and resets the counter.
     * e.g. if max account is ACC005, counter is set to 5 so next is ACC006.
     */
    private void resyncAccountCounter(List<Account> accounts) {
        int max = accounts.stream()
                .map(Account::getAccountNumber)
                .filter(n -> n.startsWith("ACC"))
                .mapToInt(n -> {
                    try { return Integer.parseInt(n.substring(3)); }
                    catch (NumberFormatException e) { return 0; }
                })
                .max()
                .orElse(0);
        Account.resetCounter(max);
    }

    /**
     * Finds the highest numeric suffix in transaction IDs and resets the counter.
     */
    private void resyncTransactionCounter(List<Transaction> transactions) {
        int max = transactions.stream()
                .map(Transaction::getTransactionId)
                .filter(id -> id.startsWith("TXN"))
                .mapToInt(id -> {
                    try { return Integer.parseInt(id.substring(3)); }
                    catch (NumberFormatException e) { return 0; }
                })
                .max()
                .orElse(0);
        Transaction.resetCounter(max);
    }

    // -------------------------------------------------------------------------
    // NIO helpers
    // -------------------------------------------------------------------------

    /**
     * Ensures the parent directory of {@code path} exists, creating it if needed.
     *
     * @param path the file whose parent must exist
     * @throws IOException if the directory cannot be created
     */
    private void ensureParentExists(Path path) throws IOException {
        Path parent = path.getParent();
        if (parent != null && !Files.exists(parent)) {
            Files.createDirectories(parent);
        }
    }
}
