package org.example.service;

import org.example.model.Account;
import org.example.model.CheckingAccount;
import org.example.model.SavingsAccount;
import org.example.model.exceptions.InvalidAccountException;

import java.util.*;

/**
 * Manages the in-memory collection of {@link Account} objects.
 * <p>
 * Uses an {@link ArrayList} for ordered iteration and a
 * {@link HashMap} for O(1) lookup by account number.
 * All operations that modify shared state are {@code synchronized}
 * to support concurrent access from the simulation feature.
 * </p>
 */
public class AccountManager {

    /** Ordered list of all accounts (preserves insertion order for display). */
    private final List<Account> accounts = new ArrayList<>();

    /** Fast lookup map: accountNumber → Account. */
    private final Map<String, Account> accountMap = new HashMap<>();

    // -------------------------------------------------------------------------
    // CRUD operations
    // -------------------------------------------------------------------------

    /**
     * Adds an account to both the list and the lookup map.
     *
     * @param account the account to add (must not be {@code null})
     * @return {@code true} always (unlimited capacity with ArrayList)
     */
    public synchronized boolean addAccount(Account account) {
        accounts.add(account);
        accountMap.put(account.getAccountNumber(), account);
        return true;
    }

    /**
     * Finds an account by number in O(1) via the map.
     *
     * @param accountNumber the account number to look up
     * @return the matching {@link Account}
     * @throws InvalidAccountException if no account with that number exists
     */
    public Account findAccount(String accountNumber) throws InvalidAccountException {
        Account account = accountMap.get(accountNumber);
        if (account == null) {
            throw new InvalidAccountException(
                    "Account not found: \"" + accountNumber
                    + "\". Please check the account number and try again.");
        }
        return account;
    }

    /**
     * Returns the account for the given number, or {@code null} if absent.
     *
     * @param accountNumber the account number to search for
     * @return matching account or {@code null}
     */
    public Account findAccountOrNull(String accountNumber) {
        return accountMap.get(accountNumber);
    }

    // -------------------------------------------------------------------------
    // Reporting / display
    // -------------------------------------------------------------------------

    /**
     * Prints a formatted table of all accounts to {@code System.out},
     * using a stream to iterate over the collection.
     */
    public void viewAllAccounts() {
        if (accounts.isEmpty()) {
            System.out.println("No accounts found.");
            return;
        }

        System.out.println("\nACCOUNT LISTING\n");
        System.out.println("ACC NO  | CUSTOMER NAME          | TYPE     | BALANCE       | STATUS | DETAILS");
        System.out.println("--------|------------------------|----------|---------------|--------|----------------------------------");

        // Functional: stream + method reference
        accounts.stream().forEach(this::printAccountRow);

        System.out.println("\nTotal Accounts     : " + accounts.size());
        System.out.printf ("Total Bank Balance : $%,.2f%n", getTotalBalance());
    }

    /**
     * Prints a single account row.
     *
     * @param acc the account to print
     */
    private void printAccountRow(Account acc) {
        String details = buildTypeDetails(acc);
        System.out.printf("%-8s| %-23s| %-9s| $%-13s| %-7s| %s%n",
                acc.getAccountNumber(),
                acc.getCustomer().getName(),
                acc.getAccountType(),
                String.format("%,.2f", acc.getBalance()),
                acc.getStatus(),
                details);
    }

    /**
     * Builds a short type-specific detail string.
     *
     * @param acc the account
     * @return formatted detail string
     */
    private String buildTypeDetails(Account acc) {
        if (acc instanceof SavingsAccount sa) {
            return String.format("MinBal: $%,.2f | Rate: %.1f%%",
                    sa.getMinimumBalance(), sa.getInterestRate());
        } else if (acc instanceof CheckingAccount ca) {
            return String.format("Overdraft: $%,.2f | Fee: %s",
                    ca.getOverdraftLimit(),
                    ca.isFeeWaived() ? "WAIVED" : String.format("$%.2f", ca.getMonthlyFee()));
        }
        return "";
    }

    // -------------------------------------------------------------------------
    // Aggregates
    // -------------------------------------------------------------------------

    /**
     * Returns the sum of all account balances using a stream reduction.
     *
     * @return total balance across all accounts
     */
    public double getTotalBalance() {
        return accounts.stream()
                       .mapToDouble(Account::getBalance)
                       .sum();
    }

    /** @return an unmodifiable view of the accounts list */
    public List<Account> getAllAccounts() {
        return Collections.unmodifiableList(accounts);
    }

    /** @return the number of accounts currently stored */
    public int getAccountCount() { return accounts.size(); }

    /**
     * Replaces the current account store with a freshly loaded list.
     * Rebuilds the map from the list.
     * Used by {@link FilePersistenceService} after loading from file.
     *
     * @param loaded the accounts loaded from persistence
     */
    public synchronized void replaceAll(List<Account> loaded) {
        accounts.clear();
        accountMap.clear();
        loaded.forEach(a -> {
            accounts.add(a);
            accountMap.put(a.getAccountNumber(), a);
        });
    }
}
